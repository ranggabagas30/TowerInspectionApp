package com.sap.inspection.util;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.sap.inspection.BuildConfig;
import com.sap.inspection.R;
import com.sap.inspection.tools.DebugLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class FileUtil {

    /** Backup local SQLite Database **/
    public static void copyDB(Context context, String dbname, String dstName){
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
            if (sd.canWrite()) {
                String currentDBPath = "//data//"+ BuildConfig.APPLICATION_ID+ "//databases//"+dbname;
                String backupDBPath = dstName;
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);
                DebugLog.d("external dir : "+backupDB.getPath());
                DebugLog.d("database path : "+currentDB.getPath());

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
            //string copy database sukses
            Toast.makeText(context, context.getString(R.string.copydatabasesuccess), Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            DebugLog.e(e.getMessage());
            DebugLog.e(e.getCause().getMessage());

            //string copy database gagal
            Toast.makeText(context, context.getString(R.string.copydatabasefailed), Toast.LENGTH_SHORT).show();
        }
    }

    public static void copyDB2(String dbname,String dstName){
        try {
//	        File sd = Environment.getExternalStorageDirectory();
//	        File data = Environment.getDataDirectory();
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
            if (sd.canWrite()) {
                String currentDBPath = "//data//"+ BuildConfig.APPLICATION_ID+ "//databases//"+dbname;
                String backupDBPath = dstName;
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);
                DebugLog.d("external dir : "+backupDB.getPath());
                DebugLog.d("database path : "+currentDB.getPath());

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {
            DebugLog.e(e.getMessage());
            DebugLog.e(e.getCause().getMessage());
        }
    }
}
