package com.sap.inspection.model.migrate;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.sap.inspection.model.DbManager;

public class GeneralPatch9 extends DBPatch {

    /**
     * 8 May 2018
     * add column site_id_customer to Site table
     * */
    @Override
    public void apply(SQLiteDatabase db) {
        Log.d(getClass().getName(), "general patch 9");
        db.execSQL("ALTER TABLE "+ DbManager.mSite+" ADD COLUMN "+DbManager.colSiteIdCustomer+" VARCHAR");
    }

    @Override
    public void revert(SQLiteDatabase db) {
        Log.d(getClass().getName(), "revert patch to 8");
        db.execSQL("ALTER TABLE " + DbManager.mSite + " RENAME TO " + DbManager.mSite +"temp");
        DbManager.createDB(db);
        db.execSQL("INSERT INTO " + DbManager.mSite + " SELECT "
                + DbManager.colID + ", "
                + DbManager.colName + ", "
                + DbManager.colSiteLocation
                + " FROM " + DbManager.mSite +"temp");
        db.execSQL("DROP TABLE " + DbManager.mSite+"temp");
    }
}
