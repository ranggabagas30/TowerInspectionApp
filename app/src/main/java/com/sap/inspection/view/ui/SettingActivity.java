package com.sap.inspection.view.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rindang.pushnotification.NotificationProcessor;
import com.sap.inspection.BuildConfig;
import com.sap.inspection.R;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.listener.UploadListener;
import com.sap.inspection.manager.AsyncDeleteAllFiles;
import com.sap.inspection.manager.ItemUploadManager;
import com.sap.inspection.model.value.CorrectiveValueModel;
import com.sap.inspection.model.value.FormValueModel;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.CommonUtil;
import com.sap.inspection.util.PrefUtil;
import com.sap.inspection.view.customview.FormInputText;
import com.sap.inspection.util.DialogUtil;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.util.ArrayList;

import pub.devrel.easypermissions.EasyPermissions;

public class SettingActivity extends BaseActivity implements UploadListener, EasyPermissions.PermissionCallbacks {

    Button settextmark;
    Button setlinespace;
    Button setheightwatermark;
    Button update;
    Button updateForm;
    Button updateFormImbasPetir;
    Button upload;
    Button reupload;
    Button delete;
    Button deleteAndUpdateSchedule;
    Button updateSchedule;
    Button updateCorrectiveSchedule;
    Button pushNotificationSchedule;
    Button pushNotificationAPK;
    Button logout;
    TextView updateStatus;
    TextView uploadInfo;
    TextView versionInfo;
    TextView title;
    FormInputText inputtextmarksizepotrait;
    FormInputText inputtextmarksizelandscape;
    FormInputText inputlinespacepotrait;
    FormInputText inputlinespacelandscape;
    FormInputText inputheightwatermarkportrait;
    FormInputText inputheightwatermarklandscape;
    LinearLayout layout_debug;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        title = findViewById(R.id.header_title);
        inputtextmarksizepotrait = findViewById(R.id.textmarksizepotrait);
        inputtextmarksizelandscape = findViewById(R.id.textmarksizelandscape);
        inputlinespacepotrait = findViewById(R.id.linespacepotrait);
        inputlinespacelandscape = findViewById(R.id.linespacelandscape);
        inputheightwatermarkportrait = findViewById(R.id.heightbackgroundwatermarkportrait);
        inputheightwatermarklandscape = findViewById(R.id.heightbackgroundwatermarklandscape);
        settextmark = findViewById(R.id.btntextmarksize);
        setlinespace = findViewById(R.id.btnlinespace);
        setheightwatermark = findViewById(R.id.btnheightbackgroundwatermark);
        update = findViewById(R.id.update);
        updateForm = findViewById(R.id.update_form);
        updateFormImbasPetir = findViewById(R.id.update_form_imbas_petir);
        updateStatus = findViewById(R.id.updateStatus);
        upload = findViewById(R.id.uploadData);
        uploadInfo = findViewById(R.id.uploadInfo);
        versionInfo = findViewById(R.id.versioninfo);
        delete = findViewById(R.id.deleteData);
        deleteAndUpdateSchedule = findViewById(R.id.deleteAndUpdateSchedule);
        reupload = findViewById(R.id.reuploadData);
        updateSchedule = findViewById(R.id.updateSchedule);
        updateCorrectiveSchedule = findViewById(R.id.updateCorrectiveSchedule);
        layout_debug = findViewById(R.id.layout_debug);
        pushNotificationSchedule = findViewById(R.id.pushnot_schedule);
        pushNotificationAPK = findViewById(R.id.pushnot_apk);
        logout = findViewById(R.id.setting_logout);

        int textSizePotrait = PrefUtil.getIntPref(R.string.textmarksizepotrait, Constants.TEXT_SIZE_POTRAIT);
        int textSizeLandscape = PrefUtil.getIntPref(R.string.textmarksizelandscape, Constants.TEXT_SIZE_LANDSCAPE);
        int linespacePotrait = PrefUtil.getIntPref(R.string.linespacepotrait, Constants.TEXT_LINE_SPACE_POTRAIT);
        int linespaceLandscape =  PrefUtil.getIntPref(R.string.linespacelandscape, Constants.TEXT_LINE_SPACE_LANDSCAPE);
        int heightbackgroundwatermarkportrait = PrefUtil.getIntPref(R.string.heightbackgroundwatermarkportrait, Constants.HEIGHT_BACKGROUND_WATERMARK_PORTRAIT);
        int heightbackgroundwatermarklandscape = PrefUtil.getIntPref(R.string.heightbackgroundwatermarklandscape, Constants.HEIGHT_BACKGROUND_WATERMARK_LANDSCAPE);

