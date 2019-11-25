package com.sap.inspection.model;

import android.content.Context;
import android.database.sqlite.SQLiteStatement;

import com.sap.inspection.TowerApplication;

import org.parceler.Parcel;

@Parcel
public class LoginLogModel extends BaseModel {
	public String id;
	public String userName;
	public String time;
	public String statusLogin;
	public String fileName;

	public LoginLogModel() { }

	public void save(Context context) {

		if (!DbRepository.getInstance().getDB().isOpen())
			DbRepository.getInstance().open(TowerApplication.getInstance());
		String sql = String
				.format("INSERT OR REPLACE INTO %s(%s,%s,%s,%s,%s) VALUES(?,?,?,?,?)",
						DbManager.mLoginLogs , DbManager.colID,
						DbManager.colUserName, DbManager.colTime, 
						DbManager.colFileName, DbManager.colStatusLogin);
		SQLiteStatement stmt = DbRepository.getInstance().getDB()
				.compileStatement(sql);

		bindAndCheckNullString(stmt, 1, id);
		bindAndCheckNullString(stmt, 2, userName);
		bindAndCheckNullString(stmt, 3, time);
		bindAndCheckNullString(stmt, 4, fileName);
		bindAndCheckNullString(stmt, 5, statusLogin);

		stmt.executeInsert();
		stmt.close();
		DbRepository.getInstance().close();
	}

	public static void delete(Context ctx){
		if (!DbRepository.getInstance().getDB().isOpen())
			DbRepository.getInstance().open(TowerApplication.getInstance());
		String sql = "DELETE FROM " + DbManager.mLoginLogs;
		SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);
		stmt.executeUpdateDelete();
		stmt.close();
		DbRepository.getInstance().close();
	}

	public static String createDB(){
		return "create table if not exists " + DbManager.mUsers
				+ " (" + DbManager.colID + " varchar, "
				+ DbManager.colUserName + " varchar, "
				+ DbManager.colTime + " varchar, "
				+ DbManager.colFileName + " varchar, "
				+ DbManager.colStatusLogin + " varchar, "
				+ "PRIMARY KEY (" + DbManager.colID + "))";
	}
}