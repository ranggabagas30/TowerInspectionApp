package com.sap.inspection.model.migrate.dbmanager;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.sap.inspection.BuildConfig;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.model.DbManager;

public class GeneralPatch12 extends DBPatch {

    /**
     * 28 Oct 2019
     * add 'colRejection'
     * */
    @Override
    public void apply(SQLiteDatabase db) {
        Log.d(getClass().getName(), "apply general patch 12");
        db.execSQL("ALTER TABLE " + DbManager.mSchedule +  " ADD COLUMN " + DbManager.colRejection + " VARCHAR");
    }

    @Override
    public void revert(SQLiteDatabase db) {
        Log.d(getClass().getName(), "revert patch to 11");
        db.execSQL("ALTER TABLE " + DbManager.mSchedule + " RENAME TO " + DbManager.mSchedule + "temp");
        DbManager.createDB(db);

        StringBuilder insertBuilder = new StringBuilder("INSERT INTO " + DbManager.mSchedule + " SELECT ");
        StringBuilder scheduleColumns = new StringBuilder();
        scheduleColumns.append(
                 DbManager.colID + ", "
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
                + DbManager.colOperatorNumber);

        if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {
            scheduleColumns.append(", " + DbManager.colHiddenItemIds);
        }

        insertBuilder.append(scheduleColumns.toString())
                     .append(" FROM " + DbManager.mSchedule + "temp");

        db.execSQL(insertBuilder.toString());
        db.execSQL("DROP TABLE " + DbManager.mSchedule+"temp");
    }
}
