package com.sap.inspection;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sap.inspection.connection.APIHelper;
import com.sap.inspection.event.DeleteAllProgressEvent;
import com.sap.inspection.event.DeleteAllScheduleEvent;
import com.sap.inspection.event.ScheduleProgressEvent;
import com.sap.inspection.event.ScheduleTempProgressEvent;
import com.sap.inspection.event.UploadProgressEvent;
import com.sap.inspection.listener.UploadListener;
import com.sap.inspection.manager.DeleteAllDataTask;
import com.sap.inspection.manager.ItemUploadManager;
import com.sap.inspection.model.DbManager;
import com.sap.inspection.model.DbRepository;
import com.sap.inspection.model.form.ColumnModel;
import com.sap.inspection.model.form.RowModel;
import com.sap.inspection.model.form.WorkFormGroupModel;
import com.sap.inspection.model.form.WorkFormModel;
import com.sap.inspection.model.responsemodel.FormResponseModel;
import com.sap.inspection.model.responsemodel.ScheduleResponseModel;
import com.sap.inspection.model.responsemodel.VersionModel;
import com.sap.inspection.model.value.CorrectiveValueModel;
import com.sap.inspection.model.value.DbRepositoryValue;
import com.sap.inspection.model.value.ItemValueModel;
import com.sap.inspection.task.ScheduleSaver;
import com.sap.inspection.task.ScheduleTempSaver;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.tools.DeleteAllDataDialog;
import com.sap.inspection.tools.DeleteAllSchedulesDialog;
import com.sap.inspection.tools.PrefUtil;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;

public class SettingActivity extends BaseActivity implements UploadListener {

    Button update;
    Button updateForm;
    Button upload;
    Button reupload;
    Button delete;
    Button deleteSchedule;
    Button refreshSchedule;
    TextView updateStatus;
    TextView uploadInfo;
    SharedPreferences prefs;
    File tempFile;

    private ProgressDialog pDialog;

    private boolean flagScheduleSaved = false;

    // Progress dialog type (0 - for Horizontal progress bar)
    public static final int progress_bar_type = 0;

    // File url to download
    private static String file_url = "http://api.androidhive.info/progressdialog/hive.jpg";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DebugLog.d("");
        String version = null;
        int versionCode = 0;
        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        DebugLog.d("version Name = " + version + " versionCode = " + versionCode);
        setContentView(R.layout.activity_setting);


        TextView title = (TextView) findViewById(R.id.header_title);
        title.setText("Settings");
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        file_url = prefs.getString(this.getString(R.string.url_update), "");
        // show progress bar button
        update = (Button) findViewById(R.id.update);
        updateForm = (Button) findViewById(R.id.update_form);
        updateStatus = (TextView) findViewById(R.id.updateStatus);
        DebugLog.d("latest_version" + prefs.getString(this.getString(R.string.latest_version), ""));
        DebugLog.d("url_update" + prefs.getString(this.getString(R.string.url_update), ""));
        if (version != null && (version.equalsIgnoreCase(prefs.getString(this.getString(R.string.latest_version), "")) || prefs.getString(this.getString(R.string.url_update), "").equalsIgnoreCase(""))) {
            update.setVisibility(View.VISIBLE);
            update.setEnabled(false);
            update.setText("No New Update");
            update.setBackgroundResource(R.drawable.selector_button_gray_small_padding);
            //updateStatus.setText("No New Update");
        } else {
            update.setVisibility(View.VISIBLE);
            updateStatus.setText("New Update Available");
        }

        upload = (Button) findViewById(R.id.uploadData);
        uploadInfo = (TextView) findViewById(R.id.uploadInfo);
        if (ItemUploadManager.getInstance().getLatestStatus() != null) {
            if (!ItemUploadManager.getInstance().getLatestStatus().equals(ItemUploadManager.getInstance().syncDone) && !ItemUploadManager.getInstance().getLatestStatus().equals(ItemUploadManager.getInstance().syncFail))
                uploadInfo.setText(ItemUploadManager.getInstance().getLatestStatus());
            else
                uploadInfo.setText("Latest status " + ItemUploadManager.getInstance().getLatestStatus().toLowerCase());
        } else
            uploadInfo.setText("Waiting to upload");

