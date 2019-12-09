package com.sap.inspection.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.sap.inspection.BuildConfig;
import com.sap.inspection.TowerApplication;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.util.DbUtil;

import org.parceler.Parcel;

import java.util.ArrayList;

@Parcel
public class SiteModel extends BaseModel {
	
    public int id;
    public String name;
    public String locationStr;
    public String site_id_customer;
	public String color_rtpo; // SAP ONLY
	public LocationModel location;

	public static SiteModel getSites(int limit) {
		SiteModel model = null;

		String table = DbManager.mSite;
		String[] columns = null;
		String where = "LIMIT =? ";
		String[] args = new String[]{String.valueOf(limit)};
		Cursor cursor;

		DbRepository.getInstance().open(TowerApplication.getInstance());
		cursor = DbRepository.getInstance().getDB().query(true, table, columns, where, args, null, null,null, null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			return model;
		}

		model = getSiteFromCursor(cursor);

		cursor.close();
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

		DbRepository.getInstance().open(TowerApplication.getInstance());
		cursor = DbRepository.getInstance().getDB().query(true, table, columns, where, args, null, null,null, null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			return model;
		}

		model = getSiteFromCursor(cursor);

		cursor.close();
		DbRepository.getInstance().close();
		return model;
	}
	
	public void save(){

		String sql;
		String format;
		StringBuilder formatBuilder = new StringBuilder("INSERT OR REPLACE INTO %s(");
		StringBuilder valuesBuilder = new StringBuilder(" VALUES(");
		ArrayList<String> argsList = new ArrayList<>();
		argsList.add(DbManager.mSite);
		argsList.add(DbManager.colID);
		argsList.add(DbManager.colName);
		argsList.add(DbManager.colSiteLocation);

		if (DbManager.schema_version >= 9) {
			argsList.add(DbManager.colSiteIdCustomer);
		}

		if (DbManager.schema_version >= 11) {
			if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP))
				argsList.add(DbManager.colColorRTPO);
		}

		String[] args = new String[argsList.size()];
		args = argsList.toArray(args);

		int size = argsList.size() - 1;
		for (int i = 0; i < size; i++) {
			formatBuilder.append("%s");
			valuesBuilder.append("?");
			if (i < size - 1) {
				formatBuilder.append(",");
				valuesBuilder.append(",");
			}
		}
		formatBuilder.append(")");
		valuesBuilder.append(")");

		format = new String(formatBuilder) + new String(valuesBuilder);
		sql = String.format(format, (Object[]) args);

		DbRepository.getInstance().open(TowerApplication.getInstance());
		SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);

		stmt.bindLong(DbUtil.getColIndex(argsList, DbManager.colID), id);
		bindAndCheckNullString(stmt, DbUtil.getColIndex(argsList, DbManager.colName), name);
		bindAndCheckNullString(stmt, DbUtil.getColIndex(argsList, DbManager.colSiteLocation), locationStr);

		if (DbManager.schema_version >= 9) {
			bindAndCheckNullString(stmt, DbUtil.getColIndex(argsList, DbManager.colSiteIdCustomer), site_id_customer);
		}

		if (DbManager.schema_version >= 11) {
			if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP))
				bindAndCheckNullString(stmt, DbUtil.getColIndex(argsList, DbManager.colColorRTPO), color_rtpo);
		}

		stmt.executeInsert();
		stmt.close();
		DbRepository.getInstance().close();
	}

	public static void delete(Context ctx){

		DbRepository.getInstance().open(TowerApplication.getInstance());
		String sql = "DELETE FROM " + DbManager.mSite;
		SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);
		stmt.executeUpdateDelete();
		stmt.close();
		DbRepository.getInstance().close();
	}

	private static SiteModel getSiteFromCursor(Cursor c) {
		SiteModel siteModel = null;

		if (null == c)
			return siteModel;

		siteModel = new SiteModel();
		siteModel.id = (c.getInt(c.getColumnIndex(DbManager.colID)));
		siteModel.name = (c.getString(c.getColumnIndex(DbManager.colName)));
		siteModel.locationStr = (c.getString(c.getColumnIndex(DbManager.colSiteLocation)));

		if (DbManager.schema_version >= 9) {
			siteModel.site_id_customer = (c.getString(c.getColumnIndex(DbManager.colSiteIdCustomer)));
		}

		if (DbManager.schema_version >= 11) {
			if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP))
				siteModel.color_rtpo = (c.getString(c.getColumnIndex(DbManager.colColorRTPO)));
		}

		return siteModel;
	}
	
	public static String createDB(){

		StringBuilder createTableBuilder = new StringBuilder("create table if not exists " + DbManager.mSite + " ("
				+ DbManager.colID + " integer, "
				+ DbManager.colName + " varchar, "
				+ DbManager.colSiteLocation + " varchar, ");

		if (DbManager.schema_version >= 9) {
			createTableBuilder.append(DbManager.colSiteIdCustomer + " varchar, ");
		}

		if (DbManager.schema_version >= 11) {
			if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP))
				createTableBuilder.append(DbManager.colColorRTPO + " varchar, ");
		}

		createTableBuilder.append("PRIMARY KEY (" + DbManager.colID + "))");

		return createTableBuilder.toString();
	}

}