        inputtextmarksizepotrait.setText(String.valueOf(textSizePotrait));
        inputtextmarksizelandscape.setText(String.valueOf(textSizeLandscape));
        inputlinespacepotrait.setText(String.valueOf(linespacePotrait));
        inputlinespacelandscape.setText(String.valueOf(linespaceLandscape));
        inputheightwatermarkportrait.setText(String.valueOf(heightbackgroundwatermarkportrait));
        inputheightwatermarklandscape.setText(String.valueOf(heightbackgroundwatermarklandscape));

        settextmark.setOnClickListener(setTextMarkClickListener);
        setlinespace.setOnClickListener(setLinespaceClickListener);
        setheightwatermark.setOnClickListener(setHeightBackgroundWatermarkClickListener);

        title.setText(getString(R.string.pengaturan));

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

        versionInfo.setText("Version " + BuildConfig.VERSION_NAME + " Build " + BuildConfig.VERSION_CODE);

        delete.setOnClickListener(deleteClickListener);
        deleteAndUpdateSchedule.setOnClickListener(deleteAndUpdateScheduleClickListener);
        update.setOnClickListener(updateClickListener);
        updateForm.setOnClickListener(updateFormClickListener);
        updateFormImbasPetir.setOnClickListener(updateFormImbasPetirClickListener);
        upload.setOnClickListener(uploadClickListener);
        reupload.setOnClickListener(reuploadClickListener);
        updateSchedule.setOnClickListener(updateScheduleListener);
        updateCorrectiveSchedule.setOnClickListener(updateCorrectiveScheduleListener);
        logout.setOnClickListener(logoutClickListener);

        if (BuildConfig.BUILD_TYPE.equalsIgnoreCase("debug")) {
            layout_debug.setVisibility(View.VISIBLE);
            pushNotificationSchedule.setOnClickListener(pushNotificationClickListener);
            pushNotificationAPK.setOnClickListener(pushNotificationClickListener);
        }

