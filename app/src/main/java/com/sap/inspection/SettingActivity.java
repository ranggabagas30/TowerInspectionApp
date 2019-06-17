package com.sap.inspection;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sap.inspection.constant.Constants;
import com.sap.inspection.listener.UploadListener;
import com.sap.inspection.manager.AsyncDeleteAllFiles;
import com.sap.inspection.manager.ItemUploadManager;
import com.sap.inspection.model.value.CorrectiveValueModel;
import com.sap.inspection.model.value.ItemValueModel;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.tools.DeleteAllDataDialog;
import com.sap.inspection.tools.DeleteAllSchedulesDialog;
import com.sap.inspection.util.CommonUtil;
import com.sap.inspection.util.PrefUtil;
import com.sap.inspection.view.FormInputText;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.io.File;
import java.util.ArrayList;

import pub.devrel.easypermissions.EasyPermissions;

public class SettingActivity extends BaseActivity implements UploadListener, EasyPermissions.PermissionCallbacks {

    Button settextmark;
    Button setlinespace;
    Button update;
    Button updateForm;
    Button updateFormImbasPetir;
    Button upload;
    Button reupload;
    Button delete;
    Button deleteSchedule;
    Button refreshSchedule;
    Button logout;
    TextView updateStatus;
    TextView uploadInfo;
    TextView title;
    File tempFile;
    FormInputText inputtextmarksizepotrait;
    FormInputText inputtextmarksizelandscape;
    FormInputText inputlinespacepotrait;
    FormInputText inputlinespacelandscape;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DebugLog.d("");
        setContentView(R.layout.activity_setting);

        // watermark configuration
        title = findViewById(R.id.header_title);
        inputtextmarksizepotrait = findViewById(R.id.textmarksizepotrait);
        inputtextmarksizelandscape = findViewById(R.id.textmarksizelandscape);
        inputlinespacepotrait = findViewById(R.id.linespacepotrait);
        inputlinespacelandscape = findViewById(R.id.linespacelandscape);
        settextmark = findViewById(R.id.btntextmarksize);
        setlinespace = findViewById(R.id.btnlinespace);

        update = findViewById(R.id.update);
        updateForm = findViewById(R.id.update_form);
        updateFormImbasPetir = findViewById(R.id.update_form_imbas_petir);
        updateStatus = findViewById(R.id.updateStatus);
        upload = findViewById(R.id.uploadData);
        uploadInfo = findViewById(R.id.uploadInfo);
        delete = findViewById(R.id.deleteData);
        deleteSchedule = findViewById(R.id.deleteSchedule);
        reupload = findViewById(R.id.reuploadData);
        refreshSchedule = findViewById(R.id.updateSchedule);
        logout = findViewById(R.id.setting_logout);

        String version = null;
        int versionCode = 0;
        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            TextView title = findViewById(R.id.versioninfo);
            title.setVisibility(View.VISIBLE);
            title.setText("Version "+version+" Build "+versionCode);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        DebugLog.d("version Name = " + version + " versionCode = " + versionCode);

        title.setText(getString(R.string.pengaturan));

        int textSizePotrait = PrefUtil.getIntPref(R.string.textmarksizepotrait, Constants.TEXT_SIZE_POTRAIT);
        int textSizeLandscape = PrefUtil.getIntPref(R.string.textmarksizelandscape, Constants.TEXT_SIZE_LANDSCAPE);
        int linespacePotrait = PrefUtil.getIntPref(R.string.linespacepotrait, Constants.TEXT_LINE_SPACE_POTRAIT);
        int linespaceLandscape =  PrefUtil.getIntPref(R.string.linespacelandscape, Constants.TEXT_LINE_SPACE_LANDSCAPE);

        inputtextmarksizepotrait.setText(String.valueOf(textSizePotrait));
        inputtextmarksizelandscape.setText(String.valueOf(textSizeLandscape));
        inputlinespacepotrait.setText(String.valueOf(linespacePotrait));
        inputlinespacelandscape.setText(String.valueOf(linespaceLandscape));

        settextmark.setOnClickListener(setTextMarkClickListener);
        setlinespace.setOnClickListener(setLinespaceClickListener);

        CommonUtil.fixVersion(getApplicationContext());
        DebugLog.d("latest_version" + mPref.getString(this.getString(R.string.latest_version), ""));
        DebugLog.d("url_update" + mPref.getString(this.getString(R.string.url_update), ""));

        if (!CommonUtil.isUpdateAvailable(getApplicationContext())) {
            update.setVisibility(View.VISIBLE);
            update.setEnabled(false);
            update.setText(getString(R.string.noNewUpdateAvail));
            update.setBackgroundResource(R.drawable.selector_button_gray_small_padding);
        } else {
            //new update available
            update.setVisibility(View.VISIBLE);
            updateStatus.setText(getString(R.string.newUpdateAvail));

            isUpdateAvailable = true;
        }

        if (ItemUploadManager.getInstance().getLatestStatus() != null) {
            if (!ItemUploadManager.getInstance().getLatestStatus().equals(ItemUploadManager.getInstance().syncDone) && !ItemUploadManager.getInstance().getLatestStatus().equals(ItemUploadManager.getInstance().syncFail))
                uploadInfo.setText(ItemUploadManager.getInstance().getLatestStatus());
            else
                uploadInfo.setText("Status Terakhir " + ItemUploadManager.getInstance().getLatestStatus().toLowerCase());
        } else
            uploadInfo.setText(getString(R.string.waitingUpload));


