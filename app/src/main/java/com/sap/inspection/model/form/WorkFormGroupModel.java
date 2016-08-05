package com.sap.inspection.model.form;

import java.util.Vector;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.os.Parcel;

import com.sap.inspection.model.BaseModel;
import com.sap.inspection.model.DbManager;
import com.sap.inspection.model.DbRepository;

public class WorkFormGroupModel extends BaseModel {
	
    public int id;
    public String name;
    public int position;
    public int work_form_id;
    public String description;
    public TableModel table;
    public String ancestry;
    public String created_at;
    public String updated_at;
    private int input = -1;

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
		return "create table if not exists " + DbManager.mWorkFormGroup
				+ " (" + DbManager.colID + " integer, "
				+ DbManager.colName + " varchar, "
				+ DbManager.colPosition + " integer, "
				+ DbManager.colWorkFormId + " integer, "
				+ DbManager.colDescription + " varchar, "
				+ DbManager.colAncestry + " varchar, "
				+ DbManager.colCreatedAt + " varchar, "
				+ DbManager.colUpdatedAt + " varchar, "
				+ DbManager.colSumInput + " integer, "
				+ "PRIMARY KEY (" + DbManager.colID + "))";
	}
	
	private int countInput(int groupId) {

		String table = DbManager.mWorkFormItem;
		String[] columns = null;
		String where =DbManager.colWorkFormGroupId+" = ? AND "+DbManager.colFieldType+" != 'label'";
		String[] args = new String[] {String.valueOf(id)};
		String order = null;

		Cursor cursor = DbRepository.getInstance().getDB().query(table, columns, where, args, null, null, order, null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			return 0;
		}

		int temp = cursor.getCount();
		cursor.close();
		return temp;
	}

	
	public void save(Context context){
		DbRepository.getInstance().open(context);
		save();
		DbRepository.getInstance().close();
	}

	public void save(){
		if (table != null)
			table.save();
		input = countInput(id);
		String sql = String
				.format("INSERT OR REPLACE INTO %s(%s,%s,%s,%s,%s,%s,%s,%s,%s) VALUES(?,?,?,?,?,?,?,?,?)",
						DbManager.mWorkFormGroup, DbManager.colID,
						DbManager.colName, DbManager.colPosition, 
						DbManager.colWorkFormId, DbManager.colDescription,
						DbManager.colAncestry, DbManager.colCreatedAt,
						DbManager.colUpdatedAt,DbManager.colSumInput);
		SQLiteStatement stmt = DbRepository.getInstance().getDB()
				.compileStatement(sql);

		stmt.bindLong(1, id);
		bindAndCheckNullString(stmt, 2, name);
		stmt.bindLong(3, position);
		stmt.bindLong(4, work_form_id);
		bindAndCheckNullString(stmt, 5, description);
		bindAndCheckNullString(stmt, 6, ancestry);
		bindAndCheckNullString(stmt, 7, created_at);
		bindAndCheckNullString(stmt, 8, updated_at);
		stmt.bindLong(9, input);

		stmt.executeInsert();
		stmt.close();
	}

	public static void delete(Context ctx){
		DbRepository.getInstance().open(ctx);
		String sql = "DELETE FROM " + DbManager.mWorkFormGroup;
		SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);
		stmt.executeUpdateDelete();
		stmt.close();
		DbRepository.getInstance().close();
	}

	public int getInput(int groupId) {
		if (input == -1)
			countInput(groupId);
		return input;
	}

	public Vector<WorkFormGroupModel> getAllItemByWorkFormGroupId(Context context, int workFormId) {
		DbRepository.getInstance().open(context);
		Vector<WorkFormGroupModel> result = getAllItemByWorkFormId(workFormId);
		DbRepository.getInstance().close();
		return result;
	}

	public Vector<WorkFormGroupModel> getAllItemByWorkFormId(int workFormId) {
		Vector<WorkFormGroupModel> result = new Vector<WorkFormGroupModel>();

		String table = DbManager.mWorkFormGroup;
		String[] columns = null;
		String where =DbManager.colWorkFormId + "=?";
		String[] args = new String[] {String.valueOf(workFormId)};
		String order = DbManager.colID+" ASC";

		Cursor cursor = DbRepository.getInstance().getDB().query(table, columns, where, args, null, null, order, null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			return result;
		}
		do {
			result.add(getFormFromCursor(cursor));
		} while(cursor.moveToNext());

		cursor.close();

		return result;
	}

	private WorkFormGroupModel getFormFromCursor(Cursor c) {
		WorkFormGroupModel item= new WorkFormGroupModel();

		if (null == c)
			return item;

		item.id = (c.getInt(c.getColumnIndex(DbManager.colID)));
		item.name = (c.getString(c.getColumnIndex(DbManager.colName)));
		item.position = (c.getInt(c.getColumnIndex(DbManager.colPosition)));
		item.created_at = (c.getString(c.getColumnIndex(DbManager.colCreatedAt)));
		item.updated_at = (c.getString(c.getColumnIndex(DbManager.colUpdatedAt)));

		return item;
	}
}