        if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_STP)) {
            updateFormImbasPetir.setVisibility(View.GONE);
            updateCorrectiveSchedule.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        if (!isUpdateAvailable)
            super.onBackPressed();
        else {
            Toast.makeText(this, "Mohon untuk klik tombol \"Update\" untuk menggunakan aplikasi terbaru", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        DebugLog.d(status);
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

    /**
     *
     * EVENT CLICK LISTENERS
     *
     * */
    OnClickListener setTextMarkClickListener = view -> {
        trackEvent("user_set_text_mark_size");
        MyApplication.getInstance().toast("text mark size saved", Toast.LENGTH_SHORT);
        writePreference(R.string.textmarksizepotrait, Integer.parseInt(inputtextmarksizepotrait.getText().toString()));
        writePreference(R.string.textmarksizelandscape, Integer.parseInt(inputtextmarksizelandscape.getText().toString()));
    };

    OnClickListener setLinespaceClickListener = view -> {
        trackEvent("user_set_line_space_size");
        MyApplication.getInstance().toast("line space size saved", Toast.LENGTH_SHORT);
        writePreference(R.string.linespacepotrait, Integer.parseInt(inputlinespacepotrait.getText().toString()));
        writePreference(R.string.linespacelandscape, Integer.parseInt(inputlinespacelandscape.getText().toString()));
    };

    OnClickListener setHeightBackgroundWatermarkClickListener = view -> {
        trackEvent("user_set_height_background_watermark_size");
        MyApplication.getInstance().toast("height background watermark size saved", Toast.LENGTH_SHORT);
        writePreference(R.string.heightbackgroundwatermarkportrait, Integer.parseInt(inputheightwatermarkportrait.getText().toString()));
        writePreference(R.string.heightbackgroundwatermarklandscape, Integer.parseInt(inputheightwatermarklandscape.getText().toString()));
    };

    OnClickListener deleteClickListener = view -> DialogUtil.deleteAllDataDialog(activity, null)
            .setOnPositiveClickListener(scheduleId -> {
                if (TextUtils.isEmpty(scheduleId)) {
                    trackEvent("user_delete_all_data");
                    AsyncDeleteAllFiles task = new AsyncDeleteAllFiles();
                    task.execute();
                }
            }).show();

    OnClickListener deleteAndUpdateScheduleClickListener = view -> DialogUtil.deleteAllSchedulesDialog(activity)
            .setOnPositiveClickListener(view1 -> {
                trackEvent("user_delete_schedule");
                downloadAndDeleteSchedules();
            })
            .show();

    OnClickListener updateClickListener = v -> {
        trackEvent("user_update_apk");
        updateAPKwithStoragePermission();
    };

    OnClickListener updateFormClickListener = v -> {
        trackEvent("user_update_form");
        DialogUtil.showWarningUpdateFormDialog(this, (dialogInterface, i) -> {dialogInterface.dismiss(); downloadNewForm();}, (dialogInterface, i) -> dialogInterface.dismiss());
    };

    OnClickListener updateFormImbasPetirClickListener = v -> {
        trackEvent("user_update_form_imbas_petir");
        downloadNewFormImbasPetir();
    };

    OnClickListener uploadClickListener = v -> {
        trackEvent("user_upload");
        showMessageDialog(getString(R.string.preparingItemForUpload));

        ArrayList<FormValueModel> formValueModels = FormValueModel.getItemValuesForUpload();
        formValueModels.addAll(CorrectiveValueModel.getItemValuesForUpload());
        if (formValueModels.size() == 0) {

            hideDialog();

            // Item is empty
            Toast.makeText(activity, getString(R.string.error_no_upload_item), Toast.LENGTH_LONG).show();
            uploadInfo.setText(getString(R.string.error_no_upload_item));
            return;
        }

        int i = 0;
        for (FormValueModel model : formValueModels) {
            i++;
            //preparing
            showMessageDialog("Persiapan " + (100 * i / formValueModels.size()) + "%");
            model.uploadStatus = FormValueModel.UPLOAD_ONGOING;
            model.save();
        }

        //String progress upload
        Toast.makeText(activity, getString(R.string.progressUpload), Toast.LENGTH_SHORT).show();
        ItemUploadManager.getInstance().addItemValues(formValueModels);
        hideDialog();
    };

    OnClickListener reuploadClickListener = view -> {
        trackEvent("user_reupload");
        DialogUtil.showUploadAllDataDialog(activity, (dialog, id) -> {
            dialog.dismiss();
            uploadInfo.setText(getString(R.string.reSettingUpload));
            FormValueModel.resetAllUploadStatus();
            CorrectiveValueModel.resetAllUploadStatus();
            upload.performClick();
        }, (dialog, id) -> dialog.dismiss());
    };

    OnClickListener updateScheduleListener = view -> {
        trackEvent("user_refresh_schedule");
        downloadSchedules();
    };

    OnClickListener updateCorrectiveScheduleListener = view -> {
        trackEvent("user_update_corrective_schedule");
        downloadCorrectiveSchedules();
    };

    OnClickListener pushNotificationClickListener = view -> {

        int id = view.getId();
        Bundle extras = new Bundle();
        switch (id) {
            case R.id.pushnot_schedule :
                extras.putString("type", "schedule");
                extras.putString("message", "Testing schedule notification");
                break;
            case R.id.pushnot_apk :
                extras.putString("type", "apk");
                extras.putString("message", "Testing apk notification");
                break;
        }
        NotificationProcessor.getNotification(extras, SettingActivity.this).sendNotification();

    };

    OnClickListener logoutClickListener = view -> new LovelyStandardDialog(SettingActivity.this,R.style.CheckBoxTintTheme)
            .setTopColor(color(R.color.theme_color))
            .setButtonsColor(color(R.color.theme_color))
            .setIcon(R.drawable.logo_app)
            .setTitle("Konfirmasi")
            .setMessage("Apa anda yakin ingin keluar?")
            .setPositiveButton(android.R.string.yes, v -> {
                trackEvent("user_logout");
                writePreference(R.string.keep_login,false);
                navigateToLoginActivity();
            })
            .setNegativeButton(android.R.string.no, null)
            .show();
}