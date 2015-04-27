package com.sap.inspection.model.migrate;

import android.database.sqlite.SQLiteDatabase;

public class BlankPatch extends DBPatch{

	@Override
	public void apply(SQLiteDatabase db) {
		
	}

	@Override
	public void revert(SQLiteDatabase db) {
		
	}

}
