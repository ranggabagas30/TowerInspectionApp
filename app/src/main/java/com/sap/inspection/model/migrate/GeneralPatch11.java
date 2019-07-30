package com.sap.inspection.model.migrate;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.sap.inspection.model.DbManager;

public class GeneralPatch11 extends DBPatch {

    /**
     * 29 July 2019
     * add column colHiddenItemIds to Schedule table
     * */
    @Override
    public void apply(SQLiteDatabase db) {
        Log.d(getClass().getName(), "general patch 11");
        db.execSQL("ALTER TABLE "+ DbManager.mSite+" ADD COLUMN "+DbManager.colColorRTPO+" VARCHAR");
    }

    @Override
    public void revert(SQLiteDatabase db) {
        Log.d(getClass().getName(), "revert patch to 10");
        db.execSQL("ALTER TABLE " + DbManager.mSite + " RENAME TO " + DbManager.mSite + "temp");
        DbManager.createDB(db);
        db.execSQL("INSERT INTO " + DbManager.mSite + " SELECT "
                + DbManager.colID + ", "
                + DbManager.colName + ", "
                + DbManager.colSiteLocation
                + " FROM " + DbManager.mSite +"temp");
        db.execSQL("DROP TABLE " + DbManager.mSite+"temp");
    }
}
