package com.sap.inspection.view.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
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
import com.sap.inspection.TowerApplication;
import com.sap.inspection.connection.rest.TowerAPIHelper;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.event.DeleteAllProgressEvent;
import com.sap.inspection.listener.UploadListener;
import com.sap.inspection.manager.ItemUploadManager;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.value.CorrectiveValueModel;
import com.sap.inspection.model.value.FormValueModel;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.CommonUtil;
import com.sap.inspection.util.DialogUtil;
import com.sap.inspection.util.NetworkUtil;
import com.sap.inspection.util.PermissionUtil;
import com.sap.inspection.util.PrefUtil;
import com.sap.inspection.view.customview.FormInputText;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
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
    String[] perms = new String[]{PermissionUtil.READ_EXTERNAL_STORAGE, PermissionUtil.WRITE_EXTERNAL_STORAGE};

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

        delete.setOnClickListener(deleteAllData);
        deleteAndUpdateSchedule.setOnClickListener(deleteAndUpdateScheduleClickListener);
        update.setOnClickListener(updateClickListener);
        updateForm.setOnClickListener(updateFormClickListener);
        updateFormImbasPetir.setOnClickListener(updateFormImbasPetirClickListener);
        upload.setOnClickListener(uploadClickListener);
        reupload.setOnClickListener(reUploadClickListener);
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
            Toast.makeText(this, getString(R.string.warning_mohon_klik_tombol_update), Toast.LENGTH_LONG).show();
        }
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

    @AfterPermissionGranted(Constants.RC_STORAGE_PERMISSION)
    private void requestStoragePermission() {
        PermissionUtil.requestPermission(this, getString(R.string.rationale_externalstorage), Constants.RC_STORAGE_PERMISSION, perms);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Constants.RC_STORAGE_PERMISSION) {
            if (PermissionUtil.hasPermission(this, perms)) {
                updateAPK();
            } else requestStoragePermission();
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == Constants.RC_STORAGE_PERMISSION) {
            updateAPK();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        new AppSettingsDialog.Builder(this).build().show();
    }

    /**
     *
     * EVENT CLICK LISTENERS
     *
     * */
    OnClickListener setTextMarkClickListener = view -> {
        trackEvent("user_set_text_mark_size");
        TowerApplication.getInstance().toast("text mark size saved", Toast.LENGTH_SHORT);
        writePreference(R.string.textmarksizepotrait, Integer.parseInt(inputtextmarksizepotrait.getText().toString()));
        writePreference(R.string.textmarksizelandscape, Integer.parseInt(inputtextmarksizelandscape.getText().toString()));
    };

    OnClickListener setLinespaceClickListener = view -> {
        trackEvent("user_set_line_space_size");
        TowerApplication.getInstance().toast("line space size saved", Toast.LENGTH_SHORT);
        writePreference(R.string.linespacepotrait, Integer.parseInt(inputlinespacepotrait.getText().toString()));
        writePreference(R.string.linespacelandscape, Integer.parseInt(inputlinespacelandscape.getText().toString()));
    };

    OnClickListener setHeightBackgroundWatermarkClickListener = view -> {
        trackEvent("user_set_height_background_watermark_size");
        TowerApplication.getInstance().toast("height background watermark size saved", Toast.LENGTH_SHORT);
        writePreference(R.string.heightbackgroundwatermarkportrait, Integer.parseInt(inputheightwatermarkportrait.getText().toString()));
        writePreference(R.string.heightbackgroundwatermarklandscape, Integer.parseInt(inputheightwatermarklandscape.getText().toString()));
    };

    OnClickListener deleteAllData = view -> DialogUtil.deleteAllDataDialog(activity)
            .setOnPositiveClickListener(scheduleId -> {
                trackEvent("user_delete_all_data");
                EventBus.getDefault().post(new DeleteAllProgressEvent(activity.getString(R.string.info_deleting_all_data)));
                compositeDisposable.add(
                        CommonUtil.deleteAllData()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        () -> {
                                            String path = Constants.DIR_PHOTOS + File.separator;
                                            if (!CommonUtil.deleteFiles(path)) {
                                                TowerApplication.getInstance().toast(TowerApplication.getContext().getString(R.string.error_delete_files), Toast.LENGTH_SHORT);
                                            }
                                            EventBus.getDefault().post(new DeleteAllProgressEvent(activity.getString(R.string.success_delete_files), true, true));
                                        },
                                        error -> {
                                            EventBus.getDefault().post(new DeleteAllProgressEvent(activity.getString(R.string.error_delete_all_local_data) + "\n" + error.getMessage(), true, false));
                                            DebugLog.e(error.getMessage(), error);
                                        }
                                )
                );
            }).show();

    OnClickListener deleteAndUpdateScheduleClickListener = view -> DialogUtil.deleteAllSchedulesDialog(activity)
            .setOnPositiveClickListener(view1 -> {
                trackEvent("user_delete_schedule");
                compositeDisposable.add(
                        Completable.fromAction(ScheduleBaseModel::delete)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        this::downloadWorkSchedules,
                                        error -> Toast.makeText(this, getString(R.string.error_failed_delete_schedules), Toast.LENGTH_LONG).show()
                        )
                );
            })
            .show();

    OnClickListener updateClickListener = v -> {
        trackEvent("user_update_apk");
        if (PermissionUtil.hasPermission(this, perms)) {
            updateAPK();
        } else requestStoragePermission();
    };

    OnClickListener updateFormClickListener = v -> {
        trackEvent("user_update_form");
        DialogUtil.showWarningUpdateFormDialog(this,
                (dialogInterface, i) -> downloadWorkForms(),
                (dialogInterface, i) -> dialogInterface.dismiss());
    };

    OnClickListener updateFormImbasPetirClickListener = v -> {
        trackEvent("user_update_form_imbas_petir");
        downloadWorkFormsImbasPetir();
    };

    OnClickListener uploadClickListener = v -> {
        trackEvent("user_upload");
        showMessageDialog(getString(R.string.preparingItemForUpload));
        ArrayList<FormValueModel> formValueModels = FormValueModel.getItemValuesForUpload();
        formValueModels.addAll(CorrectiveValueModel.getItemValuesForUpload());
        if (formValueModels.isEmpty()) {
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

        ItemUploadManager.getInstance().addItemValues(formValueModels);
        hideDialog();
    };

    OnClickListener reUploadClickListener = view -> {
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
        downloadWorkSchedules();
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
                navigateToLoginActivity(this);
            })
            .setNegativeButton(android.R.string.no, null)
            .show();

    private void downloadWorkForms() {
        DebugLog.d("--> DOWNLOADING WORK FORMS");
        showMessageDialog(getString(R.string.gettingnewform));
        compositeDisposable.add(
                TowerAPIHelper.downloadWorkForms()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                downloadResponse -> {
                                    if (downloadResponse != null) {
                                        if (downloadResponse.status == HttpURLConnection.HTTP_OK) {
                                            new FormSaver(new Handler(
                                                    message -> {
                                                        hideDialog();
                                                        String result = message.getData().getString("response");
                                                        if (result != null && result.equals("success")) {
                                                            Toast.makeText(this, getString(R.string.success_update_form), Toast.LENGTH_SHORT).show();
                                                            return true;
                                                        }

                                                        Toast.makeText(this, getString(R.string.error_failed_save_forms), Toast.LENGTH_LONG).show();
                                                        return false;
                                                    }
                                            )).execute(downloadResponse.data.toArray());
                                        } else {
                                            hideDialog();
                                            Toast.makeText(this, getString(R.string.error_failed_download_forms), Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        hideDialog();
                                        Toast.makeText(this, getString(R.string.error_response_null), Toast.LENGTH_LONG).show();
                                    }
                                }, error -> {
                                    hideDialog();
                                    String errorMsg = NetworkUtil.handleApiError(error);
                                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                                }
                        )
        );
    }

    private void downloadWorkFormsImbasPetir() {
        DebugLog.d("--> DOWNLOADING WORK FORMS IMBAS PETIR");
        showMessageDialog(getString(R.string.gettingnewformimbaspetir));
        compositeDisposable.add(
                TowerAPIHelper.downloadWorkFormsImbasPetir()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                workFormImbasPetir -> {
                                    if (workFormImbasPetir != null) {
                                        if (workFormImbasPetir.status == HttpURLConnection.HTTP_OK) {
                                            new FormImbasPetirSaver(new Handler(message -> {
                                                String response = message.getData().getString("response");
                                                if (TextUtils.isEmpty(response) || response.equalsIgnoreCase("failed")) {
                                                    Toast.makeText(this, getString(R.string.error_failed_save_forms_imbas_petir), Toast.LENGTH_LONG).show();
                                                    DebugLog.d("-- " + getString(R.string.error_failed_save_forms_imbas_petir) + " --");
                                                    return false;
                                                }

                                                Toast.makeText(this, getString(R.string.success_update_form_imbas_petir), Toast.LENGTH_SHORT).show();
                                                return true;
                                            })).execute(workFormImbasPetir.data.toArray());
                                        } else {
                                            hideDialog();
                                            Toast.makeText(this, getString(R.string.error_failed_download_forms_imbas_petir), Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        hideDialog();
                                        Toast.makeText(this, getString(R.string.error_response_null), Toast.LENGTH_LONG).show();
                                    }
                                }, error -> {
                                    hideDialog();
                                    String errorMsg = NetworkUtil.handleApiError(error);
                                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                                }
                        )
        );
    }

    private void downloadWorkSchedules() {
        DebugLog.d("--> DOWNLOADING SCHEDULES");
        showMessageDialog(getString(R.string.getScheduleFromServer));
        compositeDisposable.add(
                TowerAPIHelper.downloadSchedules()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                scheduleResponse -> {
                                    if (scheduleResponse != null) {
                                        if (scheduleResponse.status == HttpURLConnection.HTTP_OK) {
                                            saveSchedule(scheduleResponse.data.toArray());
                                        } else {
                                            hideDialog();
                                            Toast.makeText(this, getString(R.string.error_failed_download_schedules), Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        hideDialog();
                                        Toast.makeText(this, getString(R.string.error_response_null), Toast.LENGTH_LONG).show();
                                    }
                                }, error -> {
                                    hideDialog();
                                    String errorMsg = NetworkUtil.handleApiError(error);
                                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                                }
                        )
        );
    }
}