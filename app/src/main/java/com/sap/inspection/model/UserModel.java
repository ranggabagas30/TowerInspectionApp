package com.sap.inspection.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.widget.Toast;

import com.sap.inspection.TowerApplication;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.tools.MD5;

import org.parceler.Parcel;

@Parcel
public class UserModel extends BaseModel {
	public String id;
	public String username;
	public String password;
	public String email;
	public String persistence_token;
	public String full_name;
	public String last_sign_in_ip;
	public String last_sign_in_at;
	public int sign_in_count;
	public int failed_attempts;
	public RoleModel role;

	public UserModel() { }

	public void printUserValue() {

		DebugLog.d("id : " + id);
		DebugLog.d("username : " + username);
		DebugLog.d("email : " + email);
		DebugLog.d("full_name : " + full_name);
		DebugLog.d("persistence_token : " + persistence_token);
		DebugLog.d("role_name : " + role);
	}

	public void save(Context context) {

		DbRepository.getInstance().open(TowerApplication.getInstance());

		String md5 = MD5.md5(password);
		if (md5 == null){
			Toast.makeText(context, "Error on encripting password", Toast.LENGTH_SHORT).show();
			return;
		}

		String sql = String
				.format("INSERT OR REPLACE INTO %s(%s,%s,%s,%s) VALUES(?,?,?,?)",
						DbManager.mUsers , DbManager.colID,
						DbManager.colUserName,DbManager.colPassword, 
						DbManager.colRoleID);
		SQLiteStatement stmt = DbRepository.getInstance().getDB()
				.compileStatement(sql);

		bindAndCheckNullString(stmt, 1, id);
		bindAndCheckNullString(stmt, 2, username);
		bindAndCheckNullString(stmt, 3, md5);
		bindAndCheckNullString(stmt, 4, role.id);

		stmt.executeInsert();
		stmt.close();
		DbRepository.getInstance().close();
	}

	public static void delete(Context ctx){
		DbRepository.getInstance().open(TowerApplication.getInstance());
		String sql = "DELETE FROM " + DbManager.mUsers;
		SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);
		stmt.executeUpdateDelete();
		stmt.close();
		DbRepository.getInstance().close();
	}


	public UserModel getUserModel(Context context, String userName, String password) {

		String md5 = MD5.md5(password);
		if (md5 == null){
			Toast.makeText(context, "Error on encripting password", Toast.LENGTH_SHORT).show();
			return null;
		}

		UserModel result = null;

		String table = DbManager.mUsers;
		String[] columns = null;
		String where = DbManager.colUserName + "=? AND "+ DbManager.colPassword + "=? ";
		String[] args = {userName , md5};
		Cursor cursor;

		DbRepository.getInstance().open(TowerApplication.getInstance());

		cursor = DbRepository.getInstance().getDB().query(true, table, columns, where, args, null, null, null, null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			DbRepository.getInstance().close();
			return result;
		}

		result = getUserFromCursor(cursor,context);
		
		cursor.close();
		DbRepository.getInstance().close();
		
		return result;
	}
	
	public UserModel getFirstUser(Context context) {

		UserModel result = null;

		String table = DbManager.mUsers;
		String[] columns = null;
		String where =null;
		String[] args = null;
		Cursor cursor;

		DbRepository.getInstance().open(TowerApplication.getInstance());
		cursor = DbRepository.getInstance().getDB().query(true, table, columns, where, args, null, null, null, null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			DbRepository.getInstance().close();
			return result;
		}

		result = getUserFromCursor(cursor,context);
		
		cursor.close();
		DbRepository.getInstance().close();
		
		return result;
	}
	
	
	
	public int countUser(Context context) {

		String table = DbManager.mUsers;
		String[] columns = null;
		String where = null;
		String[] args = null;
		Cursor cursor;

		int result;

		DbRepository.getInstance().open(TowerApplication.getInstance());
		cursor = DbRepository.getInstance().getDB().query(true, table, columns, where, args, null, null, null, null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			DbRepository.getInstance().close();
			return 0;
		}

		result = cursor.getCount();
		
		cursor.close();
		DbRepository.getInstance().close();
		
		return result;
	}

	private UserModel getUserFromCursor(Cursor c, Context context) {
		UserModel user = new UserModel();

		if (null == c)
			return user;

		user.id = (c.getString(c.getColumnIndex(DbManager.colID)));
		user.username = (c.getString(c.getColumnIndex(DbManager.colUserName)));
//		user.password = (c.getString(c.getColumnIndex(DbManager.colPassword)));

		//get role
		user.role = new RoleModel();
		user.role.id = (c.getString(c.getColumnIndex(DbManager.colRoleID)));
		user.role = user.role.getRoleModel(context, user.role.id);
		return user;
	}

	public static String createDB(){
		return "create table if not exists " + DbManager.mUsers
				+ " (" + DbManager.colID + " varchar, "
				+ DbManager.colUserName + " varchar, "
				+ DbManager.colPassword + " varchar, "
				+ DbManager.colRoleID + " varchar, "
				+ "PRIMARY KEY (" + DbManager.colID + "))";
	}
}
