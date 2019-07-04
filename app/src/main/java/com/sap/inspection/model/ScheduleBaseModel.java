package com.sap.inspection.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.text.TextUtils;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sap.inspection.MyApplication;
import com.sap.inspection.model.form.WorkFormItemModel;
import com.sap.inspection.model.form.WorkFormModel;
import com.sap.inspection.model.value.CorrectiveValueModel;
import com.sap.inspection.model.value.FormValueModel;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.ImageUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Vector;

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
	public Vector<OperatorModel> operators;
	public ArrayList<Integer> general_corrective_item_ids;
	public UserModel user;
	public WorkTypeModel work_type;
	public Vector<Integer> hidden;

	//penambahan irwan
	public WorkFormModel work_form;
	public ProjectModel project;
	public Vector<FormValueModel> schedule_values;
	public Vector<DefaultValueScheduleModel> default_value_schedule = new Vector<>();
	public String statusColor;
	public String taskColor;
	public int sumTask = 0;
	public int sumTaskDone = 0;
	public boolean isSeparator = false;
	public boolean isAnimated = false;
	public int operator_number = 0;
	private int task = -1;


	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {

	}

	public static String createDB(){
		switch (DbManager.schema_version) {
			case 9: {
				return "create table if not exists " + DbManager.mSchedule
						+ " (" + DbManager.colID + " varchar, "
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
						+ DbManager.colOperatorNumber + " integer, "
						+ "PRIMARY KEY (" + DbManager.colID + "))";
			}
			case 10 :
				return "create table if not exists " + DbManager.mSchedule
						+ " (" + DbManager.colID + " varchar, "
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
						+ DbManager.colOperatorNumber + " integer, "
						+ DbManager.colHiddenItemIds + " varchar, "
						+ "PRIMARY KEY (" + DbManager.colID + "))";
			default: return null;
		}
	}

	public String getPercent(){
		DebugLog.d(sumTaskDone+":"+sumTask);
		if (sumTask > 0) {
			int value = 100 * sumTaskDone / sumTask;
			if (value >= 100) {
				return 100 + "%";
			} else if (value<0){
				return "0%";
			} else {
				return value + "%";
			}
//			return 100 * sumTaskDone / sumTask >= 100 ? 100 + "%" : (100 * sumTaskDone / sumTask) + "%";
		}else {
			return "0%";
		}
	}

	public abstract String getTitle();
	public abstract String getStatus();
	public abstract String getTask();
	public abstract String getPlace();
	public abstract String getPercentColor();
	public abstract String getTaskColor();

	public void save(Context context){
		//DbRepository.getInstance().open(context);
		save();
		//DbRepository.getInstance().close();
	}

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

		for (int hiddenItemIds : hidden) {
			WorkFormItemModel workFormItem = WorkFormItemModel.getItemById(hiddenItemIds);
			workFormItem.visible = false;
			workFormItem.save();
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
			task = tempTask;
		else{
			tempTask = WorkFormModel.getTaskCount(work_type.id) * operators.size();
			if (tempTask <= 0 ){
				tempTask = CorrectiveValueModel.countTaskDone(id);
			}
			task = tempTask <= 0 ? -1 : tempTask;
		}

		if (sumTaskDone == 0){
			sumTaskDone = getTaskDone(id);
		}

		insert();
	}

	private void insert() {

		String sql = String.format("INSERT OR REPLACE INTO %s(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
				DbManager.mSchedule , DbManager.colID,
				DbManager.colUserId,DbManager.colSiteId,
				DbManager.colOperatorIds,DbManager.colProjectId,
				DbManager.colProjectName,DbManager.colWorkTypeId,
				DbManager.colWorkDate,DbManager.colProgress,
				DbManager.colStatus,DbManager.colDayDate,
				DbManager.colWorkDateStr,DbManager.colSumTask,
				DbManager.colSumDone, DbManager.colOperatorNumber);
		//						DbManager.colWorkDateStr,DbManager.colSumTask,
		//						DbManager.colSumTask,DbManager.mSchedule,DbManager.colID);

		if (DbManager.schema_version >= 10) {
			sql = String.format("INSERT OR REPLACE INTO %s(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
					DbManager.mSchedule , DbManager.colID,
					DbManager.colUserId,DbManager.colSiteId,
					DbManager.colOperatorIds,DbManager.colProjectId,
					DbManager.colProjectName,DbManager.colWorkTypeId,
					DbManager.colWorkDate,DbManager.colProgress,
					DbManager.colStatus,DbManager.colDayDate,
					DbManager.colWorkDateStr,DbManager.colSumTask,
					DbManager.colSumDone, DbManager.colOperatorNumber,
					DbManager.colHiddenItemIds);
		}

		DbRepository.getInstance().open(MyApplication.getInstance());

		SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);

		bindAndCheckNullString(stmt, 1, id);
		if (user == null)
			bindAndCheckNullString(stmt, 2, null);
		else
			bindAndCheckNullString(stmt, 2, user.id);

		if (site == null){
			stmt.bindLong(3, -1);
		}else{
			stmt.bindLong(3, site.id);
		}

		if (operators == null || operators.size() < 1){
			bindAndCheckNullString(stmt, 4, null);
		}
		else{
			bindAndCheckNullString(stmt, 4, getOperatorIds(operators));
		}

		if (project == null){
			bindAndCheckNullString(stmt, 5, null);
			bindAndCheckNullString(stmt, 6, null);
		}else{
			bindAndCheckNullString(stmt, 5, project.id);
			bindAndCheckNullString(stmt, 6, project.name);
		}
		if (work_type == null){
			stmt.bindLong(7, -1);
		}else{
			stmt.bindLong(7, work_type.id);
		}
		bindAndCheckNullString(stmt, 8, work_date);
		stmt.bindLong(9, (long) progress);
		//		bindAndCheckNullString(stmt, 13, progress);
		bindAndCheckNullString(stmt, 10, status);
		//		bindAndCheckNullString(stmt, 11, day_date == null ? work_date_str.substring(0, 10) : day_date);
		bindAndCheckNullString(stmt, 11, work_date_str.substring(0, 10));
		bindAndCheckNullString(stmt, 12, work_date_str);
		//		bindAndCheckNullString(stmt, 13, id);
		stmt.bindLong(13, task);
		stmt.bindLong(14, sumTaskDone);
		stmt.bindLong(15, operator_number);

		if (DbManager.schema_version == 10) {
			if (hidden == null || hidden.size() < 1) {
				bindAndCheckNullString(stmt, 16, null);
			} else {
				bindAndCheckNullString(stmt, 16, toStringHiddenItemIds(hidden));
			}
		}

		stmt.executeInsert();
		stmt.close();
		DbRepository.getInstance().close();

	}

	public static void delete(){

		DbRepository.getInstance().open(MyApplication.getInstance());
		String sql = "DELETE FROM " + DbManager.mSchedule;
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

		DbRepository.getInstance().open(MyApplication.getInstance());
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

	public Vector<ScheduleBaseModel> getAllSchedule(Context context) {

		Vector<ScheduleBaseModel> result = new Vector<ScheduleBaseModel>();

		String table = DbManager.mSchedule;
		String[] columns = null;
		String where =null;
		String[] args = null;
		Cursor cursor;

		DbRepository.getInstance().open(MyApplication.getInstance());
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

	public Vector<ScheduleBaseModel> getScheduleByWorktype(Context context,String workType) {

		Vector<ScheduleBaseModel> result = new Vector<ScheduleBaseModel>();

		String table = DbManager.mSchedule;
		String[] columns = null;
		String where = DbManager.colWorkTypeName+"= UPPER(?)";
		//		String[] args = new String[]{workType};
		Cursor cursor;

		String query = "SELECT t1.id as sched_id,* FROM " + DbManager.mSchedule + " t1 INNER JOIN " + DbManager.mWorkType + " t2 ON t1." + DbManager.colWorkTypeId + "=t2." + DbManager.colID + " WHERE t2." + DbManager.colName + " LIKE '%" + workType.toUpperCase() + "%' ORDER BY t1." + DbManager.colWorkDate + " DESC";
		//		String[] args = new String[]{ "'%" + workType.toUpperCase() + "%'"};

		DebugLog.d(query);

		DbRepository.getInstance().open(MyApplication.getInstance());
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

	public ScheduleBaseModel getScheduleById(Context context,String id) {

		ScheduleBaseModel model = getScheduleById(id);

		return model;
	}

	public ScheduleBaseModel getScheduleById(String id) {

		ScheduleBaseModel model = null;

		String table = DbManager.mSchedule;
		String[] columns = null;
		String where =DbManager.colID+"= UPPER(?)";
		String[] args = new String[]{id};
		Cursor cursor;

		DbRepository.getInstance().open(MyApplication.getInstance());
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

	//penambahan irwan method getScheduleByForm
//	public ScheduleBaseModel getScheduleByForm(String id){
//
//		ScheduleBaseModel model = null;
//
//		String table = DbManager.mWorkForm;
//		String[] columns = null;
//		String where = DbManager.mWorkType;
//
//		return model;
//	}


	public Vector<ScheduleBaseModel> getListScheduleForScheduleAdapter(Vector<ScheduleBaseModel> rawList) {
		Vector<ScheduleBaseModel> listPerDay = new Vector<>();

		for (ScheduleBaseModel scheduleBaseModel : rawList) {
			DebugLog.d("listPerDay.size() : " + listPerDay.size());
			DebugLog.d("scheduleBaseModel.day_date : " + scheduleBaseModel.day_date);
			if (listPerDay.size() == 0 || !scheduleBaseModel.day_date.equalsIgnoreCase(listPerDay.get(listPerDay.size()-1).day_date)){
				if (listPerDay.size() >0 )
					DebugLog.d("listPerDay.get(listPerDay.size()-1).day_date : " + listPerDay.get(listPerDay.size()-1).day_date);
				ScheduleBaseModel scheduleBaseModel2 = newObject();
				scheduleBaseModel2.isSeparator = true;
				//				scheduleBaseModel2.work_date = scheduleBaseModel.work_date;
				scheduleBaseModel2.day_date = scheduleBaseModel.day_date;
				listPerDay.add(scheduleBaseModel2);
			}
			listPerDay.add(scheduleBaseModel);
		}
		return listPerDay;
	}

	public LinkedHashMap<String, Vector<CallendarModel>> getListScheduleForCallendarAdapter(Vector<ScheduleBaseModel> rawList) {
		LinkedHashMap<String, Vector<CallendarModel>> filter = new LinkedHashMap<String, Vector<CallendarModel>>();
		Vector<CallendarModel> callendarModels = null;
		CallendarModel callendarModel = null;
		int count = 0;
		for (ScheduleBaseModel scheduleBaseModel : rawList) {
			if (callendarModels == null 
					|| !scheduleBaseModel.day_date.substring(0, 7).equalsIgnoreCase(callendarModels.get(callendarModels.size()-1).date.substring(0, 7))){
				callendarModels = new Vector<CallendarModel>();
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

	protected abstract ScheduleBaseModel newObject();

	protected ScheduleBaseModel getScheduleFromCursor(Cursor c, boolean fromInnerJoin) {
		ScheduleBaseModel scheduleBase = newObject();

		if (null == c)
			return scheduleBase;

		if (fromInnerJoin)
			scheduleBase.id = (c.getString(c.getColumnIndex("sched_id")));
		else
			scheduleBase.id = (c.getString(c.getColumnIndex(DbManager.colID)));
		scheduleBase.day_date = (c.getString(c.getColumnIndex(DbManager.colDayDate)));
		scheduleBase.work_date = (c.getString(c.getColumnIndex(DbManager.colWorkDate)));
		scheduleBase.work_date_str = (c.getString(c.getColumnIndex(DbManager.colWorkDateStr)));
		scheduleBase.progress = (int) (c.getLong(c.getColumnIndex(DbManager.colProgress)));
		scheduleBase.sumTask = (int) (c.getLong(c.getColumnIndex(DbManager.colSumTask)));
		scheduleBase.sumTaskDone = (int) (c.getLong(c.getColumnIndex(DbManager.colSumDone)));
		scheduleBase.status = (c.getString(c.getColumnIndex(DbManager.colStatus)));
		scheduleBase.operator_number = (int) (c.getLong(c.getColumnIndex(DbManager.colOperatorNumber)));

		//user
		scheduleBase.user = new UserModel();
		scheduleBase.user.id = (c.getString(c.getColumnIndex(DbManager.colUserId)));

		//site
		scheduleBase.site = new SiteModel();
		scheduleBase.site.id = (c.getInt(c.getColumnIndex(DbManager.colSiteId)));
		scheduleBase.site = scheduleBase.site.getSiteById(scheduleBase.site.id);

		OperatorModel tempOp;

		String temp = c.getString(c.getColumnIndex(DbManager.colOperatorIds));
		scheduleBase.operators = new Vector<OperatorModel>();
		if (temp != null){
			String[] tempOpId = temp.split("[,]");
			for (int i = 0; i < tempOpId.length; i++) {
				tempOp = new OperatorModel();
				tempOp  = tempOp.getOperatorById(Integer.parseInt(tempOpId[i]));
				scheduleBase.operators.add(tempOp);
			}
		}

		//project
		scheduleBase.project = new ProjectModel();
		scheduleBase.project.id = (c.getString(c.getColumnIndex(DbManager.colProjectId)));
		scheduleBase.project.name = (c.getString(c.getColumnIndex(DbManager.colProjectName)));

		//worktype
		scheduleBase.work_type = new WorkTypeModel();
		scheduleBase.work_type.id = (c.getInt(c.getColumnIndex(DbManager.colWorkTypeId)));
		scheduleBase.work_type = scheduleBase.work_type.getworkTypeById(scheduleBase.work_type.id);

		// hidden item ids
		if (DbManager.schema_version >= 10) {
			scheduleBase.hidden = new Vector<>();
			scheduleBase.hidden = toVectorHiddenItemIds(c.getString(c.getColumnIndex(DbManager.colHiddenItemIds)));
		}
		return scheduleBase;
	}

	public static void resetAllSchedule(){

		DbRepository.getInstance().open(MyApplication.getInstance());
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


	private static String getOperatorIds(Vector<OperatorModel> operators){
		String s = "";
		for (int i = 0; i < operators.size(); i++)
			s += i == operators.size() - 1 ? operators.get(i).id : operators.get(i).id+",";
		return s;
	}

	private static String toStringHiddenItemIds(Vector<Integer> hidden) {
		StringBuilder hiddenItems = new StringBuilder();
		for (int i = 0; i < hidden.size(); i++) {
			hiddenItems.append( i == hidden.size()-1 ? hidden.get(i) : hidden.get(i) + ",");
		}
		return new String(hiddenItems);
	}

	private static Vector<Integer> toVectorHiddenItemIds(String hiddenItems) {
		Vector<Integer> hidden = new Vector<>();
		if (!TextUtils.isEmpty(hiddenItems)) {
			String[] hiddenArray = hiddenItems.split(",");
			for (String hiddenId : hiddenArray) {
				hidden.add(Integer.valueOf(hiddenId));
			}
		}
		return hidden;
	}
}