        delete = (Button) findViewById(R.id.deleteData);
        delete.setOnClickListener(deleteClickListener);
        deleteSchedule = (Button) findViewById(R.id.deleteSchedule);
        deleteSchedule.setOnClickListener(deleteScheduleClickListener);

        tempFile = Environment.getExternalStorageDirectory();
        tempFile = new File(tempFile.getAbsolutePath() + "/Download/sapInspection" + prefs.getString(SettingActivity.this.getString(R.string.latest_version), "") + ".apk");

        if (tempFile.exists())
            update.setText("Install");

        update.setOnClickListener(updateClickListener);
        updateForm.setOnClickListener(updateFormClickListener);

        upload.setOnClickListener(uploadClickListener);

        reupload = (Button) findViewById(R.id.reuploadData);
        reupload.setOnClickListener(reuploadClickListener);

        refreshSchedule = (Button) findViewById(R.id.updateSchedule);
        refreshSchedule.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                getSchedule();
            }
        });

        findViewById(R.id.setting_logout).setOnClickListener(logoutClickListener);
    }

    OnClickListener deleteClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            DeleteAllDataDialog dialog = new DeleteAllDataDialog(activity);
            dialog.setPositive(positiveDeleteClickListener);
            dialog.show();
        }
    };

    OnClickListener positiveDeleteClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            DeleteAllDataTask task = new DeleteAllDataTask();
            task.execute();
        }
    };

    OnClickListener deleteScheduleClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            DeleteAllSchedulesDialog dialog = new DeleteAllSchedulesDialog(activity);
            dialog.setPositive(positiveDeleteScheduleClickListener);
            dialog.show();
        }
    };

    OnClickListener positiveDeleteScheduleClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            getScheduleTemp();
        }
    };

    OnClickListener updateClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            //Chek if the file already downloaded before
            //			if(!tempFile.exists()){
            new DownloadFileFromURL().execute(file_url);
            //			}
            //			else{
            //				Intent intent = new Intent(Intent.ACTION_VIEW)
            //				.setDataAndType(Uri.fromFile(tempFile),"application/vnd.android.package-archive");
            //				intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
            //				startActivity(intent);
            //			}
        }
    };

    OnClickListener updateFormClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            checkFormVersion();
        }
    };
    //test
    private static String formVersion;

    private void checkFormVersion() {
        pDialog = new ProgressDialog(activity);
        pDialog.setMessage("Check form version");
        pDialog.setCancelable(false);
        pDialog.show();
        APIHelper.getFormVersion(activity, formVersionHandler, getPreference(R.string.user_id, ""));
    }

    private Handler formVersionHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.getData() != null && msg.getData().getString("json") != null) {
                VersionModel model = new Gson().fromJson(msg.getData().getString("json"), VersionModel.class);
                formVersion = model.version;
                DebugLog.d("check version : " + PrefUtil.getStringPref(R.string.user_id, "") + getString(R.string.latest_version_form, ""));
                DebugLog.d("check version value : " + getPreference(PrefUtil.getStringPref(R.string.user_id, "") + getString(R.string.latest_version_form, ""), "no value"));
                DebugLog.d("check version value from web: " + formVersion);
                if (!formVersion.equals(getPreference(PrefUtil.getStringPref(R.string.user_id, "") + getString(R.string.latest_version_form, ""), "no value"))) {
                    pDialog.setMessage("Get new form from server");
                    APIHelper.getForms(activity, formSaverHandler, getPreference(R.string.user_id, ""));
                } else {
                    pDialog.setMessage("Get schedule from server");
                    APIHelper.getSchedules(activity, scheduleHandler, getPreference(R.string.user_id, ""));
                }
            } else {
                pDialog.dismiss();
                Toast.makeText(activity, "Form update failed\nPlease do relogin and have fast internet connection", Toast.LENGTH_LONG).show();
            }
        }

        ;
    };

    private Handler formSaverHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.getData() != null && msg.getData().getString("json") != null) {
                initForm(msg.getData().getString("json"));
            } else {
                pDialog.dismiss();
                Toast.makeText(activity, "Form update failed\nPlease do relogin and have fast internet connection", Toast.LENGTH_LONG).show();
            }
        }

        ;
    };

    private void initForm(String json) {
        Gson gson = new Gson();
        FormResponseModel formResponseModel = gson.fromJson(json, FormResponseModel.class);
        if (formResponseModel.status == 200) {
            FormSaver formSaver = new FormSaver();
            formSaver.execute(formResponseModel.data.toArray());
        }
    }

    private class FormSaver extends AsyncTask<Object, Integer, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Prepare for saving");
            DbRepository.getInstance().open(getApplicationContext());
            DbRepository.getInstance().clearData(DbManager.mWorkFormItem);
            DbRepository.getInstance().clearData(DbManager.mWorkFormOption);
            DbRepository.getInstance().clearData(DbManager.mWorkFormColumn);
            DbRepository.getInstance().clearData(DbManager.mWorkFormRow);
            DbRepository.getInstance().clearData(DbManager.mWorkFormRowCol);
        }

        @Override
        protected Void doInBackground(Object... params) {
            int sum = 0;
            for (int i = 0; i < params.length; i++) {
                if (((WorkFormModel) params[i]).groups != null)
                    for (WorkFormGroupModel group : ((WorkFormModel) params[i]).groups) {
                        if (group.table == null) {
                            continue;
                        }
                        DebugLog.d("group name : " + group.name);
                        DebugLog.d("group table : " + group.table.toString());
                        DebugLog.d("group table header : " + group.table.headers.toString());
                        sum += group.table.headers.size();
                        sum += group.table.rows.size();
                    }
            }

            int curr = 0;
            for (int i = 0; i < params.length; i++) {
                ((WorkFormModel) params[i]).save();
                if (((WorkFormModel) params[i]).groups != null)
                    for (WorkFormGroupModel group : ((WorkFormModel) params[i]).groups) {
                        if (group.table == null) {
                            continue;
                        }
                        for (ColumnModel columnModel : group.table.headers) {
                            curr++;
                            publishProgress(curr * 100 / sum);
                            columnModel.save();
                        }

                        for (RowModel rowModel : group.table.rows) {
                            curr++;
                            publishProgress(curr * 100 / sum);
                            rowModel.save();
                        }
                    }
            }
            DebugLog.d("version saved : " + PrefUtil.getStringPref(R.string.user_id, "") + getString(R.string.latest_version_form, ""));
            DebugLog.d("version saved value : " + getPreference(PrefUtil.getStringPref(R.string.user_id, "") + getString(R.string.latest_version_form, ""), "no value"));
            DebugLog.d("version saved value from web: " + formVersion);
            writePreference(PrefUtil.getStringPref(R.string.user_id, "") + getString(R.string.latest_version_form), formVersion);
            writePreference(PrefUtil.getStringPref(R.string.user_id, "") + getString(R.string.offline_form), "not null");
            DebugLog.d("form ofline user pref: " + PrefUtil.getStringPref(R.string.user_id, "") + getString(R.string.offline_form));
            DebugLog.d("form ofline user : " + getPreference(PrefUtil.getStringPref(R.string.user_id, "") + getString(R.string.offline_form), null));
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            DebugLog.d("saving forms " + values[0] + " %...");
            pDialog.setMessage("saving forms " + values[0] + " %...");
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            //			setFlagFormSaved(true);
            pDialog.setMessage("Get schedule from server");
            APIHelper.getSchedules(activity, scheduleHandler, getPreference(R.string.user_id, ""));
//			DbRepository.getInstance().close();
        }
    }

    OnClickListener uploadClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            CorrectiveValueModel correctiveValueModel = new CorrectiveValueModel();
