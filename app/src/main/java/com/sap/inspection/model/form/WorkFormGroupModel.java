package com.sap.inspection.model.form;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.sap.inspection.TowerApplication;
import com.sap.inspection.model.BaseModel;
import com.sap.inspection.model.DbManager;
import com.sap.inspection.model.DbRepository;

import org.parceler.Parcel;

import java.util.Vector;

@Parcel
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
    public int input = -1;

    public WorkFormGroupModel() {}

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

		DbRepository.getInstance().open(TowerApplication.getInstance());
		Cursor cursor = DbRepository.getInstance().getDB().query(table, columns, where, args, null, null, order, null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			DbRepository.getInstance().close();
			return 0;
		}

		int temp = cursor.getCount();
		cursor.close();
		DbRepository.getInstance().close();
		return temp;
	}

	
	public void save(Context context){
		save();
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

		DbRepository.getInstance().open(TowerApplication.getInstance());
		SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);

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
		DbRepository.getInstance().close();
	}

	public static void delete(Context ctx){

		DbRepository.getInstance().open(TowerApplication.getInstance());
		String sql = "DELETE FROM " + DbManager.mWorkFormGroup;
		SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);
		stmt.executeUpdateDelete();
		stmt.close();
		DbRepository.getInstance().close();
	}

	public int getInputCount(int groupId) {
		if (input == -1)
			countInput(groupId);
		return input;
	}

	public static WorkFormGroupModel getWorkFormGroupById(String workFormGroupId) {

		String table = DbManager.mWorkFormGroup;
		String[] columns = null;
		String where = DbManager.colID + "=?";
		String[] args = new String[] { workFormGroupId };

		DbRepository.getInstance().open(TowerApplication.getInstance());
		Cursor cursor = DbRepository.getInstance().getDB().query(table, columns, where, args, null, null, null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			DbRepository.getInstance().close();
			return null;
		}

		WorkFormGroupModel result = getWorkFormGroupFromCursor(cursor);
		cursor.close();
		DbRepository.getInstance().close();
		return result;

	}

	public Vector<WorkFormGroupModel> getAllItemByWorkFormGroupId(Context context, int workFormId) {
		DbRepository.getInstance().open(TowerApplication.getInstance());
		Vector<WorkFormGroupModel> result = getAllItemByWorkFormId(workFormId);
		DbRepository.getInstance().close();
		return result;
	}

	public static Vector<WorkFormGroupModel> getAllItemByWorkFormId(int workFormId) {
		Vector<WorkFormGroupModel> result = new Vector<WorkFormGroupModel>();

		String table = DbManager.mWorkFormGroup;
		String[] columns = null;
		String where =DbManager.colWorkFormId + "=?";
		String[] args = new String[] {String.valueOf(workFormId)};
		String order = DbManager.colID+" ASC";

		DbRepository.getInstance().open(TowerApplication.getInstance());
		Cursor cursor = DbRepository.getInstance().getDB().query(table, columns, where, args, null, null, order, null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			DbRepository.getInstance().close();
			return result;
		}
		do {
			result.add(getWorkFormGroupFromCursor(cursor));
		} while(cursor.moveToNext());

		cursor.close();
		DbRepository.getInstance().close();
		return result;
	}

	private static WorkFormGroupModel getWorkFormGroupFromCursor(Cursor c) {
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
