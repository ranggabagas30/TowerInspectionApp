package com.sap.inspection;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sap.inspection.connection.APIHelper;
import com.sap.inspection.constant.GlobalVar;
import com.sap.inspection.constant.Constants;
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
import com.sap.inspection.model.value.ItemValueModel;
import com.sap.inspection.task.ScheduleSaver;
import com.sap.inspection.task.ScheduleTempSaver;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.tools.DeleteAllDataDialog;
import com.sap.inspection.tools.DeleteAllSchedulesDialog;
import com.sap.inspection.util.CommonUtil;
import com.sap.inspection.util.PrefUtil;
import com.sap.inspection.util.PermissionUtil;
import com.sap.inspection.view.FormInputText;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class SettingActivity extends BaseActivity implements UploadListener, EasyPermissions.PermissionCallbacks {

    Button settextmark;
    Button setlinespace;
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
    FormInputText inputtextmarksizepotrait;
    FormInputText inputtextmarksizelandscape;
    FormInputText inputlinespacepotrait;
    FormInputText inputlinespacelandscape;

    private ProgressDialog pDialog;

    private boolean flagScheduleSaved = false;

    // Progress dialog type (0 - for Horizontal progress bar)
    public static final int progress_bar_type = 0;

    // File url to download

    private static String file_url;

    private boolean isAccessStorageAllowed = false;
    private boolean isReadStorageAllowed = false;
    private boolean isWriteStorageAllowed = false;
    private boolean isUpdateAvailable = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DebugLog.d("");
        setContentView(R.layout.activity_setting);
        String version = null;
        int versionCode = 0;
        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            TextView title = (TextView) findViewById(R.id.versioninfo);
            title.setVisibility(View.VISIBLE);
            title.setText("Version "+version+" Build "+versionCode);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        DebugLog.d("version Name = " + version + " versionCode = " + versionCode);


        TextView title = (TextView) findViewById(R.id.header_title);
        //pengaturan
        title.setText(getString(R.string.pengaturan));
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        file_url = prefs.getString(this.getString(R.string.url_update), "");

        // watermark configuration
        inputtextmarksizepotrait = (FormInputText) findViewById(R.id.textmarksizepotrait);
        inputtextmarksizelandscape = (FormInputText) findViewById(R.id.textmarksizelandscape);
        inputlinespacepotrait = (FormInputText) findViewById(R.id.linespacepotrait);
        inputlinespacelandscape = (FormInputText) findViewById(R.id.linespacelandscape);

        int textSizePotrait = PrefUtil.getIntPref(R.string.textmarksizepotrait, Constants.TEXT_SIZE_POTRAIT);
        int textSizeLandscape = PrefUtil.getIntPref(R.string.textmarksizelandscape, Constants.TEXT_SIZE_LANDSCAPE);
        int linespacePotrait = PrefUtil.getIntPref(R.string.linespacepotrait, Constants.TEXT_LINE_SPACE_POTRAIT);
        int linespaceLandscape =  PrefUtil.getIntPref(R.string.linespacelandscape, Constants.TEXT_LINE_SPACE_LANDSCAPE);

        inputtextmarksizepotrait.setText(String.valueOf(textSizePotrait));
        inputtextmarksizelandscape.setText(String.valueOf(textSizeLandscape));
        inputlinespacepotrait.setText(String.valueOf(linespacePotrait));
        inputlinespacelandscape.setText(String.valueOf(linespaceLandscape));

        settextmark = (Button) findViewById(R.id.btntextmarksize);
        settextmark.setOnClickListener(setTextMarkClickListener);

        setlinespace = (Button) findViewById(R.id.btnlinespace);
        setlinespace.setOnClickListener(setLinespaceClickListener);

        // show progress bar button
        update = (Button) findViewById(R.id.update);
        updateForm = (Button) findViewById(R.id.update_form);
        updateStatus = (TextView) findViewById(R.id.updateStatus);
        CommonUtil.fixVersion(getApplicationContext());
        DebugLog.d("latest_version" + prefs.getString(this.getString(R.string.latest_version), ""));
        DebugLog.d("url_update" + prefs.getString(this.getString(R.string.url_update), ""));
//        if (version != null && (version.equalsIgnoreCase(prefs.getString(this.getString(R.string.latest_version), "")) /*|| prefs.getString(this.getString(R.string.url_update), "").equalsIgnoreCase("")*/)) {
        if (!CommonUtil.isUpdateAvailable(getApplicationContext())) {
            update.setVisibility(View.VISIBLE);
            update.setEnabled(false);
            //tidak ada new Update
            update.setText(getString(R.string.noNewUpdateAvail));
            update.setBackgroundResource(R.drawable.selector_button_gray_small_padding);

            //updateStatus.setText("No New Update");
        } else {
            //new update available
            update.setVisibility(View.VISIBLE);
            updateStatus.setText(getString(R.string.newUpdateAvail));

            isUpdateAvailable = true;
        }

        upload = (Button) findViewById(R.id.uploadData);
        uploadInfo = (TextView) findViewById(R.id.uploadInfo);
        if (ItemUploadManager.getInstance().getLatestStatus() != null) {
            if (!ItemUploadManager.getInstance().getLatestStatus().equals(ItemUploadManager.getInstance().syncDone) && !ItemUploadManager.getInstance().getLatestStatus().equals(ItemUploadManager.getInstance().syncFail))
                uploadInfo.setText(ItemUploadManager.getInstance().getLatestStatus());
            else
            //lastest status
                uploadInfo.setText("Status Terakhir " + ItemUploadManager.getInstance().getLatestStatus().toLowerCase());
        } else
        //waiting to uplaod
            uploadInfo.setText(getString(R.string.waitingUpload));

        delete = (Button) findViewById(R.id.deleteData);
        delete.setOnClickListener(deleteClickListener);
        deleteSchedule = (Button) findViewById(R.id.deleteSchedule);
        deleteSchedule.setOnClickListener(deleteScheduleClickListener);

        if (CommonUtil.isExternalStorageAvailable()) {
            DebugLog.d("external storage available");
            tempFile = Environment.getExternalStorageDirectory();
        } else {
            DebugLog.d("external storage not available");
            tempFile = getFilesDir();
        }
        tempFile = new File(tempFile.getAbsolutePath() + "/Download/sapInspection" + prefs.getString(SettingActivity.this.getString(R.string.latest_version), "") + ".apk");

        if (tempFile.exists())
            //install
            update.setText(getString(R.string.install));

        update.setOnClickListener(updateClickListener);
        updateForm.setOnClickListener(updateFormClickListener);

        upload.setOnClickListener(uploadClickListener);

        reupload = (Button) findViewById(R.id.reuploadData);
        reupload.setOnClickListener(reuploadClickListener);

        refreshSchedule = (Button) findViewById(R.id.updateSchedule);
        refreshSchedule.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                trackEvent("user_refresh_schedule");
                getSchedule();
            }
        });

        findViewById(R.id.setting_logout).setOnClickListener(logoutClickListener);
        //setting (check ulang)
        trackThisPage("Setting");
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Constants.RC_INSTALL_APK) {

            if (resultCode == RESULT_OK) {

                DebugLog.d("installation is successful");
            } else {
                DebugLog.d("installation is failed");
                new DownloadFileFromURL().execute(file_url);
            }
        }
    }*/

    @Override
    public void onBackPressed() {

        if (!isUpdateAvailable)
            super.onBackPressed();
        else {

            if (CommonUtil.getNewAPKpath(this) == null)
                Toast.makeText(this, "Mohon untuk klik \"Update\" untuk menggunakan aplikasi terbaru", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, "Silahkan klik tombol \"Memasang\" untuk menginstall aplikasi terbaru", Toast.LENGTH_LONG).show();
        }
    }

    OnClickListener setTextMarkClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            MyApplication.getInstance().toast("text mark size saved", Toast.LENGTH_SHORT);
            writePreference(R.string.textmarksizepotrait, Integer.parseInt(inputtextmarksizepotrait.getText().toString()));
            writePreference(R.string.textmarksizelandscape, Integer.parseInt(inputtextmarksizelandscape.getText().toString()));
        }
    };

    OnClickListener setLinespaceClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            MyApplication.getInstance().toast("line space size saved", Toast.LENGTH_SHORT);
            writePreference(R.string.linespacepotrait, Integer.parseInt(inputlinespacepotrait.getText().toString()));
            writePreference(R.string.linespacelandscape, Integer.parseInt(inputlinespacelandscape.getText().toString()));
        }
    };

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
            trackEvent("user_delete_all_data");
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
            trackEvent("user_delete_schedule");
            getScheduleTemp();
        }
    };

    OnClickListener updateClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            trackEvent("user_update_apk");
            //Chek if the file already downloaded before
            //			if(!tempFile.exists()){

            //new DownloadFileFromURL().execute(file_url);

            requestStoragePermission(); // check storage permission first
            /*
            if (CommonUtil.getNewAPKpath(SettingActivity.this) == null)
                requestStoragePermission(); // check storage permission first
            else {
                CommonUtil.installAPK(activity, SettingActivity.this);
            }*/

            /*DownloadFileFromURL downloadFileFromURL = new DownloadFileFromURL(SettingActivity.this.activity, SettingActivity.this);
            downloadFileFromURL.execute(file_url);*/

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
            trackEvent("user_update_form");
            checkFormVersion();
        }
    };
    //test
    private static String formVersion;

    private void checkFormVersion() {
        pDialog = new ProgressDialog(activity);
        pDialog.setMessage(getString(R.string.checkfromversion));
        pDialog.setCancelable(false);
        pDialog.show();
        APIHelper.getFormVersion(activity, formVersionHandler, getPreference(R.string.user_id, ""));
    }

    private Handler formVersionHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.getData() != null && msg.getData().getString("json") != null) {
                VersionModel model = new Gson().fromJson(msg.getData().getString("json"), VersionModel.class);
                formVersion = model.version;
                DebugLog.d("check version : " + PrefUtil.getStringPref(R.string.user_id, "") + getString(R.string.latest_version_form));
                DebugLog.d("check version value : " + getPreference(PrefUtil.getStringPref(R.string.user_id, "") + getString(R.string.latest_version_form), "no value"));
                DebugLog.d("check version value from web: " + formVersion);
                if (!formVersion.equals(getPreference(PrefUtil.getStringPref(R.string.user_id, "") + getString(R.string.latest_version_form), "no value"))) {
                    //string get new from server
                    pDialog.setMessage(getString(R.string.getNewfromServer));
                    APIHelper.getForms(activity, formSaverHandler, getPreference(R.string.user_id, ""));
                } else {
                    //string get schedule from server
                    pDialog.setMessage(getString(R.string.getScheduleFromServer));
                    APIHelper.getSchedules(activity, scheduleHandler, getPreference(R.string.user_id, ""));
                }
            } else {
                pDialog.dismiss();
                //string form update failed
                Toast.makeText(activity, getString(R.string.formUpdateFailedFastInternet), Toast.LENGTH_LONG).show();
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
                //String form update failed
                Toast.makeText(activity, getString(R.string.formUpdateFailedFastInternet), Toast.LENGTH_LONG).show();
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
            //prepare for saving
            pDialog.setMessage("Persiapan Menyimpan");
            DbRepository.getInstance().open(MyApplication.getInstance());
            DbRepository.getInstance().clearData(DbManager.mWorkFormItem);
            DbRepository.getInstance().clearData(DbManager.mWorkFormOption);
            DbRepository.getInstance().clearData(DbManager.mWorkFormColumn);
            DbRepository.getInstance().clearData(DbManager.mWorkFormRow);
            DbRepository.getInstance().clearData(DbManager.mWorkFormRowCol);
            DbRepository.getInstance().close();
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
            DebugLog.d("version saved : " + PrefUtil.getStringPref(R.string.user_id, "") + getString(R.string.latest_version_form));
            DebugLog.d("version saved value : " + getPreference(PrefUtil.getStringPref(R.string.user_id, "") + getString(R.string.latest_version_form), "no value"));
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
            //saving forms
            pDialog.setMessage("menyimpan forms " + values[0] + " %...");
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            //			setFlagFormSaved(true);
            pDialog.setMessage(getString(R.string.getScheduleFromServer));
            APIHelper.getSchedules(activity, scheduleHandler, getPreference(R.string.user_id, ""));
//			DbRepository.getInstance().close();
        }
    }

    OnClickListener uploadClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            trackEvent("user_upload");
            CorrectiveValueModel correctiveValueModel = new CorrectiveValueModel();