//			correctiveValueModel.deleteAll(activity);

            DbRepositoryValue.getInstance().open(activity);
            ProgressDialog progressDialog = new ProgressDialog(activity);
            progressDialog.setMessage("preparing item for upload");
            progressDialog.show();
            ItemValueModel itemValueModel = new ItemValueModel();
            ArrayList<ItemValueModel> itemValueModels = itemValueModel.getItemValuesForUpload();

            itemValueModels.addAll(correctiveValueModel.getItemValuesForUpload());
            if (itemValueModels.size() == 0) {
                progressDialog.dismiss();
                Toast.makeText(activity, "There is no new item to upload...", Toast.LENGTH_LONG).show();
                uploadInfo.setText("There is no new item to upload...");
                return;
            }
            int i = 0;
            for (ItemValueModel model : itemValueModels) {
                i++;
                progressDialog.setMessage("preparing " + (100 * i / itemValueModels.size()) + "%");
                model.uploadStatus = ItemValueModel.UPLOAD_ONGOING;
                model.save();
            }
            ItemUploadManager.getInstance().addItemValues(itemValueModels);
            progressDialog.dismiss();
            Toast.makeText(activity, "Upload on progress,..", Toast.LENGTH_LONG).show();
            DbRepositoryValue.getInstance().close();
        }
    };

    OnClickListener reuploadClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
            // set title
            alertDialogBuilder.setTitle("Reupload all data");
            // set dialog message
            alertDialogBuilder
                    .setMessage("Are you sure want to re-upload all data?")
                    .setPositiveButton("Upload", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            uploadInfo.setText("Resetting upload status");
                            ItemValueModel.resetAllUploadStatus();
                            CorrectiveValueModel.resetAllUploadStatus();
                            upload.performClick();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            alertDialogBuilder.create().show();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        tempFile = Environment.getExternalStorageDirectory();
        tempFile = new File(tempFile.getAbsolutePath() + "/Download/sapInspection" + prefs.getString(SettingActivity.this.getString(R.string.latest_version), "") + ".apk");

        if (tempFile.exists())
            update.setText("Install");

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    /**
     * Showing Dialog
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type: // we set this to 0
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Downloading file. Please wait...");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(false);
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }

    /**
     * Background Async Task to download file
     */
    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(progress_bar_type);
        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();
                // this will be useful so that you can show a tipical 0-100% progress bar
                int lenghtOfFile = conection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                File tempDir = Environment.getExternalStorageDirectory();
                tempDir = new File(tempDir.getAbsolutePath() + "/Download");
                if (!tempDir.exists()) {
                    tempDir.mkdir();
                }

                // Output stream
                OutputStream output = new FileOutputStream(tempDir.getAbsolutePath() + "/sapInspection" + prefs.getString(SettingActivity.this.getString(R.string.latest_version), "") + ".apk");

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                DebugLog.e(e.getMessage());
            }

            return null;
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
        protected void onPostExecute(String file_url) {
            dismissDialog(progress_bar_type);

            File tempFile = Environment.getExternalStorageDirectory();
            tempFile = new File(tempFile.getAbsolutePath() + "/Download/sapInspection" + prefs.getString(SettingActivity.this.getString(R.string.latest_version), "") + ".apk");
            if (!tempFile.exists()) {
                finish();
            }

            Intent intent = new Intent(Intent.ACTION_VIEW)
                    .setDataAndType(Uri.fromFile(tempFile), "application/vnd.android.package-archive");
            intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onUpdate(String status) {
        DebugLog.d("====================================================");
        DebugLog.d("====================================================");
        DebugLog.d(status);
        DebugLog.d("====================================================");
        DebugLog.d("====================================================");
        uploadInfo.setText(status);
    }

    @Override
    public void onFailed() {
        uploadInfo.setText(ItemUploadManager.getInstance().syncFail);
    }

    @Override
    public void onSuccess() {
        uploadInfo.setText(ItemUploadManager.getInstance().syncDone);
    }

    private void getScheduleTemp() {
        DbRepository.getInstance().open(activity);
        pDialog = new ProgressDialog(activity);
        pDialog.setMessage("Get schedule from server...");
        pDialog.setCancelable(false);
        pDialog.show();
        APIHelper.getSchedules(activity, scheduleHandlerTemp, getPreference(R.string.user_id, ""));
    }

    private void getSchedule() {
        DbRepository.getInstance().open(activity);
        pDialog = new ProgressDialog(activity);
        pDialog.setMessage("Get schedule from server...");
        pDialog.setCancelable(false);
        pDialog.show();
        APIHelper.getSchedules(activity, scheduleHandler, getPreference(R.string.user_id, ""));
    }

    Handler scheduleHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            Bundle bundle = msg.getData();
            Gson gson = new Gson();
            if (bundle.getString("json") != null) {
                ScheduleResponseModel scheduleResponseModel = gson.fromJson(bundle.getString("json"), ScheduleResponseModel.class);
                if (scheduleResponseModel.status == 200) {
                    ScheduleSaver scheduleSaver = new ScheduleSaver();
                    scheduleSaver.setActivity(activity);
                    scheduleSaver.execute(scheduleResponseModel.data.toArray());
                }
            } else {
                pDialog.dismiss();
                Toast.makeText(activity, "Can't get schedule from server\nPlease get an fast internet connection", Toast.LENGTH_LONG).show();
            }
        }

        ;
    };

    Handler scheduleHandlerTemp = new Handler() {
        public void handleMessage(android.os.Message msg) {
            Bundle bundle = msg.getData();
            Gson gson = new Gson();
            if (bundle.getString("json") != null) {
                ScheduleResponseModel scheduleResponseModel = gson.fromJson(bundle.getString("json"), ScheduleResponseModel.class);
                if (scheduleResponseModel.status == 200) {
                    DeleteAllScheduleEvent deleteAllScheduleEvent = new DeleteAllScheduleEvent();
                    deleteAllScheduleEvent.scheduleResponseModel = scheduleResponseModel;
                    EventBus.getDefault().post(deleteAllScheduleEvent);
                }
            } else {
                pDialog.dismiss();
                Toast.makeText(activity, "Can't get schedule from server\nPlease get an fast internet connection", Toast.LENGTH_LONG).show();
            }
        }

        ;
    };

    public void onEvent(ScheduleProgressEvent event) {
        if (event.done) {
            if (DbRepository.getInstance().getDB() != null && DbRepository.getInstance().getDB().isOpen())
                DbRepository.getInstance().close();
        } else
            showDialog("saving schedule " + event.progress + " %...", true);
    }

    public void onEvent(ScheduleTempProgressEvent event) {
        if (event.done) {
            if (DbRepository.getInstance().getDB() != null && DbRepository.getInstance().getDB().isOpen())
                DbRepository.getInstance().close();
        } else
            showDialog("saving schedule " + event.progress + " %...", true);
    }

    private void showDialog(String msg, boolean blockable) {
        if (pDialog == null)
            pDialog = new ProgressDialog(this);
        pDialog.setCancelable(!blockable);
        pDialog.show();
        pDialog.setMessage(msg);
    }

    public void hideDialog() {
        if (pDialog == null || !pDialog.isShowing())
            return;
        pDialog.dismiss();
    }

    public void onEvent(UploadProgressEvent event) {
        DebugLog.d("====================================================");
        DebugLog.d("====================================================");
        DebugLog.d(event.progressString);
        DebugLog.d("====================================================");
        DebugLog.d("====================================================");
        uploadInfo.setText(event.progressString);
    }

    public void onEvent(DeleteAllProgressEvent event) {
        if (event.done) {
            hideDialog();
            Toast.makeText(activity, event.progressString, Toast.LENGTH_SHORT).show();
            gotoLogin();
        } else {
            showDialog(event.progressString, true);
        }
    }

    public void onEvent(DeleteAllScheduleEvent event) {
        Toast.makeText(activity, "Schedule Updated", Toast.LENGTH_SHORT).show();
        DbRepository.getInstance().clearData(DbManager.mSchedule);

        ScheduleTempSaver scheduleSaver = new ScheduleTempSaver();
        scheduleSaver.setActivity(activity);
        scheduleSaver.execute(event.scheduleResponseModel.data.toArray());
    }

    OnClickListener logoutClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            new LovelyStandardDialog(SettingActivity.this,R.style.CheckBoxTintTheme)
                    .setTopColor(color(R.color.theme_color))
                    .setButtonsColor(color(R.color.theme_color))
                    .setIcon(R.drawable.logo_app)
                    .setTitle("Confirmation")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton(android.R.string.yes, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            writePreference(R.string.keep_login,false);
                            gotoLogin();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }
    };

    private void gotoLogin() {
        Intent i = new Intent(SettingActivity.this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
}