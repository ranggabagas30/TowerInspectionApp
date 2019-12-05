package com.sap.inspection.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sap.inspection.BuildConfig;
import com.sap.inspection.TowerApplication;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.model.form.WorkFormItemModel;
import com.sap.inspection.model.form.WorkFormModel;
import com.sap.inspection.model.value.CorrectiveValueModel;
import com.sap.inspection.model.value.FormValueModel;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.ImageUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import io.reactivex.Observable;

public abstract class ScheduleBaseModel extends BaseModel {

	public String id;
	public float progress = 0;
	public String status;
	public String work_date;
	public String work_date_str;
	public String day_date;
	public ClientModel client;
	public SiteModel site;
	public String operatorIds;
	public ArrayList<OperatorModel> operators;
	public ArrayList<Integer> general_corrective_item_ids;
	public UserModel user;
	public WorkTypeModel work_type;
	public WorkFormModel work_form;
	public ProjectModel project;
	public RejectionModel rejection;
	public ArrayList<Integer> hidden; // SAP
	public String tt_number; // SAP
	public ArrayList<FormValueModel> schedule_values;
	public ArrayList<DefaultValueScheduleModel> default_value_schedule;
	public String statusColor;
	public String taskColor;
	public int sumTask = 0;
	public int sumTaskDone = 0;
	public boolean isSeparator = false;
	public boolean isAnimated = false;
	public int operator_number = 0;

	public ScheduleBaseModel() {}