//			correctiveValueModel.deleteAll(activity);

            //DbRepositoryValue.getInstance().open(activity);
            ProgressDialog progressDialog = new ProgressDialog(activity);
            //string preparing item for upload
            progressDialog.setMessage(getString(R.string.preparingItemForUpload));
            progressDialog.show();
            ItemValueModel itemValueModel = new ItemValueModel();
            ArrayList<ItemValueModel> itemValueModels = itemValueModel.getItemValuesForUpload();

            itemValueModels.addAll(correctiveValueModel.getItemValuesForUpload());
            if (itemValueModels.size() == 0) {
                progressDialog.dismiss();
                //there is no new to upload
                Toast.makeText(activity, getString(R.string.noItemNewToUpload), Toast.LENGTH_LONG).show();
                uploadInfo.setText(getString(R.string.noItemNewToUpload));
                return;
            }
            int i = 0;
            for (ItemValueModel model : itemValueModels) {
                i++;
                //preparing
                progressDialog.setMessage("persiapan " + (100 * i / itemValueModels.size()) + "%");
                model.uploadStatus = ItemValueModel.UPLOAD_ONGOING;
                model.save();
            }
            ItemUploadManager.getInstance().addItemValues(itemValueModels);
            progressDialog.dismiss();
            //String progress upload
            Toast.makeText(activity, getString(R.string.progressUpload), Toast.LENGTH_LONG).show();
            //DbRepositoryValue.getInstance().close();
        }
    };

    OnClickListener reuploadClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
            // set title
            //String reupload all data
            alertDialogBuilder.setTitle(getString(R.string.reuploadAllData));
            // set dialog message
            alertDialogBuilder
                    //String Are you sure want to re-upload all data
                    .setMessage(getString(R.string.areyousurereuploaddata))
                    .setPositiveButton("Upload", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            //String Resetting upload
                            uploadInfo.setText(getString(R.string.reSettingUpload));
                            ItemValueModel.resetAllUploadStatus();
                            CorrectiveValueModel.resetAllUploadStatus();
                            trackEvent("user_reupload");
                            upload.performClick();
                        }
                    })
                    .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
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
        if (CommonUtil.isExternalStorageAvailable()) {
            DebugLog.d("external storage available");
            tempFile = Environment.getExternalStorageDirectory();
        } else {
            DebugLog.d("external storage not available");
            tempFile = getFilesDir();
        }
        tempFile = new File(tempFile.getAbsolutePath() + "/Download/sapInspection" + prefs.getString(SettingActivity.this.getString(R.string.latest_version), "") + ".apk");

        if (tempFile.exists())
            update.setText(getString(R.string.install));

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
                pDialog.setMessage(getString(R.string.downloadfile));
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
    class DownloadFileFromURL extends AsyncTask<String, String, Boolean> {

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
                    tempDir = getFilesDir();
                }

                DebugLog.d("asign temp dir");
                tempDir = new File(tempDir.getAbsolutePath() + "/Download");
                DebugLog.d("get tempratur dir");
                if (!tempDir.exists()) {
                    tempDir.mkdir();
                }
                DebugLog.d("get exist dir");
                // Output stream
                OutputStream output = new FileOutputStream(tempDir.getAbsolutePath() + "/sapInspection" + prefs.getString(SettingActivity.this.getString(R.string.latest_version), "") + ".apk");
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
            dismissDialog(progress_bar_type);

            if (!isSuccessful) {
                Toast.makeText(SettingActivity.this, "Gagal mengunduh APK terbaru. Periksa jaringan Anda", Toast.LENGTH_LONG).show();
            } else {

                // just install the new APK
                CommonUtil.installAPK(activity, SettingActivity.this);
            }
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
        //DbRepository.getInstance().open(activity);
        pDialog = new ProgressDialog(activity);
        pDialog.setMessage(getString(R.string.getScheduleFromServer));
        pDialog.setCancelable(false);
        pDialog.show();
        APIHelper.getSchedules(activity, scheduleHandlerTemp, getPreference(R.string.user_id, ""));
    }

    private void getSchedule() {
        //DbRepository.getInstance().open(activity);
        pDialog = new ProgressDialog(activity);
        //get schedule from server
        pDialog.setMessage(getString(R.string.getScheduleFromServer));
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
                //String cant get schedule fast internet
                Toast.makeText(activity, getString(R.string.cantgetschedulefastinternet), Toast.LENGTH_LONG).show();
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
                Toast.makeText(activity, getString(R.string.cantgetschedulefastinternet), Toast.LENGTH_LONG).show();
            }
        }

        ;
    };

    public void onEvent(ScheduleProgressEvent event) {
        if (event.done) {
            /*if (DbRepository.getInstance().getDB() != null && DbRepository.getInstance().getDB().isOpen())
                DbRepository.getInstance().close();*/
        } else
            showDialog("Menyimpan jadwal " + event.progress + " %...", true);
    }

    public void onEvent(ScheduleTempProgressEvent event) {
        if (event.done) {
            /*if (DbRepository.getInstance().getDB() != null && DbRepository.getInstance().getDB().isOpen())
                DbRepository.getInstance().close();*/
        } else
            showDialog("Menyimpan jadwal " + event.progress + " %...", true);
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
        Toast.makeText(activity, "Jadwal diperbaharui", Toast.LENGTH_SHORT).show();
        DbRepository.getInstance().open(MyApplication.getInstance());
        DbRepository.getInstance().clearData(DbManager.mSchedule);
        DbRepository.getInstance().close();
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
                    .setTitle("Konfirmasi")
                    .setMessage("Apa anda yakin ingin keluar?")
                    .setPositiveButton(android.R.string.yes, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            trackEvent("user_logout");
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

    /**
     * Permission
     *
     **/
    @AfterPermissionGranted(Constants.RC_STORAGE_PERMISSION)
    private void requestStoragePermission() {

        String[] perms = new String[]{PermissionUtil.READ_EXTERNAL_STORAGE, PermissionUtil.WRITE_EXTERNAL_STORAGE};
        if (PermissionUtil.hasPermission(this, perms)) {

            // Already has permission
            DebugLog.d("Already have permission, do the thing");
            updateAPK();
            /*// just install the new APK
            CommonUtil.installAPK(activity, this);*/

        } else {

            // Do not have permissions, request them now
            DebugLog.d("Do not have permissions, request them now");
            PermissionUtil.requestPermission(this, getString(R.string.rationale_externalstorage), Constants.RC_STORAGE_PERMISSION, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        DebugLog.d("request permission result");

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (String permission : permissions) {

            if (permission.equalsIgnoreCase(PermissionUtil.READ_EXTERNAL_STORAGE)) {

                // read external storage allowed
                if (PermissionUtil.hasPermission(this, PermissionUtil.READ_EXTERNAL_STORAGE)) {

                    DebugLog.d("read external storage allowed");
                    isReadStorageAllowed = true;
                }

            }

            if (permission.equalsIgnoreCase(PermissionUtil.WRITE_EXTERNAL_STORAGE)) {

                // write external storage allowed
                if (PermissionUtil.hasPermission(this, PermissionUtil.WRITE_EXTERNAL_STORAGE)) {

                    DebugLog.d("write external storage allowed");
                    isWriteStorageAllowed = true;
                }
            }
        }

        isAccessStorageAllowed = isReadStorageAllowed & isWriteStorageAllowed;

        if (isAccessStorageAllowed) {

            updateAPK();

            /*// just install the new APK
            CommonUtil.installAPK(activity, this);*/

        } else {

            Toast.makeText(this, "Gagal mengunduh APK karena tidak ada izin akses storage", Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

        DebugLog.d("permission granted");
        DebugLog.d("request code : " + requestCode);

        for (String permission : perms) {

            DebugLog.d(permission);
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

        DebugLog.d("permission denied");
        DebugLog.d("request code : " + requestCode);

        if (requestCode == Constants.RC_STORAGE_PERMISSION) {

            Toast.makeText(this, "Gagal mengunduh SAP Mobile App versi terbaru", Toast.LENGTH_LONG).show();
        }

    }

    private void updateAPK() {

        if (GlobalVar.getInstance().isNetworkOnline(this)) {
            new DownloadFileFromURL().execute(file_url);
        } else {
            Toast.makeText(this, getString(R.string.disconnected), Toast.LENGTH_LONG).show();
        }
    }
}