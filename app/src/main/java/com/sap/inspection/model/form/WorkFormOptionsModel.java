package com.sap.inspection.model.form;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.os.Parcel;

import com.sap.inspection.TowerApplication;
import com.sap.inspection.model.BaseModel;
import com.sap.inspection.model.DbManager;
import com.sap.inspection.model.DbRepository;

import java.util.Vector;

public class WorkFormOptionsModel extends BaseModel {

	public int id;
	public String label;
	public int work_form_group_item_id;
	public String value;
	public String created_at;
	public String updated_at;

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		// TODO Auto-generated method stub
	}

	public static String createDB(){
		return "create table if not exists " + DbManager.mWorkFormOption
				+ " (" + DbManager.colID + " integer, "
				+ DbManager.colValue + " varchar, "
				+ DbManager.colWorkFormItemId + " integer, "
				+ DbManager.colLable + " varchar, "
				+ DbManager.colCreatedAt + " varchar, "
				+ DbManager.colUpdatedAt + " varchar, "
				+ "PRIMARY KEY (" + DbManager.colID + "))";
	}

	public void save(Context context){
		save();
	}

	public static void delete(Context ctx){

		DbRepository.getInstance().open(TowerApplication.getInstance());
		String sql = "DELETE FROM " + DbManager.mWorkFormOption;
		SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);
		stmt.executeUpdateDelete();
		stmt.close();
		DbRepository.getInstance().close();
	}

	public void save(){

		String sql = String
				.format("INSERT OR REPLACE INTO %s(%s,%s,%s,%s,%s,%s) VALUES(?,?,?,?,?,?)",
						DbManager.mWorkFormOption , DbManager.colID,
						DbManager.colValue,DbManager.colLable,
						DbManager.colWorkFormItemId,DbManager.colCreatedAt,
						DbManager.colUpdatedAt);

		DbRepository.getInstance().open(TowerApplication.getInstance());
		SQLiteStatement stmt = DbRepository.getInstance().getDB()
				.compileStatement(sql);

		stmt.bindLong(1, id);
		bindAndCheckNullString(stmt, 2, value);
		bindAndCheckNullString(stmt, 3, label);
		stmt.bindLong(4, work_form_group_item_id);
		bindAndCheckNullString(stmt, 5, created_at);
		bindAndCheckNullString(stmt, 6, updated_at);

		stmt.executeInsert();
		stmt.close();
		DbRepository.getInstance().close();
	}

	public Vector<WorkFormOptionsModel> getAllItemByWorkFormItemId(Context context, int workFormRowColumnId) {

		//DbRepository.getInstance().open(context);
		Vector<WorkFormOptionsModel> result = getAllItemByWorkFormItemId(workFormRowColumnId);
		//DbRepository.getInstance().close();
		return result;
	}

	public static Vector<WorkFormOptionsModel> getAllItemByWorkFormItemId(int workFormItemId) {

		Vector<WorkFormOptionsModel> result = new Vector<WorkFormOptionsModel>();

		String table = DbManager.mWorkFormOption;
		String[] columns = null;
		String where =DbManager.colWorkFormItemId + "=?";
		String[] args = new String[] {String.valueOf(workFormItemId)};
		String order = null;
		Cursor cursor;

		DbRepository.getInstance().open(TowerApplication.getInstance());
		cursor = DbRepository.getInstance().getDB().query(table, columns, where, args, null, null, order, null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			DbRepository.getInstance().close();
			return result;
		}
		do {
			result.add(getItemFromCursor(cursor));
		} while(cursor.moveToNext());

		cursor.close();
		DbRepository.getInstance().close();
		return result;
	}

	//	public Vector<WorkFormItemModel> getAllItemByWorkFormId(Context context, String workFormId) {
	//
	//		DbRepository.getInstance().open(context);
	//		Vector<WorkFormItemModel> result = new Vector<WorkFormItemModel>();
	//
	//		String table = DbManager.mWorkFormItem;
	//		String[] columns = null;
	//		String where =DbManager.colWorkFormGroupId + "=?";
	//		String[] args = new String[] {workFormId};
	//		String order = DbManager.colPosition+" ASC";
	//		Cursor cursor;
	//
	//		cursor = DbRepository.getInstance().getDB().query(table, columns, where, args, null, null, order, null);
	//
	//		if (!cursor.moveToFirst())
	//			return result;
	//		do {
	//			result.add(getItemFromCursor(cursor));
	//		} while(cursor.moveToNext());
	//
	//		cursor.close();
	//		DbRepository.getInstance().close();
	//
	//		return result;
	//	}

	private static WorkFormOptionsModel getItemFromCursor(Cursor c) {
		WorkFormOptionsModel item= new WorkFormOptionsModel();

		if (null == c)
			return item;

		item.id = (c.getInt(c.getColumnIndex(DbManager.colID)));
		item.value = (c.getString(c.getColumnIndex(DbManager.colValue)));
		item.work_form_group_item_id = (c.getInt(c.getColumnIndex(DbManager.colWorkFormItemId)));
		item.label = (c.getString(c.getColumnIndex(DbManager.colLable)));
		item.created_at = (c.getString(c.getColumnIndex(DbManager.colCreatedAt)));
		item.updated_at = (c.getString(c.getColumnIndex(DbManager.colUpdatedAt)));

		return item;
	}

}
