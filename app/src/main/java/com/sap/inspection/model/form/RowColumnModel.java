package com.sap.inspection.model.form;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.os.Parcel;

import com.sap.inspection.MyApplication;
import com.sap.inspection.model.BaseModel;
import com.sap.inspection.model.DbManager;
import com.sap.inspection.model.DbRepository;

import java.util.Vector;

public class RowColumnModel extends BaseModel {
	
    public int id;
    public int row_id;
    public int column_id;
    public String created_at;
    public String updated_at;
    public int work_form_group_id;
    public Vector<WorkFormItemModel> items;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
	}
	
	public static String createDB(){
		return "create table if not exists " + DbManager.mWorkFormRowCol
				+ " (" + DbManager.colID + " integer, "
				+ DbManager.colRowId + " integer, "
				+ DbManager.colColId + " integer, "
				+ DbManager.colWorkFormGroupId + " integer, "
				+ DbManager.colCreatedAt + " varchar, "
				+ DbManager.colUpdatedAt + " varchar, "
				+ "PRIMARY KEY (" + DbManager.colID + "))";
	}

	public void save(Context context){
		DbRepository.getInstance().open(context);
		save();
		DbRepository.getInstance().close();
	}

	public static void delete(Context ctx){
		DbRepository.getInstance().open(ctx);
		String sql = "DELETE FROM " + DbManager.mWorkFormRowCol;
		SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);
		stmt.executeUpdateDelete();
		stmt.close();
		DbRepository.getInstance().close();
	}

	public void save(){
		String sql = String
				.format("INSERT OR REPLACE INTO %s(%s,%s,%s,%s,%s,%s) VALUES(?,?,?,?,?,?)",
						DbManager.mWorkFormRowCol , DbManager.colID,
						DbManager.colRowId,DbManager.colColId,
						DbManager.colWorkFormGroupId, DbManager.colCreatedAt,
						DbManager.colUpdatedAt);
		SQLiteStatement stmt = DbRepository.getInstance().getDB()
				.compileStatement(sql);

		stmt.bindLong(1, id);
		stmt.bindLong(2, row_id);
		stmt.bindLong(3, column_id);
		stmt.bindLong(4, work_form_group_id);
		bindAndCheckNullString(stmt, 5, created_at);
		bindAndCheckNullString(stmt, 6, updated_at);

		stmt.executeInsert();
		stmt.close();
		
		if (items != null)
			for (WorkFormItemModel item : items) {
				item.save();
			}
	}
	
	public Vector<RowColumnModel> getAllItemByWorkFormRowId(Context context, int workFormRowId) {
		DbRepository.getInstance().open(context);
		Vector<RowColumnModel> result = getAllItemByWorkFormRowId(workFormRowId);
		DbRepository.getInstance().close();
		return result;
	}
	
	public Vector<RowColumnModel> getAllItemByWorkFormRowId(int workFormRowId) {

		Vector<RowColumnModel> result = new Vector<RowColumnModel>();

		Cursor cursor;

		String query = "SELECT t1."+DbManager.colID+",t1."+DbManager.colRowId+",t1."+DbManager.colColId+",t1."+DbManager.colWorkFormGroupId+",t1."+DbManager.colCreatedAt+",t1."+DbManager.colUpdatedAt+" FROM " + DbManager.mWorkFormRowCol + " t1 INNER JOIN " + DbManager.mWorkFormColumn + " t2 ON t1." + DbManager.colColId + "=t2." + DbManager.colID + " WHERE t1." + DbManager.colRowId + "=? ORDER BY t2." + DbManager.colPosition + " ASC";
		String[] args = new String[] {String.valueOf(workFormRowId)};
		cursor = DbRepository.getInstance().getDB().rawQuery(query, args);

		if (!cursor.moveToFirst()) {
			cursor.close();
			return result;
		}
		do {
			RowColumnModel model = getRowColumnFromCursor(cursor);
			model.items = getWorkFormItemModels(model.id);
			result.add(model);
		} while(cursor.moveToNext());

		cursor.close();
		return result;
	}
	
	private Vector<WorkFormItemModel> getWorkFormItemModels(int rowColumnId){
		WorkFormItemModel formItemModel = new WorkFormItemModel();
		return formItemModel.getAllItemByWorkFormRowColumnId(rowColumnId);
	}
	
	private RowColumnModel getRowColumnFromCursor(Cursor c) {
		RowColumnModel item= new RowColumnModel();

		if (null == c)
			return item;

		item.id = (c.getInt(c.getColumnIndex(DbManager.colID)));
		item.row_id = (c.getInt(c.getColumnIndex(DbManager.colRowId)));
		item.column_id = (c.getInt(c.getColumnIndex(DbManager.colColId)));
		item.work_form_group_id = (c.getInt(c.getColumnIndex(DbManager.colWorkFormGroupId)));
		item.created_at = (c.getString(c.getColumnIndex(DbManager.colCreatedAt)));
		item.updated_at = (c.getString(c.getColumnIndex(DbManager.colUpdatedAt)));

		return item;
	}
	
	public int getCountInput(){
		if (items == null || items.size() == 0)
			return 0;
		int count = 0;
		for (WorkFormItemModel item : items) {
			if (!item.field_type.equalsIgnoreCase("label"))
				count++;
		}
		return count;
	}
}
