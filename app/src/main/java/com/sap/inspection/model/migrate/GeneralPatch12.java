package com.sap.inspection.model.migrate;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.sap.inspection.model.DbManager;

public class GeneralPatch12 extends DBPatch {

    /**
     * 28 Oct, 2019
     * add 'colRejection'
     * */
    @Override
    public void apply(SQLiteDatabase db) {
        Log.d(getClass().getName(), "general patch 12");
        db.execSQL("ALTER TABLE " + DbManager.mSchedule +  " ADD COLUMN " + DbManager.colRejection + " VARCHAR");
    }

    @Override
    public void revert(SQLiteDatabase db) {
        Log.d(getClass().getName(), "revert patch to 9");
        db.execSQL("ALTER TABLE " + DbManager.mSchedule + " RENAME TO " + DbManager.mSchedule + "temp");
        DbManager.createDB(db);
        db.execSQL("INSERT INTO " + DbManager.mSchedule + " SELECT "
                + DbManager.colID + ", "
                + DbManager.colName + ", "
                + DbManager.colSiteLocation
                + " FROM " + DbManager.mSchedule +"temp");
        db.execSQL("DROP TABLE " + DbManager.mSchedule+"temp");
    }
}
