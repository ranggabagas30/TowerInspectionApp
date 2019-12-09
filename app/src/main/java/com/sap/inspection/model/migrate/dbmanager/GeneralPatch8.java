package com.sap.inspection.model.migrate.dbmanager;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.sap.inspection.model.DbManager;

public class GeneralPatch8 extends DBPatch{

	@Override
	public void apply(SQLiteDatabase db) {
		Log.d(getClass().getName(), "general patch 8");
		db.execSQL("ALTER TABLE "+DbManager.mSchedule+" ADD COLUMN "+DbManager.colOperatorNumber+" INTEGER DEFAULT 0");
	}

	@Override
	public void revert(SQLiteDatabase db) {
		db.execSQL("ALTER TABLE "+DbManager.mSchedule+" DROP COLUMN "+DbManager.colOperatorNumber+" INTEGER DEFAULT 0");
	}
}