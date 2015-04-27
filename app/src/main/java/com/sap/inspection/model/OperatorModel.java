package com.sap.inspection.model;

import java.util.Vector;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.os.Parcel;

public class OperatorModel extends BaseModel {
	
    public int id;
    public Vector<Integer> without_form_item_ids;
    public Vector<Integer> corrective_item_ids;
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
	
	public OperatorModel getOperatorById(Context context,int id) {
		OperatorModel model = null;
		DbRepository.getInstance().open(context);
		model = getOperatorById(id);
		DbRepository.getInstance().close();
		return model;
	}
	
	public OperatorModel getOperatorById(int id) {
		OperatorModel model = null;

		String table = DbManager.mOperator;
		String[] columns = null;
		String where =DbManager.colID+"=?";
		String[] args = new String[]{String.valueOf(id)};
		Cursor cursor;

		cursor = DbRepository.getInstance().getDB().query(true, table, columns, where, args, null, null,null, null);

		if (!cursor.moveToFirst())
			return model;
		
		model = getOperatorFromCursor(cursor);

		cursor.close();
		return model;
	}
	
	public void save(){
		String sql = String.format("INSERT OR REPLACE INTO %s(%s,%s) VALUES(?,?)",
						DbManager.mOperator , DbManager.colID,
						DbManager.colName);
		
		SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);

		stmt.bindLong(1, id);
		bindAndCheckNullString(stmt, 2, name);

		stmt.executeInsert();
		stmt.close();
	}

	private OperatorModel getOperatorFromCursor(Cursor c) {
		OperatorModel operatorModel = null;

		if (null == c)
			return operatorModel;

		operatorModel = new OperatorModel();
		operatorModel.id = (c.getInt(c.getColumnIndex(DbManager.colID)));
		operatorModel.name = (c.getString(c.getColumnIndex(DbManager.colName)));

		return operatorModel;
	}
	
	public static String createDB(){
		return "create table if not exists " + DbManager.mOperator
				+ " (" + DbManager.colID + " integer, "
				+ DbManager.colName + " varchar, "
				+ "PRIMARY KEY (" + DbManager.colID + "))";
	}

}