	public static String createDB(){
		StringBuilder createTableBuilder = new StringBuilder("create table if not exists " + DbManager.mSchedule + " ("
				+ DbManager.colID + " varchar, "
				+ DbManager.colUserId + " varchar, "
				+ DbManager.colSiteId + " integer, "
				+ DbManager.colOperatorIds + " varchar, "
				+ DbManager.colProjectId + " varchar, "
				+ DbManager.colProjectName + " varchar, "
				+ DbManager.colWorkTypeId + " integer, "
				+ DbManager.colDayDate + " varchar, "
				+ DbManager.colWorkDate + " varchar, "
				+ DbManager.colWorkDateStr + " varchar, "
				+ DbManager.colProgress + " varchar, "
				+ DbManager.colStatus + " varchar, "
				+ DbManager.colSumTask + " integer, "
				+ DbManager.colSumDone + " integer, "
				+ DbManager.colOperatorNumber + " integer, ");

		if (DbManager.schema_version >= 10) {
			// SAP request add hidden items flag
			if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP))
				createTableBuilder.append(DbManager.colHiddenItemIds + " varchar, ");
		}

		if (DbManager.schema_version >= 12) {
			createTableBuilder.append(DbManager.colRejection + " varchar, ");
		}

		if (DbManager.schema_version >= 13) {
			if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP))
				createTableBuilder.append(DbManager.colTTNumber + " varchar, ");
		}
		createTableBuilder.append("PRIMARY KEY (" + DbManager.colID + "))");
		return new String(createTableBuilder);
	}

	public String getPercent(){
		/*DebugLog.d(sumTaskDone+":"+sumTask);
		if (sumTask > 0) {
			int value = 100 * sumTaskDone / sumTask;
			if (value >= 100) {
				return 100 + "%";
			} else if (value<0){
				return "0%";
			} else {
				return value + "%";
			}
		} else {
			return "0%";
		}*/

		int taskDone = FormValueModel.countTaskDone(id);
		String percentage = "0%";
		if (sumTask > 0) {
			int value = 100 * taskDone / sumTask;
			if (value >= 100) {
				percentage =  100 + "%";
			} else if (value<0){
				percentage =  "0%";
			} else {
				percentage = value + "%";
			}
		} else {
			percentage = "0%";
		}
		DebugLog.d("task done: " + taskDone);
		DebugLog.d("sum task: " + sumTask);
		DebugLog.d("percentage: " + percentage);
		return percentage;
	}

	public abstract String getTitle();
	public abstract String getStatus();
	public abstract String getTask();
	public abstract String getPlace();
	public abstract String getPercentColor();
	public abstract String getTaskColor();

	public int saveCorrective(){
		int count = 0;
		if (operators==null)
			return count;

		count = saveCorrectivePerOperatorItem(count);
		count = saveCorrectiveAllOperatorItem(count);

		return count;
	}

	private int saveCorrectiveAllOperatorItem(int count){
		if (general_corrective_item_ids != null)
			for (int itemId : general_corrective_item_ids) {
				for (OperatorModel operator : operators) {
					insertCorrectiveToDB(operator.id, itemId);
					count += 2;
				}
			}
		return count;
	}

	private int saveCorrectivePerOperatorItem(int count){
		//save corrective per operator items
		for (OperatorModel operator : operators){
			if (operator.corrective_item_ids == null)
				continue;
			CorrectiveValueModel model = new CorrectiveValueModel();
			for(Integer correctiveId : operator.corrective_item_ids){
				insertCorrectiveToDB(operator.id, correctiveId);
				count += 2;
			}
		}
		return count;
	}

	private void insertCorrectiveToDB(int operatorId, int itemId){
		CorrectiveValueModel model = new CorrectiveValueModel();
		if (CorrectiveValueModel.getItemValue(this.id, itemId, operatorId) == null){
			model.itemId = itemId;
			model.scheduleId = this.id;
			model.operatorId = operatorId;
			model.photoStatus = "AFTER";
			model.siteId = this.site.id;
			model.insert();

			model.photoStatus = "BEFORE";
			model.insert();
		}
	}

	public void save(){
		for (OperatorModel operator : operators) {
			operator.save();
		}

		if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {
			if (hidden != null)
				for (int hiddenItemIds : hidden) {
					WorkFormItemModel workFormItem = WorkFormItemModel.getWorkFormItemById(hiddenItemIds);
					workFormItem.visible = false;
					workFormItem.save();
				}
		}

		if (schedule_values!=null)
			for (FormValueModel formValueModel : schedule_values) {
				if (downloadImage(formValueModel.picture, formValueModel.value))
					formValueModel.save();
			}

		//check if any corective task
		int tempTask = saveCorrective();
		work_type.save();
		site.save();

		if (tempTask > 0)
			sumTask = tempTask;
		else{
			//tempTask = WorkFormModel.getTaskCount(work_type.id) * operators.size();
			tempTask = WorkFormModel.getTaskCount(work_type.id); // count sum task based on amount of items
			if (tempTask <= 0 ){
				tempTask = CorrectiveValueModel.countTaskDone(id);
			}
			sumTask = tempTask <= 0 ? -1 : tempTask;
		}

		insert();
	}

	private void insert() {

		String sql;
		String format;
		StringBuilder formatBuilder = new StringBuilder("INSERT OR REPLACE INTO %s(");
		StringBuilder valuesBuilder = new StringBuilder(" VALUES(");
		ArrayList<String> argsList = new ArrayList<>();
		argsList.add(DbManager.mSchedule);
		argsList.add(DbManager.colID);
		argsList.add(DbManager.colUserId);
		argsList.add(DbManager.colSiteId);
		argsList.add(DbManager.colOperatorIds);
		argsList.add(DbManager.colProjectId);
		argsList.add(DbManager.colProjectName);
		argsList.add(DbManager.colWorkTypeId);
		argsList.add(DbManager.colWorkDate);
		argsList.add(DbManager.colProgress);
		argsList.add(DbManager.colStatus);
		argsList.add(DbManager.colDayDate);
		argsList.add(DbManager.colWorkDateStr);
		argsList.add(DbManager.colSumTask);
		argsList.add(DbManager.colSumDone);
		argsList.add(DbManager.colOperatorNumber);

		if (DbManager.schema_version >= 10) {
			if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {
				argsList.add(DbManager.colHiddenItemIds);
			}
		}

		if (DbManager.schema_version >= 12) {
			argsList.add(DbManager.colRejection);
		}

		if (DbManager.schema_version >= 13) {
			if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP))
				argsList.add(DbManager.colTTNumber);
		}

		String[] args = new String[argsList.size()];
		args = argsList.toArray(args);

		int size = argsList.size() - 1;
		for (int i = 0; i < size; i++) {
			formatBuilder.append("%s");
			valuesBuilder.append("?");
			if (i < size - 1) {
				formatBuilder.append(",");
				valuesBuilder.append(",");
			}
		}
		formatBuilder.append(")");
		valuesBuilder.append(")");

		format = new String(formatBuilder) + new String(valuesBuilder);
		sql = String.format(format, (Object[]) args);

		DbRepository.getInstance().open(TowerApplication.getInstance());
		SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);

		bindAndCheckNullString(stmt, getColIndex(argsList, DbManager.colID), id);
		if (user == null)
			bindAndCheckNullString(stmt, getColIndex(argsList, DbManager.colUserId), null);
		else
			bindAndCheckNullString(stmt, getColIndex(argsList, DbManager.colUserId), user.id);

		if (site == null){
			stmt.bindLong(getColIndex(argsList, DbManager.colSiteId), -1);
		}else{
			stmt.bindLong(getColIndex(argsList, DbManager.colSiteId), site.id);
		}

		if (operators == null || operators.size() < 1){
			bindAndCheckNullString(stmt, getColIndex(argsList, DbManager.colOperatorIds), null);
		}
		else{
			bindAndCheckNullString(stmt, getColIndex(argsList, DbManager.colOperatorIds), getOperatorIds(operators));
		}

		if (project == null){
			bindAndCheckNullString(stmt, getColIndex(argsList, DbManager.colProjectId), null);
			bindAndCheckNullString(stmt, getColIndex(argsList, DbManager.colProjectName), null);
		}else{
			bindAndCheckNullString(stmt, getColIndex(argsList, DbManager.colProjectId), project.id);
			bindAndCheckNullString(stmt, getColIndex(argsList, DbManager.colProjectName), project.name);
		}
		if (work_type == null){
			stmt.bindLong(getColIndex(argsList, DbManager.colWorkTypeId), -1);
		}else{
			stmt.bindLong(getColIndex(argsList, DbManager.colWorkTypeId), work_type.id);
		}
		bindAndCheckNullString(stmt, getColIndex(argsList, DbManager.colWorkDate), work_date);
		stmt.bindLong(getColIndex(argsList, DbManager.colProgress), (long) progress);
		bindAndCheckNullString(stmt, getColIndex(argsList, DbManager.colStatus), status);
		bindAndCheckNullString(stmt, getColIndex(argsList, DbManager.colDayDate), work_date_str.substring(0, 10));
		bindAndCheckNullString(stmt, getColIndex(argsList, DbManager.colWorkDateStr), work_date_str);
		stmt.bindLong(getColIndex(argsList, DbManager.colSumTask), sumTask);
		stmt.bindLong(getColIndex(argsList, DbManager.colSumDone), sumTaskDone);
		stmt.bindLong(getColIndex(argsList, DbManager.colOperatorNumber), operator_number);

		if (DbManager.schema_version >= 10) {
			if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {
				bindAndCheckNullString(stmt, getColIndex(argsList, DbManager.colHiddenItemIds), toStringHiddenItemIds(hidden));
			}
		}

		if (DbManager.schema_version >= 12) {
			String rejectionJson = null;
			if (rejection != null) {
				rejectionJson =  new Gson().toJson(rejection);
			}
			bindAndCheckNullString(stmt, getColIndex(argsList, DbManager.colRejection), rejectionJson);
		}

		if (DbManager.schema_version >= 13) {
			if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP))
				bindAndCheckNullString(stmt, getColIndex(argsList, DbManager.colTTNumber), tt_number);
		}

		stmt.executeInsert();
		stmt.close();
		DbRepository.getInstance().close();

	}

	public static void delete(){
		DbRepository.getInstance().open(TowerApplication.getInstance());
		String sql = "DELETE FROM " + DbManager.mSchedule;
		SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);
		stmt.executeUpdateDelete();
		stmt.close();
		DbRepository.getInstance().close();
	}

	public static void deleteAllBy(String scheduleId) {
		DbRepository.getInstance().open(TowerApplication.getInstance());
		String sql = "DELETE FROM " + DbManager.mSchedule + " WHERE " + DbManager.colID + " = '" + scheduleId + "'";
		SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);
		stmt.executeUpdateDelete();
		stmt.close();
		DbRepository.getInstance().close();
	}

	public static int getTaskDone(String scheduleId){
		int result = 0;
		String table = DbManager.mSchedule;
		String[] columns = new String[] {DbManager.colSumDone};
		String where =DbManager.colID + "=?";
		String[] args = new String[] {String.valueOf(scheduleId)};
		String order = null;

		DbRepository.getInstance().open(TowerApplication.getInstance());
		Cursor cursor = DbRepository.getInstance().getDB().query(table, columns, where, args, null, null, order, null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			return result;
		}
		result = cursor.getInt(cursor.getColumnIndex(DbManager.colSumDone));

		cursor.close();
		DbRepository.getInstance().close();
		return result;
	}

	public static ArrayList<ScheduleGeneral> getAllSchedule() {

		ArrayList<ScheduleGeneral> result = new ArrayList<ScheduleGeneral>();

		String table = DbManager.mSchedule;
		String[] columns = null;
		String where =null;
		String[] args = null;
		Cursor cursor;

		DbRepository.getInstance().open(TowerApplication.getInstance());
		cursor = DbRepository.getInstance().getDB().query(true, table, columns, where, args, null, null, DbManager.colWorkDate+" DESC", null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			DbRepository.getInstance().close();
			return result;
		}
		do {
			result.add(getScheduleFromCursor(cursor,false));
		} while(cursor.moveToNext());

		cursor.close();
		DbRepository.getInstance().close();

		return result;
	}

	public static ArrayList<ScheduleGeneral> getScheduleByWorktype(String workType) {
		ArrayList<ScheduleGeneral> result = new ArrayList<>();
		String table = DbManager.mSchedule;
		String[] columns = null;
		String where = DbManager.colWorkTypeName+"= UPPER(?)";
		//		String[] args = new String[]{workType};
		Cursor cursor;

		String query = "SELECT t1.id as sched_id,* FROM " + DbManager.mSchedule + " t1 INNER JOIN " + DbManager.mWorkType + " t2 ON t1." + DbManager.colWorkTypeId + "=t2." + DbManager.colID + " WHERE t2." + DbManager.colName + " LIKE '%" + workType.toUpperCase() + "%' ORDER BY t1." + DbManager.colWorkDate + " DESC";
		//		String[] args = new String[]{ "'%" + workType.toUpperCase() + "%'"};

		DebugLog.d(query);

		DbRepository.getInstance().open(TowerApplication.getInstance());
		//		cursor = DbRepository.getInstance().getDB().query(true, table, columns, where, args, null, null, DbManager.colWorkDate+" ASC", null);
		cursor = DbRepository.getInstance().getDB().rawQuery(query, null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			DbRepository.getInstance().close();
			return result;
		}
		do {
			result.add(getScheduleFromCursor(cursor,true));
		} while(cursor.moveToNext());

		cursor.close();
		DbRepository.getInstance().close();
		return result;
	}

	public static Observable<ArrayList<ScheduleGeneral>> loadSchedules(String workType){
		if (!TextUtils.isEmpty(workType)) return Observable.fromArray(getScheduleByWorktype(workType));
		else return Observable.fromArray(getAllSchedule());
	}

	public static Observable<ArrayList<ScheduleGeneral>> loadScheduleList(ArrayList<ScheduleGeneral> rawList) {
		return Observable.fromArray(getListScheduleForScheduleAdapter(rawList));
	}

	public static ScheduleGeneral getScheduleById(String id) {

		ScheduleGeneral model = null;

		String table = DbManager.mSchedule;
		String[] columns = null;
		String where =DbManager.colID+"= UPPER(?)";
		String[] args = new String[]{id};
		Cursor cursor;

		DbRepository.getInstance().open(TowerApplication.getInstance());
		cursor = DbRepository.getInstance().getDB().query(true, table, columns, where, args, null, null, DbManager.colWorkDate+" DESC", null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			DbRepository.getInstance().close();
			return model;
		}

		model = getScheduleFromCursor(cursor,false);
		cursor.close();
		DbRepository.getInstance().close();
		return model;
	}

	public static ArrayList<ScheduleGeneral> getListScheduleForScheduleAdapter(ArrayList<ScheduleGeneral> rawList) {
		ArrayList<ScheduleGeneral> listPerDay = new ArrayList<>();
		for (ScheduleGeneral schedule : rawList) {
			int listPerDaySize = listPerDay.size();
			DebugLog.d("schedule.day_date : " + schedule.day_date);
			if (listPerDaySize == 0 || !schedule.day_date.equalsIgnoreCase(listPerDay.get(listPerDaySize-1).day_date)){
				if (listPerDaySize >0 )
					DebugLog.d("listPerDay.get(listPerDay.size()-1).day_date : " + listPerDay.get(listPerDaySize-1).day_date);
				ScheduleGeneral scheduleBaseModel2 = new ScheduleGeneral();
				scheduleBaseModel2.isSeparator = true;
				scheduleBaseModel2.day_date = schedule.day_date;
				listPerDay.add(scheduleBaseModel2);
			}
			listPerDay.add(schedule);
		}
		return listPerDay;
	}

	public static LinkedHashMap<String, ArrayList<CallendarModel>> getListScheduleForCallendarAdapter(ArrayList<ScheduleGeneral> rawList) {
		LinkedHashMap<String, ArrayList<CallendarModel>> filter = new LinkedHashMap<String, ArrayList<CallendarModel>>();
		ArrayList<CallendarModel> callendarModels = null;
		CallendarModel callendarModel = null;
		int count = 0;
		for (ScheduleGeneral scheduleBaseModel : rawList) {
			if (callendarModels == null
					|| !scheduleBaseModel.day_date.substring(0, 7).equalsIgnoreCase(callendarModels.get(callendarModels.size()-1).date.substring(0, 7))){
				callendarModels = new ArrayList<CallendarModel>();
				filter.put(scheduleBaseModel.day_date.substring(0, 7), callendarModels);
			}
			if (callendarModel == null || !scheduleBaseModel.day_date.equalsIgnoreCase(callendarModel.date)){
				callendarModel = new CallendarModel();
				callendarModel.sum = 0;
				callendarModel.date = scheduleBaseModel.day_date;
				callendarModels.add(callendarModel);
			}
			if (callendarModel != null)
				callendarModel.sum ++;
		}
		return filter;
	}

	private static ScheduleGeneral getScheduleFromCursor(Cursor c, boolean fromInnerJoin) {
		ScheduleGeneral schedule = new ScheduleGeneral();

		if (null == c)
			return schedule;

		if (fromInnerJoin)
			schedule.id = (c.getString(c.getColumnIndex("sched_id")));
		else
			schedule.id = (c.getString(c.getColumnIndex(DbManager.colID)));
		schedule.day_date = (c.getString(c.getColumnIndex(DbManager.colDayDate)));
		schedule.work_date = (c.getString(c.getColumnIndex(DbManager.colWorkDate)));
		schedule.work_date_str = (c.getString(c.getColumnIndex(DbManager.colWorkDateStr)));
		schedule.progress = (int) (c.getLong(c.getColumnIndex(DbManager.colProgress)));
		schedule.sumTask = (int) (c.getLong(c.getColumnIndex(DbManager.colSumTask)));
		schedule.sumTaskDone = (int) (c.getLong(c.getColumnIndex(DbManager.colSumDone)));
		schedule.status = (c.getString(c.getColumnIndex(DbManager.colStatus)));
		schedule.operator_number = (int) (c.getLong(c.getColumnIndex(DbManager.colOperatorNumber)));

		//user
		schedule.user = new UserModel();
		schedule.user.id = (c.getString(c.getColumnIndex(DbManager.colUserId)));

		//site
		schedule.site = new SiteModel();
		schedule.site.id = (c.getInt(c.getColumnIndex(DbManager.colSiteId)));
		schedule.site = schedule.site.getSiteById(schedule.site.id);

		OperatorModel tempOp;

		String temp = c.getString(c.getColumnIndex(DbManager.colOperatorIds));
		schedule.operators = new ArrayList<OperatorModel>();
		if (temp != null){
			String[] tempOpId = temp.split("[,]");
			for (int i = 0; i < tempOpId.length; i++) {
				tempOp = new OperatorModel();
				tempOp  = OperatorModel.getOperatorById(Integer.parseInt(tempOpId[i]));
				schedule.operators.add(tempOp);
			}
		}

		//project
		schedule.project = new ProjectModel();
		schedule.project.id = (c.getString(c.getColumnIndex(DbManager.colProjectId)));
		schedule.project.name = (c.getString(c.getColumnIndex(DbManager.colProjectName)));

		//worktype
		schedule.work_type = new WorkTypeModel();
		schedule.work_type.id = (c.getInt(c.getColumnIndex(DbManager.colWorkTypeId)));
		schedule.work_type = schedule.work_type.getworkTypeById(schedule.work_type.id);

		// hidden item ids
		if (DbManager.schema_version >= 10) {
			if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {
				schedule.hidden = toVectorHiddenItemIds(c.getString(c.getColumnIndex(DbManager.colHiddenItemIds)));
			}
		}

		if (DbManager.schema_version >= 12) {
			schedule.rejection = new Gson().fromJson(c.getString(c.getColumnIndex(DbManager.colRejection)), RejectionModel.class);
		}

		if (DbManager.schema_version >= 13) {
			if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP))
				schedule.tt_number = c.getString(c.getColumnIndex(DbManager.colTTNumber));
		}
		return schedule;
	}

	public static void resetAllSchedule(){
		DbRepository.getInstance().open(TowerApplication.getInstance());
		ContentValues cv = new ContentValues();
		cv.put(DbManager.colProgress, -1);
		cv.put(DbManager.colSumDone, 0);
		DbRepository.getInstance().getDB().update(DbManager.mSchedule, cv, null, null);
		DbRepository.getInstance().close();
	}


	/**
	 * Schedule utils
	 *
	 * */
	private boolean downloadImage(String url, String path) {
		if (url==null) return true;
		else {
			DebugLog.d("downloading "+url);
			Bitmap bitmap = ImageLoader.getInstance().loadImageSync(url);
			File file = new File(path.replaceFirst("^file\\:\\/\\/", ""));
			File dir = file.getParentFile();
			try {
				if (!dir.mkdirs() && (!dir.exists() || !dir.isDirectory())) {
					return false;
				}
				return ImageUtil.resizeAndSaveImage2(bitmap,file);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
	}

	private void printDefaultValueSchedules() {
		if (default_value_schedule != null && !default_value_schedule.isEmpty()) {
			int i = 1;
			for (DefaultValueScheduleModel itemDefaultValue : default_value_schedule) {

				DebugLog.d("index ke-" + i);
				DebugLog.d("-- item_id  : " + itemDefaultValue.getItem_id());
				DebugLog.d("-- group_id : " + itemDefaultValue.getGroup_id());
				DebugLog.d("-- form_id  : " + itemDefaultValue.getForm_id());
				DebugLog.d("-- default_value : " + itemDefaultValue.getDefault_value());
				i++;
			}
		}
	}


	private static String getOperatorIds(ArrayList<OperatorModel> operators){
		String s = "";
		for (int i = 0; i < operators.size(); i++)
			s += i == operators.size() - 1 ? operators.get(i).id : operators.get(i).id+",";
		return s;
	}

	private static String toStringHiddenItemIds(ArrayList<Integer> hidden) {
		if (hidden == null) return null;

		StringBuilder hiddenItems = new StringBuilder();
		for (int i = 0; i < hidden.size(); i++) {
			hiddenItems.append( i == hidden.size()-1 ? hidden.get(i) : hidden.get(i) + ",");
		}
		return new String(hiddenItems);
	}

	private static ArrayList<Integer> toVectorHiddenItemIds(String hiddenItems) {
		ArrayList<Integer> hidden = new ArrayList<>();
		if (!TextUtils.isEmpty(hiddenItems)) {
			String[] hiddenArray = hiddenItems.split(",");
			for (String hiddenId : hiddenArray) {
				hidden.add(Integer.valueOf(hiddenId));
			}
		}
		return hidden;
	}

	private static int getColIndex(ArrayList<String> cols, String colKeyword) {
		return cols.indexOf(colKeyword);
	}
}