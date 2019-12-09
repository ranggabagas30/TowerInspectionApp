package com.sap.inspection.model.migrate.dbmanager;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.sap.inspection.model.DbManager;

public class GeneralPatch6 extends DBPatch{

	@Override
	public void apply(SQLiteDatabase db) {
		Log.d(getClass().getName(), "general patch 6");
		db.execSQL("ALTER TABLE "+DbManager.mWorkFormItem+" ADD COLUMN "+DbManager.colDisable+" INTEGER DEFAULT 0");
	}

	@Override
	public void revert(SQLiteDatabase db) {
		db.execSQL("ALTER TABLE "+DbManager.mWorkFormItem+" DROP COLUMN "+DbManager.colDisable+" INTEGER DEFAULT 0");
	}
}