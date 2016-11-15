package com.sap.inspection.model.value;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.os.Parcel;

import com.sap.inspection.MyApplication;
import com.sap.inspection.manager.ItemUploadManager;
import com.sap.inspection.model.BaseModel;
import com.sap.inspection.model.DbManager;
import com.sap.inspection.tools.DateTools;
import com.sap.inspection.tools.DebugLog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


public class ItemValueModel extends BaseModel {
	public static final int UPLOAD_NONE = 0;
	public static final int UPLOAD_ONGOING = 1;
	public static final int UPLOAD_DONE = 2;

	public String scheduleId;
	public int operatorId;
	public int itemId;
	public int siteId;
	public String createdAt;
	public String value;
	public String picture_updated_at;
	public int rowId;
	public int gpsAccuracy;
	public String remark;
	public String material_request;
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
		DbRepositoryValue.getInstance().open(ctx);
		delete(scheduleId, itemId, operatorId);
		DbRepositoryValue.getInstance().close();
	}
	
	public static void deleteAll(Context ctx){
		DbRepositoryValue.getInstance().open(ctx);
		String sql = "DELETE FROM " + DbManagerValue.mFormValue;
		SQLiteStatement stmt = DbRepositoryValue.getInstance().getDB().compileStatement(sql);
		stmt.executeUpdateDelete();
		stmt.close();
		DbRepositoryValue.getInstance().close();
	}

	public static void delete(String scheduleId, int itemId, int operatorId){
		String sql = "DELETE FROM " + DbManagerValue.mFormValue + " WHERE "+DbManagerValue.colScheduleId+"="+scheduleId+" AND "+DbManagerValue.colItemId+"="+itemId+" AND "+DbManagerValue.colOperatorId+"="+operatorId;
		SQLiteStatement stmt = DbRepositoryValue.getInstance().getDB().compileStatement(sql);
		stmt.executeUpdateDelete();
		stmt.close();
	}

	public ItemValueModel getItemValue(Context context,String scheduleId, int itemId, int operatorId) {
		ItemValueModel model = null;
		DbRepositoryValue.getInstance().open(context);
		model = getItemValue(scheduleId, itemId, operatorId);
		DbRepositoryValue.getInstance().close();
		return model;
	}

	public static int countTaskDone(String scheduleId, int rowId){
		Cursor mCount= DbRepositoryValue.getInstance().getDB().rawQuery("select count(*) from "+DbManagerValue.mFormValue+" where "+DbManagerValue.colScheduleId+"='" + scheduleId + "' and "+DbManagerValue.colRowId+"='" + rowId +"'", null);
		mCount.moveToFirst();
		int count= mCount.getInt(0);
		mCount.close();
		return count;
	}

	public ItemValueModel getItemValue(String scheduleId,int itemId, int operatorId) {
		ItemValueModel model = null;
		String table = DbManagerValue.mFormValue;
		String[] columns = null;
		String where =DbManagerValue.colScheduleId+"=? AND "+DbManagerValue.colItemId+"=? AND "+DbManagerValue.colOperatorId+"=?";
		String[] args = new String[]{scheduleId,String.valueOf(itemId),String.valueOf(operatorId)};
		Cursor cursor;

		if (!DbRepositoryValue.getInstance().getDB().isOpen())
			DbRepositoryValue.getInstance().open(MyApplication.getInstance());

		cursor = DbRepositoryValue.getInstance().getDB().query(true, table, columns, where, args, null, null,null, null);

		if (!cursor.moveToFirst())
			return model;

		model = getSiteFromCursor(cursor);

		cursor.close();
		return model;
	}
	
	public ItemValueModel getItemValue(int itemId, int operatorId, String userName) {
		ItemValueModel model = null;
//		String table = DbManagerValue.mFormValue;
//		String[] columns = null;
//		String where =DbManagerValue.colItemId+"=? AND "+DbManagerValue.colOperatorId+"=?";
//		String[] args = new String[]{scheduleId,String.valueOf(itemId),String.valueOf(operatorId)};
//		String order = DbManagerValue.colWorkTypeId+" ASC";
//		Cursor cursor;
//
//		cursor = DbRepositoryValue.getInstance().getDB().query(true, table, columns, where, args, null, null,null, null);
		
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
		    return model;
		}

		if (!cursor.moveToFirst()) {
			cursor.close();
			return model;
		}

		model = getSiteFromCursor(cursor);

		cursor.close();
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

		cursor = DbRepositoryValue.getInstance().getDB().query(true, table, columns, where, args, null, null,null, null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			return model;
		}
		do{
			model.add(getSiteFromCursor(cursor));
		}	
		while(cursor.moveToNext());
		cursor.close();
		return model;
	}

    public ArrayList<ItemValueModel> getItemValuesForUpload(String scheduleId) {
        ArrayList<ItemValueModel> model = new ArrayList<ItemValueModel>();
        String table = DbManagerValue.mFormValue;
        String[] columns = null;
        String where = null;
        String[] args = null;
		DebugLog.d("get itemvalues for " + scheduleId);

        if (ItemUploadManager.getInstance().isRunning()){
            where =DbManagerValue.colUploadStatus+"!=? AND "+DbManagerValue.colScheduleId+"=?";
            args = new String[]{String.valueOf(UPLOAD_ONGOING), scheduleId};
        }else{
            where = DbManagerValue.colScheduleId+"=?";
            args = new String[]{scheduleId};
        }
		DebugLog.d(where);

        Cursor cursor;

        cursor = DbRepositoryValue.getInstance().getDB().query(true, table, columns, where, args, null, null,null, null);

        if (!cursor.moveToFirst()){
			DebugLog.d("model null ");
			cursor.close();
            return model;
        }
        do{
            model.add(getSiteFromCursor(cursor));
			DebugLog.d("" + model.get(model.size() - 1).value);
        }
        while(cursor.moveToNext());
		DebugLog.d("model size "+model.size());
        cursor.close();
        return model;
    }

	public void save(Context context){
		DbRepositoryValue.getInstance().open(context);
		save();
		DbRepositoryValue.getInstance().close();
	}

	public void save(){
		DebugLog.d("saving value on itemvaluemodel");
		DebugLog.d("row id : "+  rowId);
		DebugLog.d("schedule Id : "+  scheduleId);
		DebugLog.d("operator id : "+  operatorId);
		DebugLog.d("item id : " + itemId);
		DebugLog.d("------------------------------------");
		save(scheduleId, photoStatus);
	}

	public void saveForCorrective(){
		save(scheduleId+"-"+photoStatus, photoStatus);
	}  

	public void save(String scheduleId, String photoStatus){
		String sql = String.format("INSERT OR REPLACE INTO %s(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
				DbManagerValue.mFormValue , DbManagerValue.colScheduleId,
				DbManagerValue.colItemId,DbManagerValue.colValue,
				DbManagerValue.colIsPhoto,DbManagerValue.colOperatorId,
				DbManagerValue.colRowId, DbManagerValue.colRemark,
				DbManagerValue.colLatitude, DbManagerValue.colLongitude,
				DbManagerValue.colPhotoStatus,DbManagerValue.colGPSAccuracy,
				DbManagerValue.colUploadStatus,DbManagerValue.colCreatedAt,
				DbManagerValue.colDisable,DbManagerValue.material_request);

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
		bindBooleanToInteger(stmt, 13, disable);
		bindAndCheckNullString(stmt,14, material_request);


		DebugLog.d("photo Date="+createdAt);
		bindAndCheckNullString(stmt, 13, createdAt);

		DebugLog.d("scheduleId="+scheduleId+" itemId="+itemId+" value="+value+
				" typePhoto="+typePhoto+" operatorId="+operatorId+" rowId="+rowId+
				" remark="+remark+ " material_request="+material_request+ " latitude="+latitude+" longitude="+longitude+
				" photoStatus="+photoStatus+" gpsAccuracy="+gpsAccuracy+
				" uploadStatus="+uploadStatus+" createdAt="+createdAt+" disable="+disable);

		stmt.executeInsert();
		stmt.close();
	}

	private String getPhotoLastModified() {
		try {
			DebugLog.d("value="+value);
			File file = new File(value);
			if(file.exists()) {
				long date = file.lastModified();
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(date);
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm", Locale.getDefault());
				return  simpleDateFormat.format(calendar.getTime());
			} else {
				return DateTools.getCurrentDate();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return DateTools.getCurrentDate();
		}
	}

	private ItemValueModel getSiteFromCursor(Cursor c) {
		ItemValueModel FormValueModel = null;

		if (null == c)
			return FormValueModel;

		FormValueModel = new ItemValueModel();
		FormValueModel.scheduleId = (c.getString(c.getColumnIndex(DbManagerValue.colScheduleId)));
		FormValueModel.operatorId = (c.getInt(c.getColumnIndex(DbManagerValue.colOperatorId)));
		FormValueModel.itemId = (c.getInt(c.getColumnIndex(DbManagerValue.colItemId)));
		FormValueModel.rowId = (c.getInt(c.getColumnIndex(DbManagerValue.colRowId)));
		FormValueModel.gpsAccuracy = (c.getInt(c.getColumnIndex(DbManagerValue.colGPSAccuracy)));
		FormValueModel.remark = (c.getString(c.getColumnIndex(DbManagerValue.colRemark)));
		FormValueModel.material_request= (c.getString(c.getColumnIndex(DbManagerValue.material_request)));
		FormValueModel.latitude = (c.getString(c.getColumnIndex(DbManagerValue.colLatitude)));
		FormValueModel.longitude = (c.getString(c.getColumnIndex(DbManagerValue.colLongitude)));
		FormValueModel.photoStatus = (c.getString(c.getColumnIndex(DbManagerValue.colPhotoStatus)));
		FormValueModel.value = (c.getString(c.getColumnIndex(DbManagerValue.colValue)));
		FormValueModel.uploadStatus = (c.getInt(c.getColumnIndex(DbManagerValue.colUploadStatus)));
		FormValueModel.typePhoto = c.getInt(c.getColumnIndex(DbManagerValue.colIsPhoto)) == 1;
		FormValueModel.createdAt = (c.getString(c.getColumnIndex(DbManagerValue.colCreatedAt)));
		FormValueModel.disable = c.getInt(c.getColumnIndex(DbManagerValue.colDisable)) == 1;
		return FormValueModel;
	}

	public static String createDB(){
		return "create table if not exists " + DbManagerValue.mFormValue
				+ " ("+ DbManagerValue.colScheduleId + " integer, "
				+ DbManagerValue.colOperatorId + " integer, "
				+ DbManagerValue.colItemId + " integer, "
				+ DbManagerValue.colSiteId + " integer, "
				+ DbManagerValue.colGPSAccuracy + " integer, "
				+ DbManagerValue.colRowId + " integer, "
				+ DbManagerValue.colRemark + " varchar, "
				+ DbManagerValue.material_request + " varchar, "
				+ DbManagerValue.colPhotoStatus + " varchar, "
				+ DbManagerValue.colLatitude + " varchar, "
				+ DbManagerValue.colLongitude + " varchar, "
				+ DbManagerValue.colValue + " varchar, "
				+ DbManagerValue.colUploadStatus + " integer, "
				+ DbManagerValue.colIsPhoto + " integer, "
				+ DbManagerValue.colCreatedAt + " varchar, "
				+ DbManagerValue.colDisable + " integer, "
				+ "PRIMARY KEY (" + DbManagerValue.colScheduleId + ","+ DbManagerValue.colItemId + ","+ DbManagerValue.colOperatorId + "))";
	}
	
	public static void resetAllUploadStatus(){
		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
		ContentValues cv = new ContentValues();
		cv.put(DbManagerValue.colUploadStatus, UPLOAD_NONE);

		DbRepositoryValue.getInstance().getDB().update(DbManagerValue.mFormValue, cv, null, null);
		DbRepositoryValue.getInstance().close();
	}

}
