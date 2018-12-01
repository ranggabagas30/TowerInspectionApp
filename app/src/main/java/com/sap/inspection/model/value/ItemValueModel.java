package com.sap.inspection.model.value;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Debug;
import android.os.Parcel;

import com.sap.inspection.MyApplication;
import com.sap.inspection.manager.ItemUploadManager;
import com.sap.inspection.model.BaseModel;
import com.sap.inspection.model.DbManager;
import com.sap.inspection.model.DbRepository;
import com.sap.inspection.tools.DebugLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.crashlytics.android.Crashlytics.log;

//import static com.sap.inspection.model.value.DbManagerValue.material_request;

//import static com.sap.inspection.model.value.DbManagerValue.material_request;


public class ItemValueModel extends BaseModel {
	public static final int UPLOAD_NONE = 0;
	public static final int UPLOAD_ONGOING = 1;
	public static final int UPLOAD_DONE = 2;
	public static final int UPLOAD_FAIL = 3;

	public String scheduleId;
	public int operatorId;
	public int itemId;
	public int siteId;
	public int work_form_group_id;
	public String photoDate;
	public String createdAt;
	public String value;
	public int rowId;
	public int gpsAccuracy;
	public String remark;
	public String latitude;
	public String longitude;
	public String photoStatus;
	public int uploadStatus = UPLOAD_NONE;
	public boolean typePhoto;
	public String picture;
	public boolean disable;

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

	public static void deleteAll(Context ctx){

		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
		String sql = "DELETE FROM " + DbManagerValue.mFormValue;
		SQLiteStatement stmt = DbRepositoryValue.getInstance().getDB().compileStatement(sql);
		stmt.executeUpdateDelete();
		stmt.close();
		DbRepositoryValue.getInstance().close();

	}

	public static void delete(String scheduleId, int itemId, int operatorId){

		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
		String sql = "DELETE FROM " + DbManagerValue.mFormValue + " WHERE "+DbManagerValue.colScheduleId+"="+scheduleId+" AND "+DbManagerValue.colItemId+"="+itemId+" AND "+DbManagerValue.colOperatorId+"="+operatorId;
		SQLiteStatement stmt = DbRepositoryValue.getInstance().getDB().compileStatement(sql);
		stmt.executeUpdateDelete();
		stmt.close();
		DbRepositoryValue.getInstance().close();
	}

	public ArrayList<ItemValueModel> getAllItemValueByScheduleId (Context context, String scheduleId) {

		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
		ArrayList<ItemValueModel> listModel = null;
		listModel = getAllItemValueByScheduleId(scheduleId);
		DbRepositoryValue.getInstance().close();
		return listModel;
	}

	public ArrayList<ItemValueModel> getAllItemValueByScheduleId (String scheduleId) {

		ArrayList<ItemValueModel> listModel = null;
		String table = DbManagerValue.mFormValue;
		String[] columns = null;
		String where = DbManagerValue.colScheduleId+"=?";
		String[] args = new String[]{scheduleId};
		Cursor cursor;

		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
		cursor = DbRepositoryValue.getInstance().getDB().query(false, table, columns, where, args, null, null,null, null);

		if (cursor.moveToFirst()) {
			do {
				ItemValueModel model;
				model = getSiteFromCursor(cursor);
				listModel.add(model);
			} while (cursor.moveToNext());
		}

		cursor.close();
		DbRepositoryValue.getInstance().close();
		return listModel;
	}

	public ItemValueModel getItemValue(Context context,String scheduleId, int itemId, int operatorId) {

		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
		ItemValueModel model = null;
		model = getItemValue(scheduleId, itemId, operatorId);
		DbRepositoryValue.getInstance().close();
		return model;
	}

	public static int countTaskDone(String scheduleId, int rowId){

		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
		Cursor mCount= DbRepositoryValue.getInstance().getDB().rawQuery("select count(*) from "+DbManagerValue.mFormValue+" where "+DbManagerValue.colScheduleId+"='" + scheduleId + "' and "+DbManagerValue.colRowId+"='" + rowId +"'", null);
		mCount.moveToFirst();
		int count= mCount.getInt(0);
		mCount.close();
		DbRepositoryValue.getInstance().close();
		return count;
	}

