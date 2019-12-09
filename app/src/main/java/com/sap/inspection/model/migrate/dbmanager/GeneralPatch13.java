package com.sap.inspection.model.migrate.dbmanager;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.sap.inspection.BuildConfig;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.model.DbManager;
import com.sap.inspection.tools.DebugLog;

public class GeneralPatch13 extends DBPatch {

    /**
     * 15 Nov 2019
     * SAP: add 'ttNumber'
     *
     * 9 Deecember 2019
     * STP: add 'siteIdCustomer' if not exists
     * */
    @Override
    public void apply(SQLiteDatabase db) {
        Log.d(getClass().getName(), "general patch 13");
        if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {
            String alter = "ALTER TABLE " + DbManager.mSchedule + " ADD COLUMN " + DbManager.colTTNumber + " VARCHAR";
            db.execSQL(alter);
        } else {
            // execute add column "siteIdCustomer. If has been existed, then catch the exception and ignore it
            try {
                db.execSQL("ALTER TABLE "+ DbManager.mSite+" ADD COLUMN "+DbManager.colSiteIdCustomer+" VARCHAR");
            } catch (SQLiteException e) {
                DebugLog.e(e.getMessage(), e);
            }
        }
    }

    @Override
    public void revert(SQLiteDatabase db) {
        Log.d(getClass().getName(), "revert patch to 12");

        if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {
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

        // no revert patch for site table STP version
    }
}
