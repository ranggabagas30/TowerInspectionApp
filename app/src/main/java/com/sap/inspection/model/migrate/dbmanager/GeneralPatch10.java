package com.sap.inspection.model.migrate.dbmanager;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.sap.inspection.BuildConfig;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.model.DbManager;

public class GeneralPatch10 extends DBPatch {

    /**
     * 4 July 2019
     * add column colHiddenItemIds to Schedule table
     * */
    @Override
    public void apply(SQLiteDatabase db) {
        Log.d(getClass().getName(), "general patch 10");
        if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP))
            db.execSQL("ALTER TABLE "+ DbManager.mSchedule+" ADD COLUMN "+DbManager.colHiddenItemIds+" VARCHAR");
    }

    @Override
    public void revert(SQLiteDatabase db) {
        Log.d(getClass().getName(), "revert patch to 9");
        db.execSQL("ALTER TABLE " + DbManager.mSchedule + " RENAME TO " + DbManager.mSchedule +"temp");
        DbManager.createDB(db);
        db.execSQL("INSERT INTO " + DbManager.mSchedule + " SELECT "
                + DbManager.colID + ", "
                + DbManager.colUserId + ", "
                + DbManager.colSiteId + ", "
                + DbManager.colOperatorIds + ", "
                + DbManager.colProjectId + ", "
                + DbManager.colProjectName + ", "
                + DbManager.colWorkTypeId + ", "
                + DbManager.colDayDate + ", "
                + DbManager.colWorkDate + ", "
                + DbManager.colWorkDateStr + ", "
                + DbManager.colProgress + ", "
                + DbManager.colStatus + ", "
                + DbManager.colSumTask + ", "
                + DbManager.colSumDone + ", "
                + DbManager.colOperatorNumber
                + " FROM " + DbManager.mSchedule +"temp");
        db.execSQL("DROP TABLE " + DbManager.mSchedule+"temp");
    }
}
