package com.sap.inspection.util;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.widget.Toast;

import com.sap.inspection.BuildConfig;
import com.sap.inspection.R;
import com.sap.inspection.TowerApplication;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.tools.DebugLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
            Toast.makeText(context, context.getString(R.string.success_copydatabase), Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            DebugLog.e(e.getMessage());
            DebugLog.e(e.getCause().getMessage());

            //string copy database gagal
            Toast.makeText(context, context.getString(R.string.failed_copydatabase), Toast.LENGTH_SHORT).show();
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

    public static Uri getUriFromFile(Context context, File file) throws IllegalArgumentException {
        DebugLog.d("file format : " + file.toString());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(context, Constants.APPLICATION_FILE_PROVIDER, file);
        } else {
            return Uri.fromFile(file);
        }
    }

    public static File createTemporaryPhotoFile(String part, String ext, String path) throws IOException, IllegalArgumentException {
        File photoDir = getDir(path);
        File createdFile = File.createTempFile(part, ext, photoDir);
        DebugLog.d("created file : " + createdFile.getPath());
        return createdFile;
    }

    public static File getDir(String path) throws NullPointerException {
        if (TextUtils.isEmpty(path))
            throw new NullPointerException("empth path");

        File tempDir = new File(path);
        if (!tempDir.exists()) {
            String createDirMessage = TowerApplication.getContext().getString(R.string.failed_createdir);
            boolean createDirStatus = tempDir.mkdirs();
            if (createDirStatus)
                createDirMessage = TowerApplication.getContext().getString(R.string.success_createdir);
            DebugLog.d(createDirMessage);
        }
        DebugLog.d("dir : " + path);
        return tempDir;
    }

    public static boolean isStorageAvailableAndWriteable(Context context) {

        boolean isAvailable = false;
        boolean isWriteable = false;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            isAvailable = isWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            isAvailable = true;
            Toast.makeText(context, context.getString(R.string.failed_storageisreadonly), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, context.getString(R.string.failed_storageisnotavailble), Toast.LENGTH_LONG).show();
        }
        return isAvailable & isWriteable;
    }


}
