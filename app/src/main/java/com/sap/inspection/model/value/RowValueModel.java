package com.sap.inspection.model.value;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.os.Parcel;

import com.sap.inspection.model.BaseModel;


public class RowValueModel extends BaseModel {

	public String siteId;
	public String workTypeId;
	public String day_date;
	public String rowId;
	public String progress;
	public int sumTask;
	public int filled;
	public int uploaded;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
	}

	public void save(Context context){
		DbRepositoryValue.getInstance().open(context);
		save();
		DbRepositoryValue.getInstance().close();
	}

	public RowValueModel getSiteById(Context context,String scheduleId, String itemId) {
		RowValueModel model = null;
		DbRepositoryValue.getInstance().open(context);
		model = getSiteById(scheduleId, itemId);
		DbRepositoryValue.getInstance().close();
		return model;
	}

	public RowValueModel getSiteById(String scheduleId,String itemId) {
		RowValueModel model = null;

		String table = DbManagerValue.mFormValue;
		String[] columns = null;
		String where =DbManagerValue.colScheduleId+"=? AND "+DbManagerValue.colItemId+"=?";
		String[] args = new String[]{scheduleId,itemId};
		Cursor cursor;

		cursor = DbRepositoryValue.getInstance().getDB().query(true, table, columns, where, args, null, null,null, null);

		if (!cursor.moveToFirst())
			return model;

		model = getSiteFromCursor(cursor);

		cursor.close();
		return model;
	}

	public void save(){
		String sql = String.format("INSERT OR REPLACE INTO %s(%s,%s,%s,%s,%s,%s,%s,%s) VALUES(?,?,?,?,?,?,?,?)",
				DbManagerValue.mRowValue , DbManagerValue.colSiteId,
				DbManagerValue.colWorkTypeId,DbManagerValue.colDayDate,
				DbManagerValue.colRowId, DbManagerValue.colprogress,
				DbManagerValue.colSumTask, DbManagerValue.colSumFilled,
				DbManagerValue.colUploaded);

		SQLiteStatement stmt = DbRepositoryValue.getInstance().getDB().compileStatement(sql);

		bindAndCheckNullString(stmt, 1, siteId);
		bindAndCheckNullString(stmt, 2, workTypeId);
		bindAndCheckNullString(stmt, 3, day_date);
		bindAndCheckNullString(stmt, 4, rowId);
		bindAndCheckNullString(stmt, 5, progress);;

		stmt.executeInsert();
		stmt.close();
	}

	private RowValueModel getSiteFromCursor(Cursor c) {
		RowValueModel FormValueModel = null;

		if (null == c)
			return FormValueModel;

//		FormValueModel = new RowValueModel();
//		FormValueModel.scheduleId = (c.getString(c.getColumnIndex(DbManagerValue.colScheduleId)));
//		FormValueModel.itemId = (c.getString(c.getColumnIndex(DbManagerValue.colItemId)));
//		FormValueModel.value = (c.getString(c.getColumnIndex(DbManagerValue.colValue)));
//		FormValueModel.typePhoto = c.getInt(c.getColumnIndex(DbManagerValue.colIsPhoto)) == 1 ? true : false;

		return FormValueModel;
	}

	public static String createDB(){
		return "create table if not exists " + DbManagerValue.mFormValue
				+ " ("+ DbManagerValue.colScheduleId + " varchar, "
				+ DbManagerValue.colItemId + " varchar, "
				+ DbManagerValue.colValue + " varchar, "
				+ DbManagerValue.colIsPhoto + " integer, "
				+ "PRIMARY KEY (" + DbManagerValue.colScheduleId + ","+ DbManagerValue.colItemId + "))";
	}

}
