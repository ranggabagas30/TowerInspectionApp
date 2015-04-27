package com.sap.inspection.model.migrate;

import com.sap.inspection.model.DbManager;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class GeneralDropCreatePatch extends DBPatch{

	@Override
	public void apply(SQLiteDatabase db) {
		Log.d(getClass().getName(), "do drop and then create again");
		DbManager.dropTable(db);
		DbManager.createDB(db);
	}

	@Override
	public void revert(SQLiteDatabase db) {
		
	}

}
