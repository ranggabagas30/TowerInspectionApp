package com.sap.inspection.model.migrate;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.sap.inspection.model.DbManager;

public class GeneralPatch7 extends DBPatch{

	@Override
	public void apply(SQLiteDatabase db) {
		Log.d(getClass().getName(), "general patch 7");
		db.execSQL("ALTER TABLE "+DbManager.mWorkFormItem+
				" ADD COLUMN "+DbManager.colSearch+" INTEGER DEFAULT 0,"+
				" ADD COLUMN "+DbManager.colExpand+" INTEGER DEFAULT 0"
		);
	}

	@Override
	public void revert(SQLiteDatabase db) {
		db.execSQL("ALTER TABLE "+DbManager.mWorkFormItem+
				" DROP COLUMN "+DbManager.colSearch+" INTEGER DEFAULT 0,"+
				" DROP COLUMN "+DbManager.colExpand+" INTEGER DEFAULT 0"
		);
	}
}