	public ItemValueModel getItemValue(String scheduleId,int itemId, int operatorId) {


		ItemValueModel model = null;
		String table = DbManagerValue.mFormValue;
		String[] columns = null;
		String where = DbManagerValue.colScheduleId+"=? AND "+DbManagerValue.colItemId+"=? AND "+DbManagerValue.colOperatorId+"=?";
		String[] args = new String[]{scheduleId,String.valueOf(itemId),String.valueOf(operatorId)};
		Cursor cursor;

		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
		cursor = DbRepositoryValue.getInstance().getDB().query(true, table, columns, where, args, null, null,null, null);

		if (!cursor.moveToFirst()) {

			cursor.close();
			DbRepositoryValue.getInstance().close();
			return model;
		}

		model = getSiteFromCursor(cursor);

		cursor.close();
		DbRepositoryValue.getInstance().close();
		return model;
	}

	public ItemValueModel getItemValue(int itemId, int operatorId, String userName) {



		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
		ItemValueModel model = null;
		DbRepositoryValue.getInstance().getDB().execSQL("attach database "+userName+"_"+DbManager.dbName+" as general");

		String query = "SELECT * FROM " + DbManagerValue.mFormValue + " t1 INNER JOIN general." + DbManager.mSchedule + " t2 ON t1." + DbManagerValue.colScheduleId + "=t2." + DbManager.colID +" ORDER BY t2." + DbManager.colWorkDate + " DESC";
//		String[] args = new String[]{workType.toUpperCase()};
		String[] args = null;
		Cursor cursor = null;
		try{
			cursor = DbRepositoryValue.getInstance().getDB().rawQuery(query, args);
		}catch (Exception e) {
			// this gets called even if there is an exception somewhere above
			if(cursor != null)
				cursor.close();
			DbRepositoryValue.getInstance().close();
			return model;
		}

		if (!cursor.moveToFirst()) {
			cursor.close();
			DbRepositoryValue.getInstance().close();
			return model;
		}

		model = getSiteFromCursor(cursor);

		cursor.close();
		DbRepositoryValue.getInstance().close();
		return model;
	}

    public ItemValueModel getPhotoLocationItemValue(Context context, String scheduleId) {

		ItemValueModel model = null;
		model = getPhotoLocationItemValue(scheduleId);

        return model;
    }

    public ItemValueModel getPhotoLocationItemValue(String scheduleId) {


		ItemValueModel model = null;
		String table = DbManagerValue.mFormValue;
		String[] columns = null;
		String where = DbManagerValue.colScheduleId+"=?";
		String[] args = new String[]{scheduleId};
		Cursor cursor;

		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
		cursor = DbRepositoryValue.getInstance().getDB().query(false, table, columns, where, args, null, null, DbManagerValue.colCreatedAt + " DESC ", "1");

        if (!cursor.moveToFirst()) {
        	cursor.close();
			DbRepositoryValue.getInstance().close();
			return model;
		}

        model = getSiteFromCursor(cursor);

        cursor.close();
        DbRepositoryValue.getInstance().close();
        return model;
    }

