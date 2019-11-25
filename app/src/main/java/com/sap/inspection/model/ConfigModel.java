package com.sap.inspection.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;

import com.sap.inspection.TowerApplication;
import com.sap.inspection.tools.DebugLog;

import org.parceler.Parcel;

@Parcel
public class ConfigModel extends BaseModel {
    public String configName;
    public String configData;

    public ConfigModel() {}

    public enum CONFIG_ENUM {
        IMBAS_PETIR_CONFIG,
        CORRECTIVE_SCHEDULES_AND_ITEMS_CONFIG
    }

    public static ConfigModel getConfig(String[] where, String[] args) {

        ConfigModel result = null;

        String table = DbManager.mConfig;
        String[] columns = null;
        StringBuilder selection = new StringBuilder();
        String whereSelection;
        String[] arguments = args;
        Cursor cursor;

        DebugLog.d("get config by : ");

        for (int i = 0; i < where.length; i++) {

            DebugLog.d("- where " + where[i] + " = " + args[i]);
            selection.append(where[i]).append("=?");

            if (i != where.length - 1) {
                selection.append(" AND ");
            }
        }

        whereSelection = new String(selection);

        DbRepository.getInstance().open(TowerApplication.getInstance());
        cursor = DbRepository.getInstance().getDB().query(true, table, columns, whereSelection, arguments, null, null, null, null);

        if (!cursor.moveToFirst()) {
            cursor.close();
            DbRepository.getInstance().close();
            return result;
        }

        result = getConfigModelFromCursor(cursor);

        DebugLog.d("== config name : " + result.configName);
        DebugLog.d("== config data : " + result.configData);

        cursor.close();
        DbRepository.getInstance().close();
        return result;
    }

    public static void save(@NonNull String configName, String configData) {

        DebugLog.d("configName = " + configName);
        DebugLog.d("configData = " + configData);

        String sql = String.format("INSERT OR REPLACE INTO %s(%s,%s) VALUES(?,?)", DbManager.mConfig, DbManager.colConfigName, DbManager.colConfigData);

        DbRepository.getInstance().open(TowerApplication.getInstance());
        SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);

        stmt.bindString(1, configName);
        bindAndCheckNullString(stmt, 2, configData);

        stmt.executeInsert();
        stmt.close();
        DbRepository.getInstance().close();
    }

    public static void delete(){

        DbRepository.getInstance().open(TowerApplication.getInstance());
        String sql = "DELETE FROM " + DbManager.mConfig;
        SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);
        stmt.executeUpdateDelete();
        stmt.close();
        DbRepository.getInstance().close();
    }

    public static ConfigModel getConfigModelFromCursor(Cursor c) {
        ConfigModel configModel = null;

        if (null == c)
            return configModel;

        configModel = new ConfigModel();
        configModel.configName= (c.getString(c.getColumnIndex(DbManager.colConfigName)));
        configModel.configData = (c.getString(c.getColumnIndex(DbManager.colConfigData)));

        return configModel;
    }

    public static String createDB(){
        return "create table if not exists " + DbManager.mConfig
                + " (" + DbManager.colConfigName + " varchar, "
                + DbManager.colConfigData + " varchar, "
                + "PRIMARY KEY (" + DbManager.colConfigName + "))";
    }

}
