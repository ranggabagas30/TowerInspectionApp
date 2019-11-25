package com.sap.inspection.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.sap.inspection.TowerApplication;

import org.parceler.Parcel;

@Parcel
public class RoleModel extends BaseModel {
	public String id;
	public String roleName;

	public RoleModel() { }

	public void save(Context ctx) {

		DbRepository.getInstance().open(TowerApplication.getInstance());
		String sql = String
				.format("INSERT OR REPLACE INTO %s(%s,%s) VALUES(?,?)",
						DbManager.mRoles , DbManager.colID,
						DbManager.colRoleName);
		SQLiteStatement stmt = DbRepository.getInstance().getDB()
				.compileStatement(sql);

		bindAndCheckNullString(stmt, 1, id);
		bindAndCheckNullString(stmt, 2, roleName);

		stmt.executeInsert();
		stmt.close();
		DbRepository.getInstance().close();
	}

	public static void delete(Context ctx){

		DbRepository.getInstance().open(TowerApplication.getInstance());
		String sql = "DELETE FROM " + DbManager.mRoles;
		SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);
		stmt.executeUpdateDelete();
		stmt.close();
		DbRepository.getInstance().close();
	}

	public RoleModel getRoleModel(Context context, String roleID) {

		RoleModel result = null;

		String table = DbManager.mRoles;
		String[] columns = null;
		String where = DbManager.colID + "=?";
		String[] args = {roleID};
		Cursor cursor;

		DbRepository.getInstance().open(TowerApplication.getInstance());
		cursor = DbRepository.getInstance().getDB().query(true, table, columns, where, args, null, null, null, null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			DbRepository.getInstance().close();
			return result;
		}

		result = getUserFromCursor(cursor);
		
		cursor.close();
		DbRepository.getInstance().close();
		
		return result;
	}

	private RoleModel getUserFromCursor(Cursor c) {
		RoleModel user = new RoleModel();

		if (null == c)
			return user;

		user.id = (c.getString(c.getColumnIndex(DbManager.colID)));
		user.roleName = (c.getString(c.getColumnIndex(DbManager.colRoleName)));

		return user;
	}

	public static String createDB(){
		return "create table if not exists " + DbManager.mRoles
				+ " (" + DbManager.colID + " varchar, "
				+ DbManager.colRoleName + " varchar, "
				+ "PRIMARY KEY (" + DbManager.colID + "))";
	}
}