	public ArrayList<ItemValueModel> getItemValuesForUpload() {


		ArrayList<ItemValueModel> model = new ArrayList<ItemValueModel>();
		String table = DbManagerValue.mFormValue;
		String[] columns = null;
		String where = null;
		String[] args = null;

		if (ItemUploadManager.getInstance().isRunning()){
			where =DbManagerValue.colUploadStatus+"!=? AND "+DbManagerValue.colUploadStatus+"!=?";
			args = new String[]{String.valueOf(UPLOAD_ONGOING),String.valueOf(UPLOAD_DONE)};
		}else{
			where =DbManagerValue.colUploadStatus+"!=?";
			args = new String[]{String.valueOf(UPLOAD_DONE)};
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

	public ArrayList<ItemValueModel> getItemValuesForUpload(String scheduleId) {

		ArrayList<ItemValueModel> model = new ArrayList<ItemValueModel>();
		String table = DbManagerValue.mFormValue;
		String[] columns = null;
		String where = null;
		String[] args = null;
		log("get itemvalues for " + scheduleId);

		if (ItemUploadManager.getInstance().isRunning()){
			where =DbManagerValue.colUploadStatus+"!=? AND "+DbManagerValue.colScheduleId+"=?";
			args = new String[]{String.valueOf(UPLOAD_ONGOING), scheduleId};
		}else{
			where = DbManagerValue.colScheduleId+"=?";
			args = new String[]{scheduleId};
		}
		log(where);

		Cursor cursor;

		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
		cursor = DbRepositoryValue.getInstance().getDB().query(true, table, columns, where, args, null, null,null, null);

		if (!cursor.moveToFirst()){
			log("model null ");
			cursor.close();
			DbRepositoryValue.getInstance().close();
			return model;
		}
		do{
			model.add(getSiteFromCursor(cursor));
			log("" + model.get(model.size() - 1).value);
		}
		while(cursor.moveToNext());
		log("model size "+model.size());
		cursor.close();
		DbRepositoryValue.getInstance().close();
		return model;
	}

	public void save(Context context){

		save();
	}

	public void save(){

		log("saving value on itemvaluemodel");
		log("row id : "+  rowId);
		log("schedule Id : "+  scheduleId);
		log("operator id : "+  operatorId);
		log("item id : " + itemId);
		log("------------------------------------");
		save(scheduleId, photoStatus);
	}

	public void saveForCorrective(){
		save(scheduleId+"-"+photoStatus, photoStatus);
	}

	public void save(String scheduleId, String photoStatus){

		String sql = null;
		switch (DbManagerValue.schema_version) {
			case 1 : {
				break;
			}
			case 2 : {
				break;
			}
			case 3 : {
				break;
			}
			case 4 : {
				break;
			}
			case 5 : {
				break;
			}
			case 6 : {
				break;
			}
			case 7 : {
				sql = String.format("INSERT OR REPLACE INTO %s(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)",
						DbManagerValue.mFormValue , DbManagerValue.colScheduleId,
						DbManagerValue.colItemId,DbManagerValue.colValue,
						DbManagerValue.colIsPhoto,DbManagerValue.colOperatorId,
						DbManagerValue.colRowId, DbManagerValue.colRemark,
						DbManagerValue.colLatitude, DbManagerValue.colLongitude,
						DbManagerValue.colPhotoStatus,DbManagerValue.colGPSAccuracy,
						DbManagerValue.colUploadStatus,DbManagerValue.colCreatedAt);
				break;
			}
			case 8 : {
				sql = String.format("INSERT OR REPLACE INTO %s(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
						DbManagerValue.mFormValue , DbManagerValue.colScheduleId,
						DbManagerValue.colItemId,DbManagerValue.colValue,
						DbManagerValue.colIsPhoto,DbManagerValue.colOperatorId,
						DbManagerValue.colRowId, DbManagerValue.colRemark,
						DbManagerValue.colLatitude, DbManagerValue.colLongitude,
						DbManagerValue.colPhotoStatus,DbManagerValue.colGPSAccuracy,
						DbManagerValue.colUploadStatus,DbManagerValue.colCreatedAt,
						DbManagerValue.colPhotoDate);

				break;
			}
			default:
				break;
		}


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
		bindAndCheckNullString(stmt, 13, getCurrentDate());

		if (DbManagerValue.schema_version == 8)
			bindAndCheckNullString(stmt, 14, photoDate);
		stmt.executeInsert();
		stmt.close();
		DbRepositoryValue.getInstance().close();
	}

	private boolean isColumnExist(String colName) {
		boolean isExist = false;
		Cursor res = null;
		try {

			res = DbRepositoryValue.getInstance().getDB().rawQuery("Select * from "+ DbManagerValue.mFormValue +" limit 1", null);

			int colIndex = res.getColumnIndex(colName);
			if (colIndex!=-1){
				isExist = true;
			}

		} catch (Exception e) {
		} finally {
			try { if (res !=null){ res.close(); } } catch (Exception e1) {}
		}
		return isExist;
	}

	private void AddColumn(String colName, String colType) {
		String sql = String.format("ALTER TABLE %s ADD COLUMN %s %s",
				DbManagerValue.mFormValue, colName, colType);
		try {
			DbRepositoryValue.getInstance().getDB().execSQL(sql);
		} catch (Exception e) {
			e.printStackTrace();
			DebugLog.d("error add columns");
		}
	}

	private String getCurrentDate(){
		Date currentDate = Calendar.getInstance().getTime();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm", Locale.getDefault());
		return simpleDateFormat.format(currentDate);
	}


	private ItemValueModel getSiteFromCursor(Cursor c) {
		ItemValueModel FormValueModel = null;

		if (null == c)
			return FormValueModel;

		FormValueModel = new ItemValueModel();
		switch (DbManagerValue.schema_version) {
			case 1 : {
				//continue 1 to 2
			}
			case 2 : {
				//continue 2 to 3
			}
			case 3 : {
				//continue 3 to 4
			}
			case 4 : {
				//continue 4 to 5
			}
			case 5 : {
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
				break;
			}
			case 6 : {
				//continue 6 to 7
			}
			case 7 : {
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
				FormValueModel.createdAt = (c.getString(c.getColumnIndex(DbManagerValue.colCreatedAt)));
				break;
			}
			case 8 : {
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
				FormValueModel.createdAt = (c.getString(c.getColumnIndex(DbManagerValue.colCreatedAt)));
				FormValueModel.photoDate = (c.getString(c.getColumnIndex(DbManagerValue.colPhotoDate)));
				break;
			}
			default:
				break;
		}
		return FormValueModel;
	}

	public static String createDB(){
		String createTable = null;
		switch (DbManagerValue.schema_version) {
			case 1 : {
				//continue 1 to 2
			}
			case 2 : {
				//continue 2 to 3
			}
			case 3 : {
				//continue 3 to 4
			}
			case 4 : {
				//continue 4 to 5
			}
			case 5 : {
				createTable =  "create table if not exists " + DbManagerValue.mFormValue
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
								+ "PRIMARY KEY (" + DbManagerValue.colScheduleId + ","+ DbManagerValue.colItemId + ","+ DbManagerValue.colOperatorId + "))";
				break;
			}
			case 6 : {
				//continue 6 to 7
			}
			case 7 : {
				createTable =  "create table if not exists " + DbManagerValue.mFormValue
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
								+ DbManagerValue.colCreatedAt + " varchar, "
								+ "PRIMARY KEY (" + DbManagerValue.colScheduleId + ","+ DbManagerValue.colItemId + ","+ DbManagerValue.colOperatorId + "))";
				break;
			}
			case 8 : {
				createTable =  "create table if not exists " + DbManagerValue.mFormValue
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
								+ DbManagerValue.colCreatedAt + " varchar, "
								+ DbManagerValue.colPhotoDate + " varchar,"
								+ "PRIMARY KEY (" + DbManagerValue.colScheduleId + ","+ DbManagerValue.colItemId + ","+ DbManagerValue.colOperatorId + "))";
				break;
			}
			default:
				break;
		}
		return createTable;
	}

	public static void resetAllUploadStatus(){

		DbRepositoryValue.getInstance().open(MyApplication.getInstance());

		ContentValues cv = new ContentValues();
		cv.put(DbManagerValue.colUploadStatus, UPLOAD_NONE);

		DbRepositoryValue.getInstance().getDB().update(DbManagerValue.mFormValue, cv, null, null);
		DbRepositoryValue.getInstance().close();
	}

}
