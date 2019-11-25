package com.sap.inspection.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.sap.inspection.TowerApplication;
import com.sap.inspection.tools.DebugLog;

import org.parceler.Parcel;

@Parcel
public class TokenModel extends BaseModel {

	public String accToken;

	public TokenModel() { }

	public void save(Context ctx) {
		DebugLog.d(accToken);
		accToken = accToken.replace("access_token", "oauth_token");
		DebugLog.d(accToken);
		if (!DbRepository.getInstance().getDB().isOpen())
			DbRepository.getInstance().open(TowerApplication.getInstance());
		String sql = String.format("INSERT OR REPLACE INTO %s(%s) VALUES(?)",
						DbManager.mTokenTable, DbManager.colAccToken);
		SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);

		bindAndCheckNullString(stmt, 1, accToken);
		stmt.executeInsert();
		stmt.close();
		DbRepository.getInstance().close();
	}

	public static void delete(Context ctx){
		if (!DbRepository.getInstance().getDB().isOpen())
			DbRepository.getInstance().open(TowerApplication.getInstance());
		String sql = "DELETE FROM " + DbManager.mTokenTable;
		SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);
		stmt.executeUpdateDelete();
		stmt.close();
		DbRepository.getInstance().close();
	}
	
	public static String getAccTokenFromDB(Context context) {
		//DbRepository.getInstance().open(context);
		try {

			Cursor c = DbRepository
					.getInstance()
					.getDB()
					.query(DbManager.mTokenTable, null, null, null, null,
							null, null, null);

			if (!c.moveToFirst()) {
				//DbRepository.getInstance().close();
				return null;
			}

			String accToken = getFromCursor(c);
			c.close();
			//DbRepository.getInstance().close();
			return accToken;
		} catch (Exception e) {
			//DbRepository.getInstance().close();
			return null;
		}
	}

	private static String getFromCursor(Cursor c) {
		if (null == c)
			return null;
		
		return (c.getString(c.getColumnIndex(DbManager.colAccToken)));
	}
	
	public static String createDB(){
		return "create table if not exists " + DbManager.mTokenTable
				+ " (" + DbManager.colAccToken + " varchar )";
				
	}
}
