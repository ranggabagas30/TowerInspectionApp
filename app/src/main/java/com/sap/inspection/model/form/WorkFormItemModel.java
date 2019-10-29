package com.sap.inspection.model.form;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.text.TextUtils;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sap.inspection.TowerApplication;
import com.sap.inspection.model.BaseModel;
import com.sap.inspection.model.DbManager;
import com.sap.inspection.model.DbRepository;
import com.sap.inspection.model.PictureModel;
import com.sap.inspection.model.value.FormValueModel;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.ImageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class WorkFormItemModel extends BaseModel {

	public int id;
	public int position;
	public boolean searchable;
	public boolean mandatory = false;
	public boolean visible;
	public boolean listable;
	public Vector<WorkFormOptionsModel> options;
	public int work_form_group_id;
	public String field_type;
	public String label;
	public String labelHeader;
	public String label_key;
	public String description;
	public String default_value;
	public String scope_type;
	public String pictureEndPoint;
	public int work_form_row_column_id;
	public String created_at;
	public String updated_at;
	public PictureModel picture;
	public boolean disable;
	public boolean search = true;
	public boolean expand;

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
		return "create table if not exists " + DbManager.mWorkFormItem
				+ " (" + DbManager.colID + " integer, "
				+ DbManager.colPosition + " integer, "
				+ DbManager.colSearchable + " integer, "
				+ DbManager.colMandatory + " integer, "
				+ DbManager.colVisible + " integer, "
				+ DbManager.colListable + " integer, "
				+ DbManager.colWorkFormGroupId + " integer, "
				+ DbManager.colFieldType + " varchar, "
				+ DbManager.colDefaultValue + " varchar, "
				+ DbManager.colScopeType + " varchar, "
				+ DbManager.colLable + " varchar, "
				+ DbManager.colLableKey + " varchar, "
				+ DbManager.colDescription + " varchar, "
				+ DbManager.colWorkFormRowColumnId + " integer, "
				+ DbManager.colCreatedAt + " varchar, "
				+ DbManager.colUpdatedAt + " varchar, "
				+ DbManager.colPicture + " varchar, "
				+ DbManager.colDisable + " integer, "
				+ DbManager.colSearch + " integer, "
				+ DbManager.colExpand + " integer, "
				+ "PRIMARY KEY (" + DbManager.colID + "))";
	}

	public void save(Context context){

		save();

	}

	public static void delete(Context ctx){

		DbRepository.getInstance().open(TowerApplication.getInstance());
		String sql = "DELETE FROM " + DbManager.mWorkFormItem;
		SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);
		stmt.executeUpdateDelete();
		stmt.close();
		DbRepository.getInstance().close();
	}

	public void save(){

		if (picture != null && picture.medium != null){
			pictureEndPoint = picture.medium;
		}

		saveImage();
		String sql = String
				.format("INSERT OR REPLACE INTO %s(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
						DbManager.mWorkFormItem , DbManager.colID, 				// 1 and 2
						DbManager.colPosition,DbManager.colSearchable,			// 3 and 4
						DbManager.colMandatory,DbManager.colVisible,			// 5 and 6
						DbManager.colListable,DbManager.colWorkFormGroupId,		// 7 and 8
						DbManager.colFieldType, DbManager.colLable,				// 9 and 10
						DbManager.colLableKey,DbManager.colDescription,			//
						DbManager.colWorkFormRowColumnId, DbManager.colDefaultValue,
						DbManager.colScopeType, DbManager.colCreatedAt,
						DbManager.colUpdatedAt, DbManager.colPicture,
						DbManager.colDisable,DbManager.colSearch,
						DbManager.colExpand);

		DbRepository.getInstance().open(TowerApplication.getInstance());
		SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);

		stmt.bindLong(1, id);
		stmt.bindLong(2, position);
		bindBooleanToInteger(stmt, 3, searchable);
		bindBooleanToInteger(stmt, 4, mandatory);
		bindBooleanToInteger(stmt, 5, visible);
		bindBooleanToInteger(stmt, 6, listable);
		stmt.bindLong(7, work_form_group_id);
		bindAndCheckNullString(stmt, 8, field_type);
		bindAndCheckNullString(stmt, 9, label);
		bindAndCheckNullString(stmt, 10, label_key);
		bindAndCheckNullString(stmt, 11, description);
		stmt.bindLong(12, work_form_row_column_id);
		bindAndCheckNullString(stmt, 13, default_value);
		bindAndCheckNullString(stmt, 14, scope_type);
		bindAndCheckNullString(stmt, 15, created_at);
		bindAndCheckNullString(stmt, 16, updated_at);
		bindAndCheckNullString(stmt, 17, pictureEndPoint);
		bindBooleanToInteger(stmt, 18, disable);
		bindBooleanToInteger(stmt, 19, search);
		bindBooleanToInteger(stmt, 20, expand);

		stmt.executeInsert();
		stmt.close();
		DbRepository.getInstance().close();

		if (options != null)
			for (WorkFormOptionsModel optionsModel : options) {
				optionsModel.save();
			}
	}
	
	public void saveImage(){
		if (pictureEndPoint == null)
			return;
		Bitmap bmp = ImageLoader.getInstance().loadImageSync(pictureEndPoint);
		pictureEndPoint = ImageUtil.resizeAndSaveImage(bmp, pictureEndPoint);
	}

	public static void setDefaultValueFromItemSchedule(String item_id, String group_id, String new_default_value) {

		  /**
		  * step :
		  * 1. get default_value data from local db (WorkFormItems table, default_value column)
		  * 2. if the 'new_default_value' not equal 'default_value', then overwrite the data
		  * 3. else don't update the data
		  * */
		 WorkFormItemModel formitem;

		 int workFormItemId  = Integer.valueOf(item_id);
		 int workFormGroupId = Integer.valueOf(group_id);

		 formitem = getWorkFormItemById(workFormItemId, workFormGroupId);

		 if (formitem != null) {
			 if (TextUtils.isEmpty(formitem.default_value)) {
				 DebugLog.d("column 'default_value' for workFormItemId " + item_id + " is null or empty");
				 updateDefaultValue(item_id, new_default_value);
			 } else {
				 if (!formitem.default_value.equalsIgnoreCase(new_default_value)) {
					 DebugLog.d("old default_value = " + formitem.default_value);
					 DebugLog.d("new default_value = " + new_default_value);
					 updateDefaultValue(item_id, new_default_value);
				 }
			 }
		 }

	}

	private static void updateDefaultValue(String workFormItemId, String new_default_value) {

		DebugLog.d("update new default value");

		ContentValues cv = new ContentValues();
		cv.put(DbManager.colDefaultValue, new_default_value);

		String column = "id"; // workFormItem id
		String where = column + "=?";
		String[] args = new String[] { workFormItemId };

		DebugLog.d("UPDATE workFormItem SET default_value = '" + new_default_value + "' WHERE " + DbManager.colWorkFormItemId + " = '" + workFormItemId);
		DbRepository.getInstance().open(TowerApplication.getInstance());
		DbRepository.getInstance().getDB().update(DbManager.mWorkFormItem, cv, where, args);
		DbRepository.getInstance().close();

	}

	public Vector<WorkFormItemModel> getAllItemByWorkFormRowColumnId(Context context, int workFormRowColumnId) {
		Vector<WorkFormItemModel> result = getAllItemByWorkFormRowColumnId(workFormRowColumnId);
		return result;
	}

	public static Vector<WorkFormItemModel> getAllItemByWorkFormRowColumnId(int workFormRowColumnId) {

		Vector<WorkFormItemModel> result = new Vector<WorkFormItemModel>();

		String table = DbManager.mWorkFormItem;
		String[] columns = null;
		String where =DbManager.colWorkFormRowColumnId + "=?";
		String[] args = new String[] {String.valueOf(workFormRowColumnId)};
		String order = DbManager.colPosition+" ASC";
		Cursor cursor;

		DbRepository.getInstance().open(TowerApplication.getInstance());
		cursor = DbRepository.getInstance().getDB().query(table, columns, where, args, null, null, order, null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			DbRepository.getInstance().close();
			return result;
		}
		do {
			WorkFormItemModel model = getItemFromCursor(cursor);
			model.options = getWorkFormOptionsModels(model.id);
			result.add(model);
		} while(cursor.moveToNext());

		cursor.close();
		DbRepository.getInstance().close();
		return result;
	}
	
	public static WorkFormItemModel getWorkFormItemById(int id) {

		WorkFormItemModel result = new WorkFormItemModel();

		String table = DbManager.mWorkFormItem;
		String[] columns = null;
		String where =DbManager.colID + "=? ";
		String[] args = new String[] {String.valueOf(id)};
		String order = DbManager.colPosition+" ASC";
		Cursor cursor;

		DbRepository.getInstance().open(TowerApplication.getInstance());
		cursor = DbRepository.getInstance().getDB().query(table, columns, where, args, null, null, order, null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			DbRepository.getInstance().close();
			return result;
		}
		result = getItemFromCursor(cursor);
		result.options = getWorkFormOptionsModels(result.id);

		cursor.close();
		DbRepository.getInstance().close();
		return result;
	}

	public static WorkFormItemModel getWorkFormItemById(int id, int workFormGroupId) {


		String table = DbManager.mWorkFormItem;
		String[] columns = null;
		String where = DbManager.colID + "=? AND "+ DbManager.colWorkFormGroupId + "=? ";
		String[] args = new String[] {String.valueOf(id), String.valueOf(workFormGroupId)};
		String order = DbManager.colID+" DESC";
		Cursor cursor;

		DbRepository.getInstance().open(TowerApplication.getInstance());
		cursor = DbRepository.getInstance().getDB().query(table, columns, where, args, null, null, order, null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			DbRepository.getInstance().close();
			return null;
		}

		WorkFormItemModel result;

		result = getItemFromCursor(cursor);
		result.options = getWorkFormOptionsModels(result.id);

		cursor.close();
		DbRepository.getInstance().close();
		return result;
	}

	public static WorkFormItemModel getItemByLable(int work_form_group_id, String lable) {

		String table = DbManager.mWorkFormItem;
		String[] columns = null;
		String whereworkformgroupid = work_form_group_id > 0 ? DbManager.colWorkFormGroupId + "=?" : "";
		String wherelable	        = lable != null ? " AND " + DbManager.colLable + "=?" : "";
		String where = whereworkformgroupid + wherelable;

		DebugLog.d("Get work form item(s) by : " + where);

		List<String> argsList = new ArrayList<>();

		if (work_form_group_id > 0)
			argsList.add(String.valueOf(work_form_group_id));
		if (lable != null)
			argsList.add(lable);

		String[] args = new String[argsList.size()];
		args = argsList.toArray(args);

		Cursor cursor;

		DbRepository.getInstance().open(TowerApplication.getInstance());
		cursor = DbRepository.getInstance().getDB().query(table, columns, where, args, null, null, null, null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			DbRepository.getInstance().close();
			return null;
		}

		WorkFormItemModel result = getItemFromCursor(cursor);
		result.options = getWorkFormOptionsModels(result.id);

		cursor.close();
		DbRepository.getInstance().close();
		return result;
	}

	public static ArrayList<WorkFormItemModel> getWorkFormItems(int work_form_group_id, String excl_field_type) {

		String table = DbManager.mWorkFormItem;
		String[] columns = null;
		String whereworkformgroupid = work_form_group_id != FormValueModel.UNSPECIFIED ? DbManager.colWorkFormGroupId + "=? AND " : "";
		String wherefieldtype	    = excl_field_type != null ? DbManager.colFieldType + "!=?" : "";
		String where = whereworkformgroupid + wherefieldtype;

		DebugLog.d("Get work form item(s) by workFormGroupId = " + work_form_group_id + " AND field type != " + excl_field_type);

		List<String> argsList = new ArrayList<>();

		if (work_form_group_id > 0)
			argsList.add(String.valueOf(work_form_group_id));
		if (excl_field_type != null)
			argsList.add(excl_field_type);

		String[] args = new String[argsList.size()];
		args = argsList.toArray(args);

		String order = DbManager.colID + " DESC";
		Cursor cursor;

		DbRepository.getInstance().open(TowerApplication.getInstance());
		cursor = DbRepository.getInstance().getDB().query(table, columns, where, args, null, null, order, null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			DbRepository.getInstance().close();
			return null;
		}

		ArrayList<WorkFormItemModel> result = new ArrayList<>();

		do {

			WorkFormItemModel model = getItemFromCursor(cursor);
			model.options = getWorkFormOptionsModels(model.id);

			result.add(model);

		} while (cursor.moveToNext());

		cursor.close();
		DbRepository.getInstance().close();
		return result;
	}

	private static Vector<WorkFormOptionsModel> getWorkFormOptionsModels(int workFormItemId){

		return WorkFormOptionsModel.getAllItemByWorkFormItemId(workFormItemId);

	}

	private static WorkFormItemModel getItemFromCursor(Cursor c) {
		WorkFormItemModel item= new WorkFormItemModel();

		if (null == c)
			return item;

		item.id = (c.getInt(c.getColumnIndex(DbManager.colID)));
		item.position = (c.getInt(c.getColumnIndex(DbManager.colPosition)));
		item.searchable = (int) (c.getLong(c.getColumnIndex(DbManager.colSearchable))) == 1;
		item.mandatory = (int) (c.getLong(c.getColumnIndex(DbManager.colMandatory))) == 1;
		item.visible = (int) (c.getLong(c.getColumnIndex(DbManager.colVisible))) == 1;
		item.listable = (int) (c.getLong(c.getColumnIndex(DbManager.colListable))) == 1;
		item.work_form_group_id = (c.getInt(c.getColumnIndex(DbManager.colWorkFormGroupId)));
		item.field_type = (c.getString(c.getColumnIndex(DbManager.colFieldType)));
		item.default_value = (c.getString(c.getColumnIndex(DbManager.colDefaultValue)));
		item.scope_type = (c.getString(c.getColumnIndex(DbManager.colScopeType)));
		item.label = (c.getString(c.getColumnIndex(DbManager.colLable)));
		item.label_key = (c.getString(c.getColumnIndex(DbManager.colLableKey)));
		item.work_form_row_column_id = (c.getInt(c.getColumnIndex(DbManager.colWorkFormRowColumnId)));
		item.created_at = (c.getString(c.getColumnIndex(DbManager.colCreatedAt)));
		item.updated_at = (c.getString(c.getColumnIndex(DbManager.colUpdatedAt)));
		item.description = (c.getString(c.getColumnIndex(DbManager.colDescription)));
		item.pictureEndPoint = (c.getString(c.getColumnIndex(DbManager.colPicture)));
		item.disable = (int) (c.getLong(c.getColumnIndex(DbManager.colDisable))) == 1;
		item.search = (int) (c.getLong(c.getColumnIndex(DbManager.colSearch))) == 1;
		item.expand = (int) (c.getLong(c.getColumnIndex(DbManager.colExpand))) == 1;

		return item;
	}

}
