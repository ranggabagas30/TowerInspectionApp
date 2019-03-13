package com.sap.inspection.task;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

import com.sap.inspection.R;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.CommonUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadFileFromURL extends AsyncTask<String, String, Boolean> {

    /**
     * Before starting background thread
     * Show Progress Bar Dialog
     */
    private Activity activity;
    private Context context;
    private ProgressDialog pDialog;
    private SharedPreferences prefs;
    private Handler responseHandler;

    public DownloadFileFromURL(Activity activity, Context context, Handler responseHandler) {
        this.activity = activity;
        this.context = context;
        this.responseHandler = responseHandler;

        pDialog = new ProgressDialog(context);
        pDialog.setMessage(context.getString(R.string.downloadfile));
        pDialog.setIndeterminate(false);
        pDialog.setMax(100);
        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setCancelable(false);

        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog.show();
    }

    /**
     * Downloading file in background thread
     */
    @Override
    protected Boolean doInBackground(String... f_url) {
        int count;
        try {
            URL url = new URL(f_url[0]);
            URLConnection conection = url.openConnection();
            conection.connect();
            // this will be useful so that you can show a tipical 0-100% progress bar
            int lenghtOfFile = conection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream(), 8192);

            File tempDir;
            if (CommonUtil.isExternalStorageAvailable()) {
                DebugLog.d("external storage available");
                tempDir = Environment.getExternalStorageDirectory();
                DebugLog.d("temp dir present");
            } else {
                DebugLog.d("external storage not available");
                tempDir = context.getFilesDir();
            }

            DebugLog.d("asign temp dir");
            tempDir = new File(tempDir.getAbsolutePath() + "/Download");
            DebugLog.d("get temp dir");
            if (!tempDir.exists()) {
                tempDir.mkdir();
            }
            DebugLog.d("get exist dir");
            // Output stream
            OutputStream output = new FileOutputStream(tempDir.getAbsolutePath() + "/sapInspection" + prefs.getString(context.getString(R.string.latest_version), "") + ".apk");
            DebugLog.d("get output sream");
            byte data[] = new byte[1024];

            long total = 0;
            DebugLog.d("start download");
            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
                publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                // writing data to file
                output.write(data, 0, count);
            }
            DebugLog.d("finish download");
            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();

            return true;

        } catch (Exception e) {
            DebugLog.e(e.getMessage());
        }

        return false;
    }

    /**
     * Updating progress bar
     */
    protected void onProgressUpdate(String... progress) {
        // setting progress percentage
        pDialog.setProgress(Integer.parseInt(progress[0]));
    }

    /**
     * After completing background task
     * Dismiss the progress dialog
     **/

    @Override
    protected void onPostExecute(Boolean isSuccessful) {
        pDialog.dismiss();

        Bundle bundle = new Bundle();
        bundle.putBoolean("issuccessful", isSuccessful);

        Message message = new Message();
        message.setData(bundle);

        responseHandler.sendMessage(message);

        //CommonUtil.installAPK(activity, context);
    }
}
