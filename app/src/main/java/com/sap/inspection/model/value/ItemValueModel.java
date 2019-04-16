package com.sap.inspection.model.value;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Debug;
import android.os.Parcel;
import android.support.annotation.NonNull;

import com.sap.inspection.MyApplication;
import com.sap.inspection.manager.ItemUploadManager;
import com.sap.inspection.model.BaseModel;
import com.sap.inspection.model.DbManager;
import com.sap.inspection.model.DbRepository;
import com.sap.inspection.tools.DebugLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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

	public static final int UNSPECIFIED = -1;

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

	// STP only
	public String wargaId;
	public String barangId;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
	}

	public static void deleteAll(){

		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
		String sql = "DELETE FROM " + DbManagerValue.mFormValue;
		SQLiteStatement stmt = DbRepositoryValue.getInstance().getDB().compileStatement(sql);
		stmt.executeUpdateDelete();
		stmt.close();
		DbRepositoryValue.getInstance().close();

	}

	public static void delete(String scheduleId, int itemId, int operatorId){

		delete(scheduleId, itemId, operatorId, null, null);
	}

	public static void delete(String scheduleId, int itemId, int operatorId, String wargaId, String barangId) {

		DebugLog.d("(scheduleId, itemid, operatorid, wargaid, barangid) = (" + scheduleId + ", " + itemId + ", " + operatorId + ", " + wargaId + ", " + barangId + ")");
		String whereItemId = itemId != UNSPECIFIED ? " AND " + DbManagerValue.colItemId + "=" + itemId : "";				// if itemid is unspecified
		String whereOperatorId = operatorId != UNSPECIFIED ? " AND " + DbManagerValue.colOperatorId + "=" + operatorId : ""; // if operatorid is unspecified
		String whereWarga = wargaId != null ? " AND " + DbManagerValue.colWargaId + "= '" + wargaId + "'" : "";
		String whereBarang = barangId != null ? " AND " + DbManagerValue.colBarangId + "= '" + barangId + "'" : "";

		DebugLog.d("delete item(s) with scheduleid = " + scheduleId + whereWarga + whereBarang + whereItemId + whereOperatorId);

		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
		String sql = "DELETE FROM " + DbManagerValue.mFormValue + " WHERE " + DbManagerValue.colScheduleId + "=" + scheduleId + whereItemId + whereOperatorId + whereWarga + whereBarang;
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

	public static ArrayList<ItemValueModel> getAllItemValueByScheduleId (String scheduleId) {

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

	public static int countTaskDone(String scheduleId, int rowId){

		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
		Cursor mCount= DbRepositoryValue.getInstance().getDB().rawQuery("select count(*) from "+DbManagerValue.mFormValue+" where "+DbManagerValue.colScheduleId+"='" + scheduleId + "' and "+DbManagerValue.colRowId+"='" + rowId +"'", null);
		mCount.moveToFirst();
		int count= mCount.getInt(0);
		mCount.close();
		DbRepositoryValue.getInstance().close();
		return count;
	}

	public static ItemValueModel getItemValue(String scheduleId, int itemId, int operatorId) {

		return getItemValue(scheduleId, itemId, operatorId, null, null);

	}

	public static ItemValueModel getItemValue(String scheduleId, int itemId, int operatorId, String wargaId, String barangId) {

		String table = DbManagerValue.mFormValue;
		String[] columns = null;
		String wherescheduleid  = DbManagerValue.colScheduleId + "=?";
		String whereitemid   	= itemId > 0 ? " AND " + DbManagerValue.colItemId + "=?" : "";
		String whereoperatorid  = operatorId > 0 ? " AND " + DbManagerValue.colOperatorId + "=?" : "";
		String wherewargaid  	= wargaId != null ? " AND " + DbManagerValue.colWargaId + "=?" : "";
		String wherebarangid 	= barangId != null ? " AND " + DbManagerValue.colBarangId + "=?" : "";

		String where = wherescheduleid + whereitemid + whereoperatorid + wherewargaid + wherebarangid;
		DebugLog.d("Get item(s) by (" + scheduleId + "," + itemId + "," + operatorId + "," + wargaId + "," + barangId +")");

		List<String> argsList = new ArrayList<>();

		if (scheduleId != null)
			argsList.add(scheduleId);
		if (itemId > 0)
			argsList.add(String.valueOf(itemId));
		if (operatorId > 0)
			argsList.add(String.valueOf(operatorId));
		if (wargaId != null)
			argsList.add(wargaId);
		if (barangId != null)
			argsList.add(barangId);

		String[] args = new String[argsList.size()];
		args = argsList.toArray(args);

		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
		Cursor cursor = DbRepositoryValue.getInstance().getDB().query(true, table, columns, where, args, null, null,null, null);;

		if (!cursor.moveToFirst()) {

			cursor.close();
			DbRepositoryValue.getInstance().close();
			return null;
		}

		ItemValueModel model = getSiteFromCursor(cursor);

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

	public static ArrayList<ItemValueModel> getItemValuesForUpload(String scheduleId) {

		return getItemValuesForUpload(scheduleId, null, null);
	}

	public static ArrayList<ItemValueModel> getItemValuesForUpload(String scheduleId, String wargaId, String barangId) {

		String table = DbManagerValue.mFormValue;
		String[] columns = null;
		String wherescheduleid  = scheduleId != null ? DbManagerValue.colScheduleId + "=?" : "";
		String wherewargaid  	= wargaId != null ? " AND " + DbManagerValue.colWargaId + "=?" : "";
		String wherebarangid 	= barangId != null ? " AND " + DbManagerValue.colBarangId + "=?" : "";

		String where = wherescheduleid + wherewargaid + wherebarangid;
		DebugLog.d("Get item(s) by (" + scheduleId + "," + wargaId + "," + barangId +")");

		List<String> argsList = new ArrayList<>();

		if (scheduleId != null)
			argsList.add(scheduleId);
		if (wargaId != null)
			argsList.add(wargaId);
		if (barangId != null)
			argsList.add(barangId);

		String[] args = new String[argsList.size()];
		args = argsList.toArray(args);

		Cursor cursor;

		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
		cursor = DbRepositoryValue.getInstance().getDB().query(true, table, columns, where, args, null, null,null, null);

		if (!cursor.moveToFirst()){
			cursor.close();
			DbRepositoryValue.getInstance().close();
			return null;
		}

		ArrayList<ItemValueModel> model = new ArrayList<>();

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

		log("saving value on itemvaluemodel");
		log("row id : "+  rowId);
		log("schedule Id : "+  scheduleId);
		log("operator id : "+  operatorId);
		log("item id : " + itemId);
		log("warga id : " + wargaId);
		log("barang id : " + barangId);
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
				stmt.executeInsert();
				stmt.close();
				DbRepositoryValue.getInstance().close();

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
				bindAndCheckNullString(stmt, 14, photoDate);
				stmt.executeInsert();
				stmt.close();
				DbRepositoryValue.getInstance().close();

				break;
			}
			case 9 : {
				sql = String.format("INSERT OR REPLACE INTO %s(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
						DbManagerValue.mFormValue , DbManagerValue.colScheduleId,
						DbManagerValue.colItemId,DbManagerValue.colValue,
						DbManagerValue.colIsPhoto,DbManagerValue.colOperatorId,
						DbManagerValue.colRowId, DbManagerValue.colRemark,
						DbManagerValue.colLatitude, DbManagerValue.colLongitude,
						DbManagerValue.colPhotoStatus,DbManagerValue.colGPSAccuracy,
						DbManagerValue.colUploadStatus,DbManagerValue.colCreatedAt,
						DbManagerValue.colPhotoDate,DbManagerValue.colWargaId,
						DbManagerValue.colBarangId);

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
				bindAndCheckNullString(stmt, 14, photoDate);
				bindAndCheckNullString(stmt, 15, wargaId);
				bindAndCheckNullString(stmt, 16, barangId);
				stmt.executeInsert();
				stmt.close();
				DbRepositoryValue.getInstance().close();
				break;
			}
			default:
				break;
		}

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


	private static ItemValueModel getSiteFromCursor(Cursor c) {
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
			case 9 : {
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
				FormValueModel.wargaId	 = (c.getString(c.getColumnIndex(DbManagerValue.colWargaId)));
				FormValueModel.barangId	 = (c.getString(c.getColumnIndex(DbManagerValue.colBarangId)));
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
			case 9 : {
				createTable = "create table if not exists " + DbManagerValue.mFormValue
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
						+ DbManagerValue.colPhotoDate + " varchar, "
						+ DbManagerValue.colWargaId + " varchar, "
						+ DbManagerValue.colBarangId + " varchar,"
						+ "PRIMARY KEY (" + DbManagerValue.colScheduleId + ","+ DbManagerValue.colItemId + ","+ DbManagerValue.colOperatorId
						//+ "," + DbManagerValue.colWargaId + "," + DbManagerValue.colBarangId
						+ "))";
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

	// update wargaid by scheduleid
	public static void updateWargaId(String scheduleId, String oldWargaId, String newWargaId) {

		DebugLog.d("update values scheduleid (oldWargaId, NewWargaId) : " + scheduleId + "(" + oldWargaId + "," + newWargaId + ")");

		String where  = DbManagerValue.colScheduleId + "=?" + " AND " +  DbManagerValue.colWargaId + "=?";
		String[] args = new String[] {scheduleId, oldWargaId};

		DbRepositoryValue.getInstance().open(MyApplication.getInstance());

		ContentValues cv = new ContentValues();
		cv.put(DbManagerValue.colWargaId, newWargaId);

		DbRepositoryValue.getInstance().getDB().update(DbManagerValue.mFormValue, cv, where, args);
		DbRepositoryValue.getInstance().close();
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
