package com.sap.inspection.model.form;

import java.util.Vector;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.os.Parcel;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.model.BaseModel;
import com.sap.inspection.model.DbManager;
import com.sap.inspection.model.DbRepository;
import com.sap.inspection.model.PictureModel;
import com.sap.inspection.util.ImageUtil;

public class WorkFormItemModel extends BaseModel {

	public int id;
	public int position;
	public boolean searchable;
	public boolean mandatory;
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
				+ "PRIMARY KEY (" + DbManager.colID + "))";
	}

	public void save(Context context){
		DbRepository.getInstance().open(context);
		save();
		DbRepository.getInstance().close();
	}

	public void save(){
		if (picture != null && picture.medium != null){
			pictureEndPoint = picture.medium;
		}

		saveImage();
		String sql = String
				.format("INSERT OR REPLACE INTO %s(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
						DbManager.mWorkFormItem , DbManager.colID,
						DbManager.colPosition,DbManager.colSearchable,
						DbManager.colMandatory,DbManager.colVisible,
						DbManager.colListable,DbManager.colWorkFormGroupId,
						DbManager.colFieldType,DbManager.colLable,
						DbManager.colLableKey,DbManager.colDescription,
						DbManager.colWorkFormRowColumnId, DbManager.colDefaultValue,
						DbManager.colScopeType, DbManager.colCreatedAt,
						DbManager.colUpdatedAt, DbManager.colPicture);
		SQLiteStatement stmt = DbRepository.getInstance().getDB()
				.compileStatement(sql);

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

		stmt.executeInsert();
		stmt.close();

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

	public Vector<WorkFormItemModel> getAllItemByWorkFormRowColumnId(Context context, int workFormRowColumnId) {

		DbRepository.getInstance().open(context);
		Vector<WorkFormItemModel> result = getAllItemByWorkFormRowColumnId(workFormRowColumnId);
		DbRepository.getInstance().close();
		return result;
	}

	public Vector<WorkFormItemModel> getAllItemByWorkFormRowColumnId(int workFormRowColumnId) {

		Vector<WorkFormItemModel> result = new Vector<WorkFormItemModel>();

		String table = DbManager.mWorkFormItem;
		String[] columns = null;
		String where =DbManager.colWorkFormRowColumnId + "=?";
		String[] args = new String[] {String.valueOf(workFormRowColumnId)};
		String order = DbManager.colPosition+" ASC";
		Cursor cursor;

		cursor = DbRepository.getInstance().getDB().query(table, columns, where, args, null, null, order, null);

		if (!cursor.moveToFirst())
			return result;
		do {
			WorkFormItemModel model = getItemFromCursor(cursor);
			model.options = getWorkFormOptionsModels(model.id);
			result.add(model);
		} while(cursor.moveToNext());

		cursor.close();
		return result;
	}
	
	public WorkFormItemModel getItemById(int id) {

		WorkFormItemModel result = new WorkFormItemModel();

		String table = DbManager.mWorkFormItem;
		String[] columns = null;
		String where =DbManager.colID + "=? ";
		String[] args = new String[] {String.valueOf(id)};
		String order = DbManager.colPosition+" ASC";
		Cursor cursor;

		cursor = DbRepository.getInstance().getDB().query(table, columns, where, args, null, null, order, null);

		if (!cursor.moveToFirst())
			return result;
		result = getItemFromCursor(cursor);
		result.options = getWorkFormOptionsModels(result.id);

		cursor.close();
		return result;
	}

	
	private Vector<WorkFormOptionsModel> getWorkFormOptionsModels(int workFormItemId){
		WorkFormOptionsModel model = new WorkFormOptionsModel();
		return model.getAllItemByWorkFormItemId(workFormItemId);
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

	private WorkFormItemModel getItemFromCursor(Cursor c) {
		WorkFormItemModel item= new WorkFormItemModel();

		if (null == c)
			return item;

		item.id = (c.getInt(c.getColumnIndex(DbManager.colID)));
		item.position = (c.getInt(c.getColumnIndex(DbManager.colPosition)));
		item.searchable = (int) (c.getLong(c.getColumnIndex(DbManager.colSearchable))) == 1 ? true : false;
		item.mandatory = (int) (c.getLong(c.getColumnIndex(DbManager.colMandatory))) == 1 ? true : false;
		item.visible = (int) (c.getLong(c.getColumnIndex(DbManager.colVisible))) == 1 ? true : false;
		item.listable = (int) (c.getLong(c.getColumnIndex(DbManager.colListable))) == 1 ? true : false;
		item.work_form_group_id = (c.getInt(c.getColumnIndex(DbManager.colWorkFormGroupId)));
		item.field_type = (c.getString(c.getColumnIndex(DbManager.colFieldType)));
		item.label = (c.getString(c.getColumnIndex(DbManager.colLable)));
		item.default_value = (c.getString(c.getColumnIndex(DbManager.colDefaultValue)));
		item.scope_type = (c.getString(c.getColumnIndex(DbManager.colScopeType)));
		item.label_key = (c.getString(c.getColumnIndex(DbManager.colLableKey)));
		item.work_form_row_column_id = (c.getInt(c.getColumnIndex(DbManager.colWorkFormRowColumnId)));
		item.created_at = (c.getString(c.getColumnIndex(DbManager.colCreatedAt)));
		item.updated_at = (c.getString(c.getColumnIndex(DbManager.colUpdatedAt)));
		item.description = (c.getString(c.getColumnIndex(DbManager.colDescription)));
		item.pictureEndPoint = (c.getString(c.getColumnIndex(DbManager.colPicture)));

		return item;
	}

}
