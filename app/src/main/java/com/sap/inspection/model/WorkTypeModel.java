package com.sap.inspection.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.os.Parcel;

public class WorkTypeModel extends BaseModel {

	public int id;
	public String name;

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		// TODO Auto-generated method stub
	}
	
	public void save(Context context){
		DbRepository.getInstance().open(context);
		save();
		DbRepository.getInstance().close();
	}
	
	public WorkTypeModel getworkTypeById(Context context,int id) {
		WorkTypeModel model = null;
		DbRepository.getInstance().open(context);
		model = getworkTypeById(id);
		DbRepository.getInstance().close();
		return model;
	}
	
	public WorkTypeModel getworkTypeById(int id) {
		WorkTypeModel model = null;

		String table = DbManager.mWorkType;
		String[] columns = null;
		String where =DbManager.colID+"=?";
		String[] args = new String[]{String.valueOf(id)};
		Cursor cursor;

		cursor = DbRepository.getInstance().getDB().query(true, table, columns, where, args, null, null,null, null);

		if (!cursor.moveToFirst())
			return model;
		
		model = getworkTypeFromCursor(cursor);

		cursor.close();
		return model;
	}
	
	public void save(){
		String sql = String.format("INSERT OR REPLACE INTO %s(%s,%s) VALUES(?,?)",
						DbManager.mWorkType , DbManager.colID,
						DbManager.colName);
		
		SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);

		stmt.bindLong(1, id);
		bindAndCheckNullString(stmt, 2, name.toUpperCase());

		stmt.executeInsert();
		stmt.close();
	}

	private WorkTypeModel getworkTypeFromCursor(Cursor c) {
		WorkTypeModel workTypeModel = null;

		if (null == c)
			return workTypeModel;

		workTypeModel = new WorkTypeModel();
		workTypeModel.id = (c.getInt(c.getColumnIndex(DbManager.colID)));
		workTypeModel.name = (c.getString(c.getColumnIndex(DbManager.colName)));

		return workTypeModel;
	}

	public static String createDB(){
		return "create table if not exists " + DbManager.mWorkType
				+ " (" + DbManager.colID + " varchar, "
				+ DbManager.colName + " varchar, "
				+ "PRIMARY KEY (" + DbManager.colID + "))";
	}
}