        delete.setOnClickListener(deleteClickListener);
        deleteSchedule.setOnClickListener(deleteScheduleClickListener);

        if (CommonUtil.isExternalStorageAvailable()) {
            DebugLog.d("external storage available");
            tempFile = Environment.getExternalStorageDirectory();
        } else {
            DebugLog.d("external storage not available");
            tempFile = getFilesDir();
        }
        tempFile = new File(tempFile.getAbsolutePath() + "/Download/sapInspection" + mPref.getString(SettingActivity.this.getString(R.string.latest_version), "") + ".apk");

        if (tempFile.exists())
            update.setText(getString(R.string.install));

        update.setOnClickListener(updateClickListener);
        updateForm.setOnClickListener(updateFormClickListener);
        updateFormImbasPetir.setOnClickListener(updateFormImbasPetirClickListener);
        upload.setOnClickListener(uploadClickListener);
        reupload.setOnClickListener(reuploadClickListener);
        logout.setOnClickListener(logoutClickListener);
        refreshSchedule.setOnClickListener(v -> {
            trackEvent("user_refresh_schedule");
            downloadSchedules();
        });

        trackThisPage("Setting");
    }


    @Override
    public void onBackPressed() {
        if (!isUpdateAvailable)
            super.onBackPressed();
        else {
            if (CommonUtil.getNewAPKpath(this) == null)
                Toast.makeText(this, "Mohon untuk klik tombol \"Update\" untuk menggunakan aplikasi terbaru", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, "Silahkan klik tombol \"Memasang\" untuk menginstall aplikasi terbaru", Toast.LENGTH_LONG).show();
        }
    }


    /**
     *
     * EVENT CLICK LISTENERS
     *
     * */

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
            DeleteAllDataDialog dialog = new DeleteAllDataDialog(activity, null);
            dialog.setOnPositiveClickListener(new DeleteAllDataDialog.OnPositiveClickListener() {
                @Override
                public void onPositiveClick(String scheduleId) {

                    if (TextUtils.isEmpty(scheduleId)) {
                        trackEvent("user_delete_all_data");
                        AsyncDeleteAllFiles task = new AsyncDeleteAllFiles();
                        task.execute();
                    }
                }
            });
            dialog.show();
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

    OnClickListener positiveDeleteScheduleClickListener = v -> {
        trackEvent("user_delete_schedule");
        downloadAndDeleteSchedules();
    };

    OnClickListener updateClickListener = v -> {
        trackEvent("user_update_apk");
        requestStoragePermission(); // check storage permission first
    };

    OnClickListener updateFormClickListener = v -> {
        trackEvent("user_update_form");
        checkFormVersion();
    };

    OnClickListener updateFormImbasPetirClickListener = v -> {
        trackEvent("user_update_form_imbas_petir");
        downloadNewFormImbasPetir();
    };

    OnClickListener uploadClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            trackEvent("user_upload");
            showMessageDialog(getString(R.string.preparingItemForUpload));

            ArrayList<ItemValueModel> itemValueModels = ItemValueModel.getItemValuesForUpload();

            itemValueModels.addAll(CorrectiveValueModel.getItemValuesForUpload());
            if (itemValueModels.size() == 0) {

                hideDialog();

                //there is not any items to be uploaded
                Toast.makeText(activity, getString(R.string.noItemNewToUpload), Toast.LENGTH_LONG).show();
                uploadInfo.setText(getString(R.string.noItemNewToUpload));
                return;
            }
            int i = 0;
            for (ItemValueModel model : itemValueModels) {
                i++;
                //preparing
                showMessageDialog("persiapan " + (100 * i / itemValueModels.size()) + "%");
                model.uploadStatus = ItemValueModel.UPLOAD_ONGOING;
                model.save();
            }

            ItemUploadManager.getInstance().addItemValues(itemValueModels);
            hideDialog();

            //String progress upload
            Toast.makeText(activity, getString(R.string.progressUpload), Toast.LENGTH_SHORT).show();
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

    OnClickListener logoutClickListener = view -> new LovelyStandardDialog(SettingActivity.this,R.style.CheckBoxTintTheme)
            .setTopColor(color(R.color.theme_color))
            .setButtonsColor(color(R.color.theme_color))
            .setIcon(R.drawable.logo_app)
            .setTitle("Konfirmasi")
            .setMessage("Apa anda yakin ingin keluar?")
            .setPositiveButton(android.R.string.yes, new OnClickListener() {
                @Override
                public void onClick(View v) {
                    trackEvent("user_logout");
                    writePreference(R.string.keep_login,false);
                    navigateToLoginActivity();
                }
            })
            .setNegativeButton(android.R.string.no, null)
            .show();

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
        tempFile = new File(tempFile.getAbsolutePath() + "/Download/sapInspection" + mPref.getString(SettingActivity.this.getString(R.string.latest_version), "") + ".apk");

        if (tempFile.exists())
            update.setText(getString(R.string.install));
    }

    @Override
    protected void onPause() {
        super.onPause();
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
}