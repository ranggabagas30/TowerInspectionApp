package com.sap.inspection.model.form;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.os.Debug;
import android.os.Parcel;

import com.sap.inspection.MyApplication;
import com.sap.inspection.model.BaseModel;
import com.sap.inspection.model.DbManager;
import com.sap.inspection.model.DbRepository;
import com.sap.inspection.model.value.DbRepositoryValue;
import com.sap.inspection.tools.DebugLog;

import java.util.Vector;

public class RowModel extends BaseModel {

	public int id;
	public int position;
	public int parent_id;
	public String ancestry;
	public int work_form_group_id;
	public int sumTask;
	public String created_at;
	public String updated_at;
	public static int maxLevel;
	public Vector<RowColumnModel> row_columns;

	public boolean isOpen;
	public boolean hasForm = false;
	public int level;
	public String text;
	public Vector<RowModel> children;

	private Context context;

	public RowModel() {

	}

	public RowModel(Context context) {
		this.context = context;
	}

	public int getCount(){
		int count = 0;
		if (isOpen && children != null){
			for (RowModel child : children) {
				count += child.getCount();
			}
			count += children.size();
		}
		return count;
	}

	public Vector<RowModel> getModels(){
		Vector<RowModel> models = new Vector<RowModel>();
		if (isOpen && children != null){
			for (RowModel child : children) {
				models.add(child);
				models.addAll(child.getModels());
			}
		}
		return models;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
	}

	public static String createDB(){
		return "create table if not exists " + DbManager.mWorkFormRow
				+ " (" + DbManager.colID + " integer, "
				+ DbManager.colPosition + " integer, "
				+ DbManager.colParentId + " integer, "
				+ DbManager.colAncestry + " varchar, "
				+ DbManager.colLevel + " integer, "
				+ DbManager.colWorkFormGroupId + " integer, "
				+ DbManager.colSumTask + " integer, "
				+ DbManager.colCreatedAt + " varchar, "
				+ DbManager.colUpdatedAt + " varchar, "
				+ "PRIMARY KEY (" + DbManager.colID + "))";
	}

	public void save(Context context){

		save();

	}

	public static void delete(Context ctx){

		DbRepository.getInstance().open(MyApplication.getInstance());
		String sql = "DELETE FROM " + DbManager.mWorkFormRow;
		SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);
		stmt.executeUpdateDelete();
		stmt.close();
		DbRepository.getInstance().close();
	}

	public void save(){

		String sql = String
				.format("INSERT OR REPLACE INTO %s(%s,%s,%s,%s,%s,%s,%s,%s,%s) VALUES(?,?,?,?,?,?,?,?,?)",
						DbManager.mWorkFormRow, DbManager.colID,
						DbManager.colPosition, DbManager.colParentId,
						DbManager.colAncestry, DbManager.colWorkFormGroupId,
						DbManager.colSumTask, DbManager.colCreatedAt,
						DbManager.colUpdatedAt, DbManager.colLevel);


		DbRepository.getInstance().open(MyApplication.getInstance());
		SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);

		stmt.bindLong(1, id);
		stmt.bindLong(2, position);
		stmt.bindLong(3, parent_id);
		bindAndCheckNullString(stmt, 4, ancestry);
		stmt.bindLong(5, work_form_group_id);
		stmt.bindLong(6, sumTask);
		bindAndCheckNullString(stmt, 7, created_at);
		bindAndCheckNullString(stmt, 8, updated_at);

		if (ancestry == null)
			level = 1;
		else if (ancestry.contains("/"))
			level = ancestry.length() - ancestry.replace("/", "").length() + 2;
		else
			level = 2;

		stmt.bindLong(9, level);

		stmt.executeInsert();
		stmt.close();
		DbRepository.getInstance().close();

		if (row_columns != null)
			for (RowColumnModel item : row_columns) {
				item.save();
			}
	}

	public Vector<RowModel> getAllItemByWorkFormGroupId(Context context, int workFormGroupId) {

		Vector<RowModel> result = getAllItemByWorkFormGroupId(workFormGroupId);

		return result;
	}

	/**
	 * Only if DB is opened 
	 * @return max tree level from the form
	 */
	public int getMaxLevel(String workFormGroupId){

		DbRepository.getInstance().open(MyApplication.getInstance());
		Cursor cursor = DbRepository.getInstance().getDB().rawQuery("SELECT MAX("+DbManager.colLevel+") FROM "+DbManager.mWorkFormRow+" WHERE "+DbManager.colWorkFormGroupId+"="+workFormGroupId, null);
		if (!cursor.moveToFirst()){
			cursor.close();
			return -1;
		}
		int temp  = cursor.getInt(0);

		cursor.close();
		DbRepository.getInstance().close();
		return temp;
	}

	public Vector<RowModel> getAllItemByWorkFormGroupId(int workFormGroupId) {
		Vector<RowModel> result;
		//		maxLevel = getMaxLevel(workFormGroupId);
		//		if (maxLevel == -1)
		//			return result;

		result = getAllItemByWorkFormGroupIdAndAncestry(workFormGroupId, null);
		for (RowModel rowModel : result) {
//			rowModel.children = getAllItemByWorkFormGroupIdAndAncestry(workFormGroupId, String.valueOf(rowModel.id));
//			for (RowModel model : rowModel.children) {
				rowModel.hasForm = true;
//			}
		}

		//		for (int i = 1; i <= 2; i++) {
		//			
		//		String table = DbManager.mWorkFormRow;
		//		String[] columns = null;
		//		String where =DbManager.colWorkFormGroupId + "=? AND "+DbManager.colLevel+"="+3;
		//		String[] args = new String[] {workFormGroupId};
		//		String order = DbManager.colLevel+" ASC, LENGTH("+DbManager.colAncestry+") ASC,"+ DbManager.colAncestry+" ASC," + DbManager.colPosition+" ASC";
		//
		//		Cursor cursor = DbRepository.getInstance().getDB().query(table, columns, where, args, null, null, order, null);
		//
		//		if (!cursor.moveToFirst())
		//			return result;
		//		do {
		//			RowModel model = getRowFromCursor(cursor); 
		//			model.row_columns = getRowColumnModels(model.id);
		//			log("===== id : "+model.id+"   position : "+model.position+"   ancestry : "+model.ancestry+" row_col size : "+model.row_columns.size());
		//			result.add(model);
		//		} while(cursor.moveToNext());
		//
		//		cursor.close();
		//		
		//		}

		return result;
	}

	public Vector<RowModel> getAllItemByWorkFormGroupIdAndAncestry(int workFormGroupId,String ancestry) {

		DebugLog.d("workFormGroupId : " + workFormGroupId + ", ancestry LIKE : " + ancestry);
		Vector<RowModel> result = new Vector<RowModel>();
		String table = DbManager.mWorkFormRow;
		String[] columns = null;
		String where = null;
		String[] args = null;
		if (ancestry != null){
			where =DbManager.colWorkFormGroupId + "=? AND "+DbManager.colAncestry+"=?";
			args = new String[] {String.valueOf(workFormGroupId),ancestry};
		}
		else{
			where =DbManager.colWorkFormGroupId + "=? AND "+DbManager.colAncestry+" IS NULL";
			args = new String[] {String.valueOf(workFormGroupId)};
		}
//		String order = DbManager.colLevel+" ASC, LENGTH("+DbManager.colAncestry+") ASC,"+ DbManager.colAncestry+" ASC," + DbManager.colPosition+" ASC";
		String order = DbManager.colPosition+" ASC";

		DbRepository.getInstance().open(MyApplication.getInstance());
		Cursor cursor = DbRepository.getInstance().getDB().query(table, columns, where, args, null, null, order, null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			DbRepository.getInstance().close();
			return result;
		}
		do {
			RowModel model = getRowFromCursor(cursor); 
			model.row_columns = getRowColumnModels(model.id);
			for (RowColumnModel row_col : model.row_columns) {
				DebugLog.d("== row_col "+row_col.id);
				for (WorkFormItemModel item : row_col.items) {
					DebugLog.d("== item "+item.label);
					if (item.label != null){
						model.text = item.label;
						break;
					}
				}
				if (model.text != null)
					break;
			}
			DebugLog.d("===== level : "+model.level+"  text : "+model.text+"  id : "+model.id+"   position : "+model.position+"   ancestry : "+model.ancestry+" row_col size : "+model.row_columns.size());
			result.add(model);
		} while(cursor.moveToNext());

		cursor.close();
		DbRepository.getInstance().close();
		return result;
	}
	
	public Vector<RowModel> getAllItemByWorkFormGroupIdAndLikeAncestry(int workFormGroupId,String ancestry) {

		DebugLog.d("workFormGroupId : " + workFormGroupId + ", ancestry LIKE : " + ancestry);
		Vector<RowModel> result = new Vector<RowModel>();
		String table = DbManager.mWorkFormRow;
		String[] columns = null;
		String where = null;
		String[] args = null;
		if (ancestry != null){
			where =DbManager.colWorkFormGroupId + "=? AND "+DbManager.colAncestry+" LIKE '"+ancestry+"%'";
			args = new String[] {String.valueOf(workFormGroupId)};
		}
		
		else{
			where =DbManager.colWorkFormGroupId + "=? AND "+DbManager.colAncestry+" IS NULL";
			args = new String[] {String.valueOf(workFormGroupId)};
		}
//		String order = DbManager.colLevel+" ASC, LENGTH("+DbManager.colAncestry+") ASC,"+ DbManager.colAncestry+" ASC," + DbManager.colPosition+" ASC";
		String order = DbManager.colPosition+" ASC";


		DbRepository.getInstance().open(MyApplication.getInstance());
		Cursor cursor = DbRepository.getInstance().getDB().query(table, columns, where, args, null, null, order, null);

		if (!cursor.moveToFirst())
		{
			cursor.close();
			DbRepository.getInstance().close();
			return result;
		}

		do {
			RowModel model = getRowFromCursor(cursor); 
			model.row_columns = getRowColumnModels(model.id);
			for (RowColumnModel row_col : model.row_columns) {
//				log("== row_col "+row_col.id);
				for (WorkFormItemModel item : row_col.items) {
//					log("== item "+item.label);
					if (item.label != null){
						model.text = item.label;
						break;
					}
				}
				if (model.text != null)
					break;
			}
//			log("===== level : "+model.level+"  text : "+model.text+"  id : "+model.id+"   position : "+model.position+"   ancestry : "+model.ancestry+" row_col size : "+model.row_columns.size());
			result.add(model);
		} while(cursor.moveToNext());

		cursor.close();
		DbRepository.getInstance().close();
		return result;
	}
	public RowModel getItemById(int workFormGroupId,int rowId) {
		return getItemById(workFormGroupId, rowId, false);
	}
	
	public RowModel getItemById(int workFormGroupId,int rowId, boolean allChild) {

		RowModel result = null;


		String table = DbManager.mWorkFormRow;
		String[] columns = null;
		String where = null;
		String[] args = null;
			where =DbManager.colWorkFormGroupId + "=? AND " + DbManager.colID + "=?";
			args = new String[] {String.valueOf(workFormGroupId),String.valueOf(rowId)};
		
//		String order = DbManager.colLevel+" ASC, LENGTH("+DbManager.colAncestry+") ASC,"+ DbManager.colAncestry+" ASC," + DbManager.colPosition+" ASC";
		String order = DbManager.colPosition+" ASC";


		DbRepository.getInstance().open(MyApplication.getInstance());
		Cursor cursor = DbRepository.getInstance().getDB().query(table, columns, where, args, null, null, order, null);

		if (!cursor.moveToFirst()){
			DbRepository.getInstance().close();
			return result;

		}
		do {
			result = getRowFromCursor(cursor); 
			result.row_columns = getRowColumnModels(result.id);
			for (RowColumnModel row_col : result.row_columns) {
//				log("== row_col "+row_col.id);
				for (WorkFormItemModel item : row_col.items) {
//					log("== item "+item.label);
					if (item.label != null){
						result.text = item.label;
						break;
					}
				}
				if (result.text != null)
					break;
			}
//			log("===== level : "+result.level+"  text : "+result.text+"  id : "+result.id+"   position : "+result.position+"   ancestry : "+result.ancestry+" row_col size : "+result.row_columns.size());
		} while(cursor.moveToNext());
		cursor.close();
		DbRepository.getInstance().close();

		if (result.ancestry != null)
			result.children = getAllItemByWorkFormGroupIdAndLikeAncestry(workFormGroupId, result.ancestry+"/"+result.id);
		else
			result.children = getAllItemByWorkFormGroupIdAndLikeAncestry(workFormGroupId, String.valueOf(result.id));

		return result;
	}
	
	private Vector<RowColumnModel> getRowColumnModels(int rowId){
		RowColumnModel rowColumnModel = new RowColumnModel();
		return rowColumnModel.getAllItemByWorkFormRowId(rowId);
	}

	private RowModel getRowFromCursor(Cursor c) {
		RowModel item= new RowModel();

		if (null == c)
			return item;

		item.id = (c.getInt(c.getColumnIndex(DbManager.colID)));
		item.position = (c.getInt(c.getColumnIndex(DbManager.colPosition)));
		item.level = (c.getInt(c.getColumnIndex(DbManager.colLevel)));
		item.parent_id = (c.getInt(c.getColumnIndex(DbManager.colParentId)));
		item.work_form_group_id = (c.getInt(c.getColumnIndex(DbManager.colWorkFormGroupId)));
		item.sumTask = (c.getInt(c.getColumnIndex(DbManager.colSumTask)));
		item.ancestry = (c.getString(c.getColumnIndex(DbManager.colAncestry)));
		item.created_at = (c.getString(c.getColumnIndex(DbManager.colCreatedAt)));
		item.updated_at = (c.getString(c.getColumnIndex(DbManager.colUpdatedAt)));

		return item;
	}

	public int getInputCount(){
		if (row_columns == null && row_columns.size() == 0)
			return 0;
		int count = 0;
		for (RowColumnModel rowColumnModel : row_columns) {
			count += rowColumnModel.getCountInput();
		}
		return count;
	}
}
