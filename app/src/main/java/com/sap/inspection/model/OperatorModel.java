package com.sap.inspection.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.sap.inspection.TowerApplication;
import com.sap.inspection.tools.DebugLog;

import org.parceler.Parcel;

import java.util.ArrayList;

@Parcel
public class OperatorModel extends BaseModel {
	
    public int id;
    public ArrayList<Integer> without_form_item_ids;
    public ArrayList<Integer> corrective_item_ids;
    public String name;

	public OperatorModel() {}

	public void save(Context context){


		save();

	}
	
	public OperatorModel getOperatorById(Context context,int id) {

		OperatorModel model = null;
		model = getOperatorById(id);

		return model;
	}
	
	public static OperatorModel getOperatorById(int id) {

		OperatorModel model = null;

		String table = DbManager.mOperator;
		String[] columns = null;
		String where =DbManager.colID+"=?";
		String[] args = new String[]{String.valueOf(id)};
		Cursor cursor;

		DbRepository.getInstance().open(TowerApplication.getInstance());
		cursor = DbRepository.getInstance().getDB().query(true, table, columns, where, args, null, null,null, null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			DbRepository.getInstance().close();
			return model;
		}

		model = getOperatorFromCursor(cursor);

		cursor.close();
		DbRepository.getInstance().close();
		return model;
	}
	
	public void save(){

		DebugLog.d("id="+id+" name="+name);
		String sql = String.format("INSERT OR REPLACE INTO %s(%s,%s) VALUES(?,?)",
						DbManager.mOperator , DbManager.colID,
						DbManager.colName);


		DbRepository.getInstance().open(TowerApplication.getInstance());

		SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);

		stmt.bindLong(1, id);
		bindAndCheckNullString(stmt, 2, name);

		stmt.executeInsert();
		stmt.close();
		DbRepository.getInstance().close();
	}

	public static void delete(Context ctx){

		DbRepository.getInstance().open(TowerApplication.getInstance());
		String sql = "DELETE FROM " + DbManager.mOperator;
		SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);
		stmt.executeUpdateDelete();
		stmt.close();
		DbRepository.getInstance().close();
	}

	private static OperatorModel getOperatorFromCursor(Cursor c) {
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
