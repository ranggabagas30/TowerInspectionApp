package com.sap.inspection.model.value;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Parcel;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.sap.inspection.BuildConfig;
import com.sap.inspection.TowerApplication;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.event.UploadProgressEvent;
import com.sap.inspection.manager.ItemUploadManager;
import com.sap.inspection.model.OperatorModel;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.ScheduleGeneral;
import com.sap.inspection.model.form.WorkFormGroupModel;
import com.sap.inspection.model.form.WorkFormItemModel;
import com.sap.inspection.model.form.WorkFormModel;
import com.sap.inspection.tools.DebugLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import de.greenrobot.event.EventBus;


public class CorrectiveValueModel extends FormValueModel {

	private static final int ALL_OP_ID = -1;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
	}

	public static void delete(Context ctx, String scheduleId, int itemId, int operatorId){

		delete(scheduleId, itemId, operatorId);

	}

	public static void delete(String scheduleId, int itemId, int operatorId){

		DbRepositoryValue.getInstance().open(TowerApplication.getInstance());
		String sql = "DELETE FROM " + DbManagerValue.mCorrectiveValue + " WHERE "+DbManagerValue.colScheduleId+"="+scheduleId+" AND "+DbManagerValue.colItemId+"="+itemId+" AND "+DbManagerValue.colOperatorId+"="+operatorId;
		SQLiteStatement stmt = DbRepositoryValue.getInstance().getDB().compileStatement(sql);
		stmt.executeUpdateDelete();
		stmt.close();
		DbRepositoryValue.getInstance().close();
	}
	
	public static void deleteAll(Context ctx){

		DbRepositoryValue.getInstance().open(TowerApplication.getInstance());
		String sql = "DELETE FROM " + DbManagerValue.mCorrectiveValue;
		SQLiteStatement stmt = DbRepositoryValue.getInstance().getDB().compileStatement(sql);
		stmt.executeUpdateDelete();
		stmt.close();
		DbRepositoryValue.getInstance().close();
	}

	public static CorrectiveValueModel getItemValue(Context context,String scheduleId, int itemId, int operatorId) {
		CorrectiveValueModel model = getItemValue(scheduleId, itemId, operatorId);
		return model;
	}

	public static int countTaskDone(String scheduleId){
		DbRepositoryValue.getInstance().open(TowerApplication.getInstance());
		Cursor mCount= DbRepositoryValue.getInstance().getDB().rawQuery("select count(*) from "+DbManagerValue.mCorrectiveValue+" where "+DbManagerValue.colScheduleId+"='" + scheduleId + "' and "+DbManagerValue.colValue+" IS NOT NULL", null);
		mCount.moveToFirst();
		int count= mCount.getInt(0);
		mCount.close();
		DbRepositoryValue.getInstance().close();
		return count;
	}

	public static CorrectiveValueModel getItemValue(String scheduleId,int itemId, int operatorId) {
		CorrectiveValueModel model = null;
		String table = DbManagerValue.mCorrectiveValue;
		String[] columns = null;
		String where =DbManagerValue.colScheduleId+"=? AND "+DbManagerValue.colItemId+"=? AND "+DbManagerValue.colOperatorId+"=?";
		String[] args = new String[]{scheduleId,String.valueOf(itemId),String.valueOf(operatorId)};

		Cursor cursor;

		DbRepositoryValue.getInstance().open(TowerApplication.getInstance());
		cursor = DbRepositoryValue.getInstance().getDB().query(true, table, columns, where, args, null, null,null, null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			return model;
		}

		model = getSiteFromCursor(cursor);
		cursor.close();
		DbRepositoryValue.getInstance().close();
		return model;
	}

	public ArrayList<CorrectiveValueModel> getCorrectiveValues(String scheduleId) {
//		String MY_QUERY = "SELECT * FROM table_a a INNER JOIN table_b b ON a.id=b.other_id WHERE b.property_id=?";
//
//		DbRepositoryValue.getInstance().getDB().rawQuery(MY_QUERY, new String[]{String.valueOf(propertyId)});
		ArrayList<CorrectiveValueModel> model = new ArrayList<CorrectiveValueModel>();
		String table = DbManagerValue.mCorrectiveValue;
		String[] columns = null;
		String where =DbManagerValue.colScheduleId+"=?";
		String[] args = new String[]{scheduleId};
		String order =  DbManagerValue.colItemId + " ASC,"+ DbManagerValue.colOperatorId + " ASC,"+DbManagerValue.colPhotoStatus + " DESC";
		Cursor cursor;

		DbRepositoryValue.getInstance().open(TowerApplication.getInstance());
		cursor = DbRepositoryValue.getInstance().getDB().query(true, table, columns, where, args, null, null,order, null);

		if (!cursor.moveToFirst()){
			DebugLog.d("corrective value for this schedule is null");
			cursor.close();
			return model;
		}
		do{
			model.add(getSiteFromCursor(cursor));
			DebugLog.d("corrective item id : "+model.get(model.size() - 1).itemId);
		}while(cursor.moveToNext());

		cursor.close();
		DbRepositoryValue.getInstance().close();
		return model;
	}

	public static ArrayList<CorrectiveValueModel> getCorrectiveValues(String scheduleId, int itemId) {

		String table = DbManagerValue.mCorrectiveValue;
		String[] columns = null;
		String wherescheduleid  = DbManagerValue.colScheduleId + "=?";
		String whereitemid   	= itemId != UNSPECIFIED ? " AND " + DbManagerValue.colItemId + "=?" : "";
		String order 			= DbManagerValue.colItemId + " ASC";

		String where = wherescheduleid + whereitemid;
		DebugLog.d("Get corrective value item(s) by (" + scheduleId + "," + itemId +")");

		List<String> argsList = new ArrayList<>();

		if (scheduleId != null)
			argsList.add(scheduleId);
		if (itemId != UNSPECIFIED)
			argsList.add(String.valueOf(itemId));

		String[] args = new String[argsList.size()];
		args = argsList.toArray(args);

		DbRepositoryValue.getInstance().open(TowerApplication.getInstance());
		Cursor cursor = DbRepositoryValue.getInstance().getDB().query(true, table, columns, where, args, null, null, order, null);

        if (!cursor.moveToFirst()) {

			cursor.close();
			DbRepositoryValue.getInstance().close();
			return null;
		}

		ArrayList<CorrectiveValueModel> results = new ArrayList<>();

		do {

			CorrectiveValueModel model = getSiteFromCursor(cursor);
			results.add(model);

		} while (cursor.moveToNext());

		cursor.close();
		DbRepositoryValue.getInstance().close();
		return results;
	}

	public static ArrayList<CorrectiveValueModel> getCorrectiveValuesForUpload() {
		ArrayList<CorrectiveValueModel> model = new ArrayList<CorrectiveValueModel>();
		String table = DbManagerValue.mCorrectiveValue;
		String[] columns = null;
		String where = null;
		String[] args = null;
		if (ItemUploadManager.getInstance().isRunning()){
			where =DbManagerValue.colUploadStatus+"!=? AND "+DbManagerValue.colUploadStatus+"!=? AND "+DbManagerValue.colOperatorId+"!=? AND "+DbManagerValue.colValue+" IS NOT NULL";
			args = new String[]{String.valueOf(UPLOAD_ONGOING),String.valueOf(UPLOAD_DONE), String.valueOf(ALL_OP_ID)};
		}else{
			where =DbManagerValue.colUploadStatus+"!=? AND "+DbManagerValue.colOperatorId+"!=? AND "+DbManagerValue.colValue+" IS NOT NULL";
			args = new String[]{String.valueOf(UPLOAD_DONE), String.valueOf(ALL_OP_ID)};
		}
		Cursor cursor;

		DbRepositoryValue.getInstance().open(TowerApplication.getInstance());
		cursor = DbRepositoryValue.getInstance().getDB().query(true, table, columns, where, args, null, null,null, null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			DbRepositoryValue.getInstance().close();
			return model;
		}
		do{
			model.add(getSiteFromCursor(cursor));
		}	
		while(cursor.moveToNext());
		cursor.close();
		DbRepositoryValue.getInstance().close();
		return model;
	}

	public static ArrayList<CorrectiveValueModel> getCorrectiveValuesForUploadWithMandatoryCheck(String scheduleId, Vector<WorkFormGroupModel> groupModels) {

		DebugLog.d("upload corrective value items by scheduleId = " + scheduleId);
		ArrayList<CorrectiveValueModel> uploadItems = new ArrayList<>();

		for (WorkFormGroupModel perGroup : groupModels) {

			if (getCorrectiveValuesForUploadWithMandatoryCheck(scheduleId, perGroup.id) == null)
				return null;

			uploadItems.addAll(getCorrectiveValuesForUploadWithMandatoryCheck(scheduleId, perGroup.id));

		}

		return uploadItems;
	}

	public static ArrayList<CorrectiveValueModel> getCorrectiveValuesForUploadWithMandatoryCheck(String scheduleId, int work_form_group_id) {

		DebugLog.d("upload corrective value items by scheduleid = " + scheduleId + " workFormGroupId = " + work_form_group_id);
		ArrayList<CorrectiveValueModel> results = new ArrayList<>();

		ArrayList<WorkFormItemModel> workFormItems = WorkFormItemModel.getWorkFormItems(work_form_group_id, "label");

		if (workFormItems != null) {

			for (WorkFormItemModel workFormItem : workFormItems) {

				boolean isMandatory = workFormItem.mandatory;
				boolean isDisabled  = workFormItem.disable;

				if (!isDisabled) {

					if (workFormItem.scope_type.equalsIgnoreCase("all")) {

						ArrayList<CorrectiveValueModel> correctiveValues = getCorrectiveValues(scheduleId, workFormItem.id);

						if (correctiveValues == null && isMandatory) {

							// mandatory item is not filled
							DebugLog.e("Item " + workFormItem.label + " kosong, cancel upload");
							return null;
						}
						else if (correctiveValues != null) { // there are some filled items

							if (!isCorrectiveValueValidated(workFormItem, correctiveValues.get(0)))
								return null;

							// otherwise just add all the items
							DebugLog.d("-> added");
							results.addAll(correctiveValues);
						}

					} else {

						ScheduleBaseModel schedule = new ScheduleGeneral();
						schedule = schedule.getScheduleById(scheduleId);

						for (OperatorModel operator : schedule.operators) {

							int operatorid = operator.id;
							CorrectiveValueModel correctiveValue = getItemValue(scheduleId, workFormItem.id, operatorid);

							if (workFormItem.mandatory) {

								if (!isCorrectiveValueValidated(workFormItem, correctiveValue))
									return null;

								results.add(correctiveValue);

							} else {

								if (correctiveValue != null)
									results.add(correctiveValue);

							}
						}
					}
				}
			}

		} else {
			DebugLog.e("workitems is null");
			return null;
		}

		return results;
	}

	public static class AsyncCollectCorrectiveValuesForUpload extends AsyncTask<Void, String, ArrayList<CorrectiveValueModel>> {

		private final String DONEPREPARINGITEMS = "DONEPREPARINGITEMS";
		private String scheduleId;
		private int workFormGroupId;

		private ScheduleBaseModel scheduleBaseModel;
		private WorkFormModel workFormModel;
		private WorkFormGroupModel groupModel;
		private Vector<WorkFormGroupModel> workFormGroupModels;

		public AsyncCollectCorrectiveValuesForUpload(String scheduleId, int work_form_group_id) {
			this.scheduleId = scheduleId;
			this.workFormGroupId = work_form_group_id;
		}

		private void publish(String msg) {
			publishProgress(msg);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			publish("Menyiapkan item untuk diupload");

			if (workFormGroupId == UNSPECIFIED) {

				scheduleBaseModel = new ScheduleGeneral();
				scheduleBaseModel = scheduleBaseModel.getScheduleById(scheduleId);

				DebugLog.d("scheduleBase id : " + scheduleBaseModel.id);
				DebugLog.d("scheduleBase worktype id : " + scheduleBaseModel.work_type.id);

				workFormModel = new WorkFormModel();
				workFormModel = workFormModel.getItemByWorkTypeId(scheduleBaseModel.work_type.id);

				groupModel = new WorkFormGroupModel();
				workFormGroupModels = WorkFormGroupModel.getAllItemByWorkFormId(workFormModel.id);
			}
		}

		@Override
		protected ArrayList<CorrectiveValueModel> doInBackground(Void... voids) {

			if (workFormGroupId == UNSPECIFIED) {

				if (!workFormGroupModels.isEmpty()) {

					return getCorrectiveValuesForUploadWithMandatoryCheck(scheduleId, workFormGroupModels);

				} else {

					Crashlytics.log(Log.ERROR, CorrectiveValueModel.class.getSimpleName(), "Schedule " + scheduleId + " tidak memiliki daftar workformgroup");
					TowerApplication.getInstance().toast("Schedule " + scheduleId + " tidak memiliki daftar workformgroup", Toast.LENGTH_SHORT);

					return new ArrayList<>();
				}

			} else {

				return getCorrectiveValuesForUploadWithMandatoryCheck(scheduleId, workFormGroupId);

			}

		}

		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			UploadProgressEvent event = new UploadProgressEvent(values[0]);
			event.done = values[0].equalsIgnoreCase(DONEPREPARINGITEMS);
			EventBus.getDefault().post(event);
		}

		@Override
		protected void onPostExecute(ArrayList<CorrectiveValueModel> uploadItems) {
			super.onPostExecute(uploadItems);
			publish(DONEPREPARINGITEMS);
			ItemUploadManager.getInstance().addCorrectiveValues(uploadItems);

		}
	}

	public static boolean isPictureRadioCorrectiveValidated(WorkFormItemModel workFormItem, FormValueModel filledItem) {

		// checking for form's item type picture radio with mandatory applied only on "NOK" option
		if (TextUtils.isEmpty(filledItem.photoStatus)) {
			DebugLog.e("Photo status item " + workFormItem.label + " harus diisi");
			TowerApplication.getInstance().toast("Photo status item " + workFormItem.label + " harus diisi", Toast.LENGTH_LONG);
			return false;
		}

		if (!TextUtils.isEmpty(filledItem.photoStatus) &&
				filledItem.photoStatus.equalsIgnoreCase(Constants.NOK) &&
				TextUtils.isEmpty(filledItem.remark)) {
			DebugLog.e("Remark item " + workFormItem.label + " harus diisi");
			TowerApplication.getInstance().toast("Remark item " + workFormItem.label + " harus diisi", Toast.LENGTH_LONG);
			return false;
		}

		return true;
	}

	public static boolean isCorrectiveValueValidated(WorkFormItemModel workFormItem, CorrectiveValueModel filledItem) {

		if (!workFormItem.disable) {

			if (filledItem == null && workFormItem.mandatory) {

				// mandatory item is not filled
				//DebugLog.e("Item " + workFormItem.label + " kosong harus diisi");
				//TowerApplication.getInstance().toast("Item " + workFormItem.label + " kosong harus diisi", Toast.LENGTH_SHORT);

				DebugLog.d("Mandatory item ada yang kosong");
				TowerApplication.getInstance().toast("Mandatory item ada yang kosong", Toast.LENGTH_SHORT);
				return false;

			} else if (filledItem != null) {

				// if the filled items are mandatory, then apply strict rules
				if (workFormItem.mandatory) {

					// if the value is empty
					if (TextUtils.isEmpty(filledItem.value)) {

						// if the item type is file "photoitem"
						if (workFormItem.field_type.equalsIgnoreCase("file")) {

							// and photo status is not null or empty and photo status not equal "NA"
							if (!TextUtils.isEmpty(filledItem.photoStatus) && !filledItem.photoStatus.equalsIgnoreCase(Constants.NA)) {
								DebugLog.e("Photo item" + workFormItem.label + " harus ada");
								TowerApplication.getInstance().toast("Photo item" + workFormItem.label + " harus ada", Toast.LENGTH_LONG);
								return false;
							}

						} else {

                            /*DebugLog.e("Item " + workFormItem.label + " kosong, cancel upload");
                            TowerApplication.getInstance().toast("Item " + workFormItem.label + " kosong harus diisi", Toast.LENGTH_SHORT);*/

							DebugLog.d("Mandatory item ada yang kosong");
							TowerApplication.getInstance().toast("Mandatory item ada yang kosong", Toast.LENGTH_SHORT);
							return false; // and not photo item radio, then return null
						}

					}

				}

				return !workFormItem.field_type.equalsIgnoreCase("file") || isPictureRadioCorrectiveValidated(workFormItem, filledItem);
			}
		}

		return true;
	}
	public void save(Context context){

		save();

	}

	public void save(){
		DebugLog.d("saving value on itemvaluemodel");
		DebugLog.d("row id : "+  rowId);
		DebugLog.d("schedule Id : "+  scheduleId);
		DebugLog.d("operator id : "+  operatorId);
		DebugLog.d("item id : "+  itemId);
		DebugLog.d("------------------------------------");
		save(scheduleId, photoStatus);
	}

	public void saveForCorrective(){
		save(scheduleId+"-"+photoStatus, photoStatus);
	}  

	public void save(String scheduleId, String photoStatus){
		String sql = String.format("INSERT OR REPLACE INTO %s(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",
				DbManagerValue.mCorrectiveValue , DbManagerValue.colScheduleId,
				DbManagerValue.colItemId,DbManagerValue.colValue,
				DbManagerValue.colIsPhoto,DbManagerValue.colOperatorId,
				DbManagerValue.colRowId, DbManagerValue.colRemark,
				DbManagerValue.colLatitude, DbManagerValue.colLongitude,
				DbManagerValue.colPhotoStatus,DbManagerValue.colGPSAccuracy,
				DbManagerValue.colUploadStatus);

		DbRepositoryValue.getInstance().open(TowerApplication.getInstance());
		SQLiteStatement stmt = DbRepositoryValue.getInstance().getDB().compileStatement(sql);

		bindAndCheckNullString(stmt, 1, scheduleId);
		stmt.bindLong(2, itemId);
		bindAndCheckNullString(stmt, 3, value);
		bindBooleanToInteger(stmt, 4, typePhoto);
		stmt.bindLong(5, operatorId);
		stmt.bindLong(6, rowId);
		bindAndCheckNullString(stmt, 7, remark);
		bindAndCheckNullString(stmt, 8, latitude);
		bindAndCheckNullString(stmt, 9, longitude);
		bindAndCheckNullString(stmt, 10, photoStatus);
		stmt.bindLong(11, gpsAccuracy);
		stmt.bindLong(12, uploadStatus);

		stmt.executeInsert();
		stmt.close();
		DbRepositoryValue.getInstance().close();
	}

	public void insert(){
		try{
			String sql = String.format("INSERT INTO %s(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",
					DbManagerValue.mCorrectiveValue , DbManagerValue.colScheduleId,
					DbManagerValue.colItemId,DbManagerValue.colValue,
					DbManagerValue.colIsPhoto,DbManagerValue.colOperatorId,
					DbManagerValue.colRowId, DbManagerValue.colRemark,
					DbManagerValue.colLatitude, DbManagerValue.colLongitude,
					DbManagerValue.colPhotoStatus,DbManagerValue.colGPSAccuracy,
					DbManagerValue.colUploadStatus);

			DbRepositoryValue.getInstance().open(TowerApplication.getInstance());
			SQLiteStatement stmt = DbRepositoryValue.getInstance().getDB().compileStatement(sql);

			bindAndCheckNullString(stmt, 1, scheduleId);
			stmt.bindLong(2, itemId);
			bindAndCheckNullString(stmt, 3, value);
			bindBooleanToInteger(stmt, 4, typePhoto);
			stmt.bindLong(5, operatorId);
			stmt.bindLong(6, rowId);
			bindAndCheckNullString(stmt, 7, remark);
			bindAndCheckNullString(stmt, 8, latitude);
			bindAndCheckNullString(stmt, 9, longitude);
			bindAndCheckNullString(stmt, 10, photoStatus);
			stmt.bindLong(11, gpsAccuracy);
			stmt.bindLong(12, uploadStatus);

			stmt.executeInsert();
			stmt.close();
			DbRepositoryValue.getInstance().close();
		}catch(Exception e) {

		}
	}

	public void saveOrReplace(){
		DebugLog.d("saving value on itemvaluemodel");
		DebugLog.d("row id : "+  rowId);
		DebugLog.d("schedule Id : "+  scheduleId);
		DebugLog.d("operator id : "+  operatorId);
		DebugLog.d("item id : "+  itemId);
		DebugLog.d("------------------------------------");
		saveOrReplace(scheduleId, photoStatus);
	}


	public void saveOrReplace(String scheduleId, String photoStatus){
		String sql = String.format("INSERT OR REPLACE INTO %s(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",
				DbManagerValue.mCorrectiveValue , DbManagerValue.colScheduleId,
				DbManagerValue.colItemId,DbManagerValue.colValue,
				DbManagerValue.colIsPhoto,DbManagerValue.colOperatorId,
				DbManagerValue.colRowId, DbManagerValue.colRemark,
				DbManagerValue.colLatitude, DbManagerValue.colLongitude,
				DbManagerValue.colPhotoStatus,DbManagerValue.colGPSAccuracy,
				DbManagerValue.colUploadStatus);

		DbRepositoryValue.getInstance().open(TowerApplication.getInstance());
		SQLiteStatement stmt = DbRepositoryValue.getInstance().getDB().compileStatement(sql);

		bindAndCheckNullString(stmt, 1, scheduleId);
		stmt.bindLong(2, itemId);
		bindAndCheckNullString(stmt, 3, value);
		bindBooleanToInteger(stmt, 4, typePhoto);
		stmt.bindLong(5, operatorId);
		stmt.bindLong(6, rowId);
		bindAndCheckNullString(stmt, 7, remark);
		bindAndCheckNullString(stmt, 8, latitude);
		bindAndCheckNullString(stmt, 9, longitude);
		bindAndCheckNullString(stmt, 10, photoStatus);
		stmt.bindLong(11, gpsAccuracy);
		stmt.bindLong(12, uploadStatus);

		stmt.executeInsert();
		stmt.close();
		DbRepositoryValue.getInstance().close();
	}

	private static CorrectiveValueModel getSiteFromCursor(Cursor c) {
		CorrectiveValueModel FormValueModel = null;

		if (null == c)
			return FormValueModel;

		FormValueModel = new CorrectiveValueModel();
		FormValueModel.scheduleId = (c.getString(c.getColumnIndex(DbManagerValue.colScheduleId)));
		FormValueModel.operatorId = (c.getInt(c.getColumnIndex(DbManagerValue.colOperatorId)));
		FormValueModel.itemId = (c.getInt(c.getColumnIndex(DbManagerValue.colItemId)));
		FormValueModel.rowId = (c.getInt(c.getColumnIndex(DbManagerValue.colRowId)));
		FormValueModel.gpsAccuracy = (c.getInt(c.getColumnIndex(DbManagerValue.colGPSAccuracy)));
		FormValueModel.remark = (c.getString(c.getColumnIndex(DbManagerValue.colRemark)));
		FormValueModel.latitude = (c.getString(c.getColumnIndex(DbManagerValue.colLatitude)));
		FormValueModel.longitude = (c.getString(c.getColumnIndex(DbManagerValue.colLongitude)));
		FormValueModel.photoStatus = (c.getString(c.getColumnIndex(DbManagerValue.colPhotoStatus)));
		FormValueModel.value = (c.getString(c.getColumnIndex(DbManagerValue.colValue)));
		FormValueModel.uploadStatus = (c.getInt(c.getColumnIndex(DbManagerValue.colUploadStatus)));
		FormValueModel.typePhoto = c.getInt(c.getColumnIndex(DbManagerValue.colIsPhoto)) == 1;

		return FormValueModel;
	}

	public static String createDB(){
		return "create table if not exists " + DbManagerValue.mCorrectiveValue
				+ " ("+ DbManagerValue.colScheduleId + " integer, "
				+ DbManagerValue.colOperatorId + " integer, "
				+ DbManagerValue.colItemId + " integer, "
				+ DbManagerValue.colSiteId + " integer, "
				+ DbManagerValue.colGPSAccuracy + " integer, "
				+ DbManagerValue.colRowId + " integer, "
				+ DbManagerValue.colRemark + " varchar, "
				+ DbManagerValue.colPhotoStatus + " varchar, "
				+ DbManagerValue.colLatitude + " varchar, "
				+ DbManagerValue.colLongitude + " varchar, "
				+ DbManagerValue.colValue + " varchar, "
				+ DbManagerValue.colUploadStatus + " integer, "
				+ DbManagerValue.colIsPhoto + " integer, "
				+ "PRIMARY KEY (" + DbManagerValue.colScheduleId + ","+ DbManagerValue.colItemId + ","+ DbManagerValue.colOperatorId + "," + DbManagerValue.colPhotoStatus + "))";
	}
	
	public static void resetAllUploadStatus(){

		DbRepositoryValue.getInstance().open(TowerApplication.getInstance());
		ContentValues cv = new ContentValues();
		cv.put(DbManagerValue.colUploadStatus, UPLOAD_NONE);

		DbRepositoryValue.getInstance().getDB().update(DbManagerValue.mCorrectiveValue, cv, null, null);
		DbRepositoryValue.getInstance().close();
	}
}
