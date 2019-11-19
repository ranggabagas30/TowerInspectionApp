package com.sap.inspection.model.migrate;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.sap.inspection.BuildConfig;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.model.DbManager;

public class GeneralPatch13 extends DBPatch {

    /**
     * 15 Nov 2019
     * add 'ttNumber'
     * */
    @Override
    public void apply(SQLiteDatabase db) {
        Log.d(getClass().getName(), "apply general patch 13");
        if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {
            String alter = "ALTER TABLE " + DbManager.mSchedule + " ADD COLUMN " + DbManager.colTTNumber + " VARCHAR";
            db.execSQL(alter);
        }
    }

    @Override
    public void revert(SQLiteDatabase db) {
        Log.d(getClass().getName(), "revert patch to 12");
        String alterOldTableName = "ALTER TABLE " + DbManager.mSchedule + " RENAME TO " + DbManager.mSchedule + "temp";
        db.execSQL(alterOldTableName);
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
                        + DbManager.colOperatorNumber + ", "
                        + DbManager.colRejection);

        if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {
            scheduleColumns.append(", " + DbManager.colHiddenItemIds);
        }

        insertBuilder.append(scheduleColumns.toString())
                .append(" FROM " + DbManager.mSchedule + "temp");

        db.execSQL(insertBuilder.toString());
        db.execSQL("DROP TABLE " + DbManager.mSchedule+"temp");
    }
}
