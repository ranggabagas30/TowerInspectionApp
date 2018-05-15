package com.sap.inspection.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.os.Parcel;

import com.sap.inspection.MyApplication;
import com.sap.inspection.tools.DebugLog;

public class SiteModel extends BaseModel {
	
    public int id;
    public String name;
    public String locationStr;
    public String site_id_customer;
    public LocationModel location;

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		// TODO Auto-generated method stub
	}
	
	public void save(Context context){
		DbRepository.getInstance().open(context);
		save();
		DbRepository.getInstance().close();
	}
	
	public SiteModel getSiteById(Context context,int id) {
		SiteModel model = null;
		DbRepository.getInstance().open(context);
		model = getSiteById(id);
		DbRepository.getInstance().close();
		return model;
	}
	
	public SiteModel getSiteById(int id) {
		SiteModel model = null;

		String table = DbManager.mSite;
		String[] columns = null;
		String where = DbManager.colID+"=?";
		String[] args = new String[]{String.valueOf(id)};
		Cursor cursor;

		cursor = DbRepository.getInstance().getDB().query(true, table, columns, where, args, null, null,null, null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			return model;
		}

		model = getSiteFromCursor(cursor);

		cursor.close();
		return model;
	}
	
	public void save(){
		switch (DbManager.schema_version) {
			case 9 : {

				DebugLog.d("id="+id+" name="+name+" locationStr="+locationStr+ " site_id_customer=" +site_id_customer);
				String sql = String.format("INSERT OR REPLACE INTO %s(%s,%s,%s,%s) VALUES(?,?,?,?)",
						DbManager.mSite , DbManager.colID,
						DbManager.colName,DbManager.colSiteLocation, DbManager.colSiteIdCustomer);

				DbRepository.getInstance().open(MyApplication.getContext());
				SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);

				stmt.bindLong(1, id);
				bindAndCheckNullString(stmt, 2, name);
				bindAndCheckNullString(stmt, 3, locationStr);
				bindAndCheckNullString(stmt, 4,site_id_customer);
				stmt.executeInsert();
				stmt.close();
				break;
			}
			default: {

				DebugLog.d("id="+id+" name="+name+" locationStr="+locationStr);
				String sql = String.format("INSERT OR REPLACE INTO %s(%s,%s,%s) VALUES(?,?,?)",
						DbManager.mSite , DbManager.colID,
						DbManager.colName,DbManager.colSiteLocation);

				DbRepository.getInstance().open(MyApplication.getContext());
				SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);

				stmt.bindLong(1, id);
				bindAndCheckNullString(stmt, 2, name);
				bindAndCheckNullString(stmt, 3, locationStr);

				stmt.executeInsert();
				stmt.close();
				break;
			}
		}

	}

	public static void delete(Context ctx){
		DbRepository.getInstance().open(ctx);
		String sql = "DELETE FROM " + DbManager.mSite;
		SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);
		stmt.executeUpdateDelete();
		stmt.close();
		DbRepository.getInstance().close();
	}

	private SiteModel getSiteFromCursor(Cursor c) {
		SiteModel siteModel = null;

		if (null == c)
			return siteModel;

		siteModel = new SiteModel();
		siteModel.id = (c.getInt(c.getColumnIndex(DbManager.colID)));
		siteModel.name = (c.getString(c.getColumnIndex(DbManager.colName)));
		siteModel.locationStr = (c.getString(c.getColumnIndex(DbManager.colSiteLocation)));

		if (DbManager.schema_version == 9) {
			siteModel.site_id_customer = (c.getString(c.getColumnIndex(DbManager.colSiteIdCustomer)));
		}

		return siteModel;
	}
	
	public static String createDB(){
		switch (DbManager.schema_version) {
			case 9: {

				return "create table if not exists " + DbManager.mSite
						+ " (" + DbManager.colID + " integer, "
						+ DbManager.colName + " varchar, "
						+ DbManager.colSiteLocation + " varchar, "
						+ DbManager.colSiteIdCustomer + " varchar, " // added in patch 9
						+ "PRIMARY KEY (" + DbManager.colID + "))";
			}
			default: {

				return "create table if not exists " + DbManager.mSite
						+ " (" + DbManager.colID + " integer, "
						+ DbManager.colName + " varchar, "
						+ DbManager.colSiteLocation + " varchar, "
						+ "PRIMARY KEY (" + DbManager.colID + "))";
			}
		}
	}

}
