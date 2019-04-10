package com.sap.inspection.model.value;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.os.Parcel;

import com.sap.inspection.MyApplication;
import com.sap.inspection.manager.ItemUploadManager;
import com.sap.inspection.model.BaseModel;
import com.sap.inspection.model.DbRepository;
import com.sap.inspection.tools.DebugLog;


public class CorrectiveValueModel extends ItemValueModel {

	private final int ALL_OP_ID = -1; 

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

		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
		String sql = "DELETE FROM " + DbManagerValue.mCorrectiveValue + " WHERE "+DbManagerValue.colScheduleId+"="+scheduleId+" AND "+DbManagerValue.colItemId+"="+itemId+" AND "+DbManagerValue.colOperatorId+"="+operatorId;
		SQLiteStatement stmt = DbRepositoryValue.getInstance().getDB().compileStatement(sql);
		stmt.executeUpdateDelete();
		stmt.close();
		DbRepositoryValue.getInstance().close();
	}
	
	public static void deleteAll(Context ctx){

		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
		String sql = "DELETE FROM " + DbManagerValue.mCorrectiveValue;
		SQLiteStatement stmt = DbRepositoryValue.getInstance().getDB().compileStatement(sql);
		stmt.executeUpdateDelete();
		stmt.close();
		DbRepositoryValue.getInstance().close();
	}

	public static CorrectiveValueModel getItemValue(Context context,String scheduleId, int itemId, int operatorId) {
		CorrectiveValueModel model = null;

		model = getItemValue(scheduleId, itemId, operatorId);

		return model;
	}

	public static int countTaskDone(String scheduleId){
		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
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

		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
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

	public ArrayList<CorrectiveValueModel> getCorrectiveValue(String scheduleId) {
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

		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
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

	public ArrayList<ItemValueModel> getItemValuesForUpload() {
		ArrayList<ItemValueModel> model = new ArrayList<ItemValueModel>();
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

		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
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

		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
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

			DbRepositoryValue.getInstance().open(MyApplication.getInstance());
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

		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
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

		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
		ContentValues cv = new ContentValues();
		cv.put(DbManagerValue.colUploadStatus, UPLOAD_NONE);

		DbRepositoryValue.getInstance().getDB().update(DbManagerValue.mCorrectiveValue, cv, null, null);
		DbRepositoryValue.getInstance().close();
	}
}
