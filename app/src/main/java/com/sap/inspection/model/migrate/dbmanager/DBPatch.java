package com.sap.inspection.model.migrate.dbmanager;

import android.database.sqlite.SQLiteDatabase;

public abstract class DBPatch {
	public abstract void apply(SQLiteDatabase db);
	public abstract void revert(SQLiteDatabase db);
}
