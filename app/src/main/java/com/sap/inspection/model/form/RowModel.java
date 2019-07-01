package com.sap.inspection.model.form;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.os.Debug;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.sap.inspection.MyApplication;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.model.BaseModel;
import com.sap.inspection.model.DbManager;
import com.sap.inspection.model.DbRepository;
import com.sap.inspection.model.config.formimbaspetir.Barang;
import com.sap.inspection.model.config.formimbaspetir.FormImbasPetirConfig;
import com.sap.inspection.model.value.DbRepositoryValue;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.StringUtil;

import java.util.ArrayList;
import java.util.Vector;

import static com.crashlytics.android.Crashlytics.log;

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
			DebugLog.d("get row-col by rowid : " + model.id);
			DebugLog.d("row-col size : " + model.row_columns.size());

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
			DebugLog.d("===== level : "+model.level+", text : "+model.text+", id : "+model.id+", position : "+model.position+", ancestry : "+model.ancestry+", row_col size : "+model.row_columns.size());
			result.add(model);
		} while(cursor.moveToNext());

		cursor.close();
		DbRepository.getInstance().close();
		return result;
	}
	
	public Vector<RowModel> getAllItemByWorkFormGroupIdAndLikeAncestry(int workFormGroupId, String ancestry) {
	    return getAllItemByWorkFormGroupIdAndLikeAncestry(workFormGroupId, ancestry, null);
	}

	public static Vector<RowModel> getAllItemByWorkFormGroupIdAndLikeAncestry(int workFormGroupId, String ancestry, int ... rowIds) {

        DebugLog.d("workFormGroupId : " + workFormGroupId + ", ancestry LIKE : " + ancestry + ", and rowIds");
        Vector<RowModel> result = new Vector<RowModel>();
        String table = DbManager.mWorkFormRow;
        String[] columns = null;

        ArrayList<String> argsList = new ArrayList<>();
        argsList.add(String.valueOf(workFormGroupId));

        String whereAncestry;

		if (ancestry != null) {
			whereAncestry = " AND " + DbManager.colAncestry +" LIKE '" + ancestry + "%'";
		} else {
			whereAncestry =  " AND " + DbManager.colAncestry +" IS NULL";
		}

		StringBuilder whereRowIdBuilder = new StringBuilder();
		if (rowIds != null) {
			whereRowIdBuilder.append(" AND " + DbManager.colID + " in (");
			for (int i = 0; i < rowIds.length; i++) {
				DebugLog.d("rowId : " + rowIds[i]);
				whereRowIdBuilder.append(rowIds[i]);
				if (rowIds[i] != rowIds[rowIds.length-1]) {
					whereRowIdBuilder.append(",");
				}
			}
			whereRowIdBuilder.append(")");
		}

		String[] args = new String[argsList.size()];
		args = argsList.toArray(args);

		String where = DbManager.colWorkFormGroupId + "=?" + whereAncestry + whereRowIdBuilder;
		String order = DbManager.colPosition + " ASC";
		DebugLog.d("where = " + where);
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
                for (WorkFormItemModel item : row_col.items) {
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

        DebugLog.d("row children size : " + result.size());

        cursor.close();
        DbRepository.getInstance().close();
        return result;
    }

	public RowModel getItemById(int workFormGroupId,int parentRowId) {
		return getItemById(workFormGroupId, parentRowId, false);
	}
	
	public RowModel getItemById(int workFormGroupId,int parentRowId, boolean allChild) {

		DebugLog.d("workFormGroupId : " + workFormGroupId + ", and parentRowId : " + parentRowId + " ascending order");

		String table = DbManager.mWorkFormRow;
		String[] columns = null;
		String where = DbManager.colWorkFormGroupId + "=? AND " + DbManager.colID + "=?";;
		String[] args = new String[] {String.valueOf(workFormGroupId),String.valueOf(parentRowId)};
		String order = DbManager.colPosition+" ASC";

		DbRepository.getInstance().open(MyApplication.getInstance());
		Cursor cursor = DbRepository.getInstance().getDB().query(table, columns, where, args, null, null, order, null);

		if (!cursor.moveToFirst()){
			DbRepository.getInstance().close();
			return null;
		}

		RowModel result;

		do {
			result = getRowFromCursor(cursor); 
			result.row_columns = getRowColumnModels(result.id);
			for (RowColumnModel row_col : result.row_columns) {
				for (WorkFormItemModel item : row_col.items) {
					if (item.label != null){
						DebugLog.d("found label --> " + item.label);
						result.text = item.label;
						break;
					}
				}
				if (result.text != null)
					break;
			}
		} while(cursor.moveToNext());
		cursor.close();
		DbRepository.getInstance().close();

		if (result.ancestry != null)
			result.children = getAllItemByWorkFormGroupIdAndLikeAncestry(workFormGroupId, result.ancestry+"/"+result.id);
		else
			result.children = getAllItemByWorkFormGroupIdAndLikeAncestry(workFormGroupId, String.valueOf(result.id));

		return result;
	}

	public RowModel getItemById(int workFormGroupId, int parentRowId, int ... childRowIds) {

		DebugLog.d("workFormGroupId : " + workFormGroupId + ", parentRowId : " + parentRowId + ", childrowIds and ascending order");
		RowModel result = null;

		String table = DbManager.mWorkFormRow;
		String[] columns = null;
		String where = null;
		String[] args = null;
		where = DbManager.colWorkFormGroupId + "=? AND " + DbManager.colID + "=?";
		args = new String[] {String.valueOf(workFormGroupId),String.valueOf(parentRowId)};

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
						DebugLog.d("found label --> " + item.label);
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
			result.children = getAllItemByWorkFormGroupIdAndLikeAncestry(workFormGroupId, result.ancestry+"/"+result.id, childRowIds);
		else
			result.children = getAllItemByWorkFormGroupIdAndLikeAncestry(workFormGroupId, String.valueOf(result.id), childRowIds);

		return result;
	}

	public static Vector<RowModel> getWargaKeNavigationItemsRowModel(String parentId, String scheduleId, String wargaId) {

	    DebugLog.d("get wargaKe navigation menu items ");

	    String table = DbManager.mWorkFormRow;
	    String where = DbManager.colParentId + "=?";
	    String[] args = new String[]{parentId};

        DbRepository.getInstance().open(MyApplication.getInstance());
        Cursor cursor = DbRepository.getInstance().getDB().query(true, table, null, where, args, null, null, null, null);

        if (!cursor.moveToFirst()) {
            DbRepository.getInstance().close();
            return null;
        }

        Vector<RowModel> navigationItemRowModels = new Vector<>();

        do {

            RowModel parentItem = getRowFromCursor(cursor);

            DebugLog.d("acuan parent id = " + parentId);
			if (parentItem.parent_id == Integer.valueOf(parentId)) {

				parentItem.text = getRowLabel(parentItem.id);
				parentItem.hasForm = true;

				if (parentItem.text != null && parentItem.text.equalsIgnoreCase(Constants.regexId)) {

				    int dataIndex = FormImbasPetirConfig.getDataIndex(scheduleId);

				    if (dataIndex != -1) {

                        // get list data of barang
                        ArrayList<Barang> barangs = FormImbasPetirConfig.getDataBarang(dataIndex, wargaId);
                        int barangSize = barangs == null ? 0 : barangs.size();

                        DebugLog.d("barangsize = " + barangSize);

                        // adding barangid menu to the list
						String barangLabel = parentItem.text;

                        for (int barangke = 0; barangke < barangSize; barangke++) {

                        	String barangID = barangs.get(barangke).getBarangid();
                        	String barangName = StringUtil.getName(scheduleId, wargaId, barangID, parentItem.work_form_group_id);
                            StringBuilder barangLabelBuilder = new StringBuilder(barangLabel).append(barangID);

                            if (!TextUtils.isEmpty(barangName))
                            	barangLabelBuilder.append(" (").append(barangName).append(")");

                            DebugLog.d("baranglabelbuilder = " + barangLabelBuilder);

                            RowModel barangMenuModel = new RowModel();
                            barangMenuModel.id   = parentItem.id;
                            barangMenuModel.parent_id = parentItem.parent_id;
							barangMenuModel.ancestry = parentItem.ancestry;
							barangMenuModel.level = parentItem.level;
							barangMenuModel.hasForm = parentItem.hasForm;
							barangMenuModel.work_form_group_id = parentItem.work_form_group_id;
							barangMenuModel.text = new String(barangLabelBuilder);

                            DebugLog.d("== result navigation items ==");
                            DebugLog.d("id : " + barangMenuModel.id);
                            DebugLog.d("name : " + barangMenuModel.text);
                            DebugLog.d("parentid : " + barangMenuModel.parent_id);
                            DebugLog.d("ancestry : " + barangMenuModel.ancestry);
                            DebugLog.d("level : " + barangMenuModel.level);
                            DebugLog.d("hasForm : " + barangMenuModel.hasForm);
                            DebugLog.d("workFormGroupId : " + barangMenuModel.work_form_group_id);

                            navigationItemRowModels.add(barangMenuModel);
                        }

                        // row model for "tambah barang" submenu action
                        RowModel addBarangKeModel = new RowModel();
                        addBarangKeModel.id = -1;
                        addBarangKeModel.work_form_group_id = parentItem.work_form_group_id;
                        addBarangKeModel.hasForm = false;
                        addBarangKeModel.text = "Tambah barang";
                        addBarangKeModel.level = 2;
                        addBarangKeModel.ancestry = null;
                        addBarangKeModel.parent_id = 0;

                        navigationItemRowModels.add(addBarangKeModel);
                    }

                } else {

                    DebugLog.d("== result navigation items ==");
                    DebugLog.d("id : " + parentItem.id);
                    DebugLog.d("name : " + parentItem.text);
                    DebugLog.d("parentid : " + parentItem.parent_id);
                    DebugLog.d("ancestry : " + parentItem.ancestry);
                    DebugLog.d("level : " + parentItem.level);
                    DebugLog.d("hasForm : " + parentItem.hasForm);
                    DebugLog.d("workFormGroupId : " + parentItem.work_form_group_id);

                    navigationItemRowModels.add(parentItem);
                }

				if (parentItem.level == 1) {

					DebugLog.d("==== get children navigation ===");
					Vector<RowModel> childItems = getWargaKeNavigationItemsRowModel(String.valueOf(parentItem.id), scheduleId, wargaId);

					if (childItems != null && !childItems.isEmpty()) {
						parentItem.children = childItems;
					}
				}
			}

        } while (cursor.moveToNext());

        cursor.close();
        DbRepository.getInstance().close();

        return navigationItemRowModels;
    }

    private static String getRowLabel(int rowId) {

		Vector<RowColumnModel> rowColumnModels = getRowColumnModels(rowId);

		for (RowColumnModel row_col : rowColumnModels) {
			DebugLog.d("== row_col "+row_col.id);
			for (WorkFormItemModel item : row_col.items) {
				DebugLog.d("== item "+item.label);
				if (item.label != null){
					return item.label;
				}
			}
		}
		return null;
	}

	private static Vector<RowColumnModel> getRowColumnModels(int rowId){

		return RowColumnModel.getAllItemByWorkFormRowId(rowId);

	}

	private static RowModel getRowFromCursor(Cursor c) {
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
