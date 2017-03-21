package com.sap.inspection.model.form;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.os.Parcel;

import com.sap.inspection.MyApplication;
import com.sap.inspection.model.BaseModel;
import com.sap.inspection.model.DbManager;
import com.sap.inspection.model.DbRepository;
import com.sap.inspection.model.OperatorModel;
import com.sap.inspection.model.SiteModel;
import com.sap.inspection.model.WorkTypeModel;
import com.sap.inspection.tools.DebugLog;

import java.util.Vector;

public class WorkFormModel extends BaseModel {
	
    public int id;
    public String name;
    public String notes;
    public int work_type_id;
    public Vector<WorkFormGroupModel> groups;
    public SiteModel site;
	public OperatorModel operator;
	public WorkTypeModel type;
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
		return "create table if not exists " + DbManager.mWorkForm
				+ " (" + DbManager.colID + " integer, "
				+ DbManager.colName + " varchar, "
				+ DbManager.colNotes + " varchar, "
				+ DbManager.colWorkTypeId + " integer, "
				+ DbManager.colSumInput + " integer, "
				+ DbManager.colCreatedAt + " varchar, "
				+ DbManager.colUpdatedAt + " varchar, "
				+ "PRIMARY KEY (" + DbManager.colID + "))";
	}
	
	public static int getTaskCount(int workTypeId){
		int result = -1;
		
		String table = DbManager.mWorkForm;
		String[] columns = new String[] {DbManager.colSumInput};
		String where =DbManager.colWorkTypeId + "=?";
		String[] args = new String[] {String.valueOf(workTypeId)};
		String order = null;

		if (!DbRepository.getInstance().getDB().isOpen())
			DbRepository.getInstance().open(MyApplication.getInstance());
		Cursor cursor = DbRepository.getInstance().getDB().query(table, columns, where, args, null, null, order, null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			return result;
		}
		result = cursor.getInt(cursor.getColumnIndex(DbManager.colSumInput));

		cursor.close();

		return result;
	}

	public void save(Context context){
		DbRepository.getInstance().open(context);
		save();
		DbRepository.getInstance().close();
	}

	public void save(){
		int count = 0;
		if (groups != null)
			for (WorkFormGroupModel group : groups) {
				group.save();
				count += group.getInput(group.id);
			}
		String sql = String
				.format("INSERT OR REPLACE INTO %s(%s,%s,%s,%s,%s,%s,%s) VALUES(?,?,?,?,?,?,?)",
						DbManager.mWorkForm, DbManager.colID,
						DbManager.colName, DbManager.colNotes,
						DbManager.colWorkTypeId,DbManager.colCreatedAt, 
						DbManager.colUpdatedAt,DbManager.colSumInput);
		SQLiteStatement stmt = DbRepository.getInstance().getDB()
				.compileStatement(sql);

		stmt.bindLong(1, id);
		bindAndCheckNullString(stmt, 2, name);
		bindAndCheckNullString(stmt, 3, notes);
		stmt.bindLong(4, work_type_id);
		bindAndCheckNullString(stmt, 5, created_at);
		bindAndCheckNullString(stmt, 6, updated_at);
		stmt.bindLong(7, count);

		stmt.executeInsert();
		stmt.close();

	}

	public static void delete(Context ctx){
		DbRepository.getInstance().open(ctx);
		String sql = "DELETE FROM " + DbManager.mWorkForm;
		SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);
		stmt.executeUpdateDelete();
		stmt.close();
		DbRepository.getInstance().close();
	}

	public int countItem() {

		String table = DbManager.mWorkForm;
		String[] columns = null;
		String where =null;
		String[] args = null;
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
	
	public WorkFormModel getFormByWorkTypeId(Context context, int workTypeId) {
		DbRepository.getInstance().open(context);
		WorkFormModel result = getItemByWorkTypeId(workTypeId);
		DbRepository.getInstance().close();
		return result;
	}
	public WorkFormModel getItemByWorkTypeId(int workTypeId) {

		WorkFormModel result = new WorkFormModel();

		String table = DbManager.mWorkForm;
		String[] columns = null;
		String where =DbManager.colWorkTypeId + "=?";
		String[] args = new String[] {String.valueOf(workTypeId)};
		String order = null;

		Cursor cursor = DbRepository.getInstance().getDB().query(table, columns, where, args, null, null, order, null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			return result;
		}
		DebugLog.d("=============cursor can move to first==================");
		result = getItemFromCursor(cursor);

		cursor.close();

		return result;
	}

	private WorkFormModel getItemFromCursor(Cursor c) {
		WorkFormModel item= new WorkFormModel();
		DebugLog.d("=============cursor can move to second==================");
		if (null == c)
			return item;
		DebugLog.d("=============cursor can move to third==================");
		item.id = (c.getInt(c.getColumnIndex(DbManager.colID)));
		item.name = (c.getString(c.getColumnIndex(DbManager.colName)));
		item.work_type_id = (c.getInt(c.getColumnIndex(DbManager.colWorkTypeId)));
		item.notes = (c.getString(c.getColumnIndex(DbManager.colNotes)));
		item.created_at = (c.getString(c.getColumnIndex(DbManager.colCreatedAt)));
		item.updated_at = (c.getString(c.getColumnIndex(DbManager.colUpdatedAt)));
		DebugLog.d("============= id =================="+item.id);
		return item;
	}
}
