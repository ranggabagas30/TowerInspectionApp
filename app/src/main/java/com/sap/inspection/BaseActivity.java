package com.sap.inspection;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Window;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sap.inspection.connection.APIHelper;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.constant.GlobalVar;
import com.sap.inspection.event.DeleteAllProgressEvent;
import com.sap.inspection.event.DeleteAllScheduleEvent;
import com.sap.inspection.event.ScheduleProgressEvent;
import com.sap.inspection.event.ScheduleTempProgressEvent;
import com.sap.inspection.event.UploadProgressEvent;
import com.sap.inspection.fragments.BaseFragment;
import com.sap.inspection.manager.ScreenManager;
import com.sap.inspection.model.DbManager;
import com.sap.inspection.model.DbRepository;
import com.sap.inspection.model.config.formimbaspetir.CorrectiveScheduleConfig;
import com.sap.inspection.model.form.ColumnModel;
import com.sap.inspection.model.form.RowModel;
import com.sap.inspection.model.form.WorkFormGroupModel;
import com.sap.inspection.model.form.WorkFormModel;
import com.sap.inspection.model.responsemodel.CorrectiveScheduleResponseModel;
import com.sap.inspection.model.responsemodel.FormResponseModel;
import com.sap.inspection.model.responsemodel.ScheduleResponseModel;
import com.sap.inspection.model.responsemodel.VersionModel;
import com.sap.inspection.task.ScheduleSaver;
import com.sap.inspection.task.ScheduleTempSaver;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.CommonUtil;
import com.sap.inspection.util.PermissionUtil;
import com.sap.inspection.util.PrefUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import de.greenrobot.event.EventBus;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public abstract class BaseActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

	protected FragmentActivity activity;
	protected SharedPreferences mPref;

	public static ImageLoader imageLoader = ImageLoader.getInstance();

	// Progress dialog type (0 - for Horizontal progress bar)
	public static final int progress_bar_type = 0;

	private ProgressDialog progressDialog, pDialog;
	private String formVersion;
	private boolean instanceStateSaved;
	private boolean flagScheduleSaved = false;
	private boolean flagFormSaved = false;
	private boolean isAccessStorageAllowed = false;
	private boolean isReadStorageAllowed = false;
	private boolean isWriteStorageAllowed = false;
	protected boolean isUpdateAvailable = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		activity = this;
		progressDialog = new ProgressDialog(this);
		progressDialog.setCancelable(false);

		mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		ScreenManager.getInstance().setHeight(metrics.heightPixels);
		ScreenManager.getInstance().setWidth(metrics.widthPixels);
	}

	@Override
	protected void onStart() {
		super.onStart();
		EventBus.getDefault().register(this);
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
	protected void onStop() {
		super.onStop();
		EventBus.getDefault().unregister(this);
	}
	@Override
	protected void onDestroy() {
		if (!instanceStateSaved) {
//			imageLoader.stop();
		}
		super.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		instanceStateSaved = true;
	}

	private void offlineSchedule(){
		byte[] buffer = null;
		InputStream is;
		try {
			is = this.getAssets().open("schedules.txt");
			int size = is.available();
			buffer = new byte[size];
			is.read(buffer);
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String bufferString = new String(buffer);
		Gson gson = new Gson();
		if (bufferString != null){
			ScheduleResponseModel scheduleResponseModel = gson.fromJson(bufferString, ScheduleResponseModel.class);
			if (scheduleResponseModel.status == 200){
				ScheduleSaver scheduleSaver = new ScheduleSaver();
				scheduleSaver.execute(scheduleResponseModel.data.toArray());
			}
		}
	}

	public void onEvent(ScheduleTempProgressEvent event) {
		if (event.done) {
			hideDialog();
			Toast.makeText(activity, "Schedule berhasil diperbaharui", Toast.LENGTH_SHORT).show();
		} else
			showMessageDialog("menyimpan schedule " + event.progress + " %...");
	}

    public void onEvent(ScheduleProgressEvent event) {
        if (event.done) {
            hideDialog();
            Toast.makeText(activity, "Schedule berhasil diperbaharui", Toast.LENGTH_SHORT).show();
        } else
            showMessageDialog("Menyimpan schedule " + event.progress + " %...");
    }

	public void onEvent(DeleteAllScheduleEvent event) {
		DbRepository.getInstance().open(MyApplication.getInstance());
		DbRepository.getInstance().clearData(DbManager.mSchedule);
		DbRepository.getInstance().close();
		ScheduleTempSaver scheduleSaver = new ScheduleTempSaver();
		scheduleSaver.setActivity(activity);
		scheduleSaver.execute(event.scheduleResponseModel.data.toArray());
	}

	public void onEvent(DeleteAllProgressEvent event) {
		if (event.done) {
			hideDialog();
			Toast.makeText(activity, event.progressString, Toast.LENGTH_SHORT).show();
			if (event.shouldRelogin)
				navigateToLoginActivity();
		} else {
			showMessageDialog(event.progressString);
		}
	}

	public void onEvent(UploadProgressEvent event) {
		DebugLog.d("event="+new Gson().toJson(event));
		if (!event.done)
			showMessageDialog(event.progressString);
		else
			hideDialog();
	}

	public void writePreference(int key, String value) {
		mPref.edit().putString(getString(key), value).commit();
	}
	
	public void writePreference(String key, String value) {
		mPref.edit().putString(key, value).commit();
	}
	
	public void writePreference(int key, int value) {
		mPref.edit().putInt(getString(key), value).commit();
	}
	
	public void writePreference(int key, boolean value) {
		mPref.edit().putBoolean(getString(key), value).commit();
	}
	
	public String getPreference(int key, String defaultValue) {
		return mPref.getString(getString(key), defaultValue);
	}
	
	public String getPreference(String key, String defaultValue) {
		return mPref.getString(key, defaultValue);
	}
	
	public boolean getPreference(int key, boolean defaultValue) {
		return mPref.getBoolean(getString(key), defaultValue);
	}

	protected int color(int colorRes) {
		return ContextCompat.getColor(this, colorRes);
	}

	protected void trackEvent(String name) {
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
		MyApplication myApplication = (MyApplication) getApplication();
		FirebaseAnalytics mFirebaseAnalytics = myApplication.getDefaultAnalytics();
		mFirebaseAnalytics.logEvent("track_event", bundle);
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
	public void showMessageDialog(String message) {

		if (progressDialog != null) {
			progressDialog.setMessage(message);

			if (!progressDialog.isShowing())
				progressDialog.show();
		}
	}

	public void hideDialog() {

		if (progressDialog != null && progressDialog.isShowing())
			progressDialog.dismiss();

	}

	private void downloadNewForm() {

		showMessageDialog(getString(R.string.gettingnewform));
		APIHelper.getForms(activity, formSaverHandler, getPreference(R.string.user_id, ""));

	}

	protected void downloadNewFormImbasPetir() {

		showMessageDialog(getString(R.string.gettingnewformimbaspetir));
		APIHelper.getFormImbasPetir(activity, formImbasPetirSaverHandler);
	}

	protected void downloadAndDeleteSchedules() {

		showMessageDialog(getString(R.string.getScheduleFromServer));
		APIHelper.getSchedules(activity, scheduleHandlerTemp, getPreference(R.string.user_id, ""));

	}

	protected void downloadSchedules() {
		showMessageDialog(getString(R.string.getScheduleFromServer));
		APIHelper.getSchedules(activity, scheduleHandler, getPreference(R.string.user_id, ""));
	}

	protected void downloadCorrectiveSchedules() {
		showMessageDialog("Mendapatkan Corrective Schedule dari server");
		APIHelper.getCorrectiveSchedule(activity, correctiveScheduleHandler, getPreference(R.string.user_id, ""));
	}

	private void initFormOffline(){
		WorkFormModel form = new WorkFormModel();
		//DbRepository.getInstance().open(this);
		if (form.countItem() != 0){
			//			DbRepository.getInstance().close();
			//			setFlagFormSaved(true);
			return;
		}
		byte[] buffer = null;
		InputStream is;
		try {
			is = this.getAssets().open("forms_14_10_2014.txt");
			int size = is.available();
			buffer = new byte[size];
			is.read(buffer);
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String bufferString = new String(buffer);
		initForm(bufferString);
	}

	private void initForm(String json){
		Gson gson = new Gson();
		FormResponseModel formResponseModel = gson.fromJson(json,FormResponseModel.class);
		if (formResponseModel.status == 200){
			FormSaver formSaver = new FormSaver();
			formSaver.execute(formResponseModel.data.toArray());
		}
	}

    private void initFormImbasPetir(String json){
        Gson gson = new Gson();
        FormResponseModel formResponseModel = gson.fromJson(json, FormResponseModel.class);
        if (formResponseModel.status == 200) {
            new FormImbasPetirSaver().execute(formResponseModel.data.toArray());
        }
    }

	protected void checkLatestAPKVersion(){
		DebugLog.d("check apk version");
		showMessageDialog(getString(R.string.checkversionapplication));
		APIHelper.getAPKVersion(activity, apkHandler, getPreference(R.string.user_id, ""));
	}

	protected void checkFormVersion(){
		DebugLog.d("check form version");
		showMessageDialog(getString(R.string.checkfromversion));
		APIHelper.getFormVersion(activity, formVersionHandler, getPreference(R.string.user_id, ""));
	}

	protected void checkFormVersionOffline(){
		DebugLog.d("check form ofline user pref: "+PrefUtil.getStringPref(R.string.user_id, "")+getString(R.string.offline_form));
		DebugLog.d("check form ofline user : "+getPreference(PrefUtil.getStringPref(R.string.user_id, "")+getString(R.string.offline_form), null));
		if (getPreference(PrefUtil.getStringPref(R.string.user_id, "")+getString(R.string.offline_form), null) != null){
			downloadSchedules();
		}
		else{
			showMessageDialog("Generate offline form");
			initFormOffline();
		}
	}

	public void setFlagScheduleSaved(boolean flagScheduleSaved) {
		this.flagScheduleSaved = flagScheduleSaved;
		hideDialog();
	}

	/**
     *  navigation
     * */
	protected void addFragment(BaseFragment fragment, int viewContainerResId) {
		FragmentManager fm = getSupportFragmentManager();
		addFragment(fm, fragment, viewContainerResId);
	}

	protected void addFragment(FragmentManager fragmentManager, BaseFragment fragment, int viewContainerResId) {
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.add(viewContainerResId, fragment, fragment.getClass().getSimpleName());
		ft.commit();
	}

    protected void replaceFragmentWith(BaseFragment fragment, int viewContainerResId) {
		FragmentManager fm = getSupportFragmentManager();
		replaceFragmentWith(fm, fragment, viewContainerResId);
    }

	protected void replaceFragmentWith(FragmentManager fragmentManager, BaseFragment fragment, int viewContainerResId) {
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.replace(viewContainerResId, fragment);
		ft.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.commit();
	}

	public static void navigateToLoginActivity() {
		Intent i = new Intent(MyApplication.getContext(), LoginActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		MyApplication.getContext().startActivity(i);
	}

	public static void navigateToGroupActivity(Context context, String scheduleId, int siteId, int workTypeId, String workTypeName, String dayDate) {
		Intent intent = new Intent(context, GroupActivity.class);
		intent.putExtra(Constants.KEY_SCHEDULEID, scheduleId);
		intent.putExtra(Constants.KEY_SITEID, siteId);
		intent.putExtra(Constants.KEY_DAYDATE, dayDate);
		intent.putExtra(Constants.KEY_WORKTYPEID, workTypeId);
		intent.putExtra(Constants.KEY_WORKTYPENAME, workTypeName);
		context.startActivity(intent);
	}

	public static void navigateToGroupWargaActivity(Context context, int dataIndex, String scheduleId, String parentId, int rowId, String workFormGroupId, String workFormGroupName, String workTypeName, String wargaId) {
		Intent intent = new Intent(context, GroupWargaActivity.class);
		intent.putExtra(Constants.KEY_DATAINDEX, dataIndex);
		intent.putExtra(Constants.KEY_SCHEDULEID, scheduleId);
		intent.putExtra(Constants.KEY_PARENTID, parentId);
		intent.putExtra(Constants.KEY_ROWID, rowId);
		intent.putExtra(Constants.KEY_WORKFORMGROUPID, workFormGroupId);
		intent.putExtra(Constants.KEY_WORKFORMGROUPNAME, workFormGroupName);
		intent.putExtra(Constants.KEY_WORKTYPENAME, workTypeName);
		intent.putExtra(Constants.KEY_WARGAID, wargaId);
		context.startActivity(intent);
	}

	public static void navigateToFormFillActivity(Context context, String scheduleId, int rowId, int workFormGroupId, String workFormGroupName, String workTypeName) {
		Intent intent = new Intent(context, FormFillActivity.class);
		intent.putExtra(Constants.KEY_SCHEDULEID, scheduleId);
		intent.putExtra(Constants.KEY_ROWID, rowId);
		intent.putExtra(Constants.KEY_WORKFORMGROUPID, workFormGroupId);
		intent.putExtra(Constants.KEY_WORKFORMGROUPNAME, workFormGroupName);
		intent.putExtra(Constants.KEY_WORKTYPENAME, workTypeName);
		context.startActivity(intent);
	}

	public static void navigateToFormFillActivity(Context context, String scheduleId, int rowId, int workFormGroupId, String workFormGroupName, String workTypeName, String wargaId, String barangId) {
		Intent intent = new Intent(context, FormFillActivity.class);
		intent.putExtra(Constants.KEY_SCHEDULEID, scheduleId);
		intent.putExtra(Constants.KEY_ROWID, rowId);
		intent.putExtra(Constants.KEY_WORKFORMGROUPID, workFormGroupId);
		intent.putExtra(Constants.KEY_WORKFORMGROUPNAME, workFormGroupName);
		intent.putExtra(Constants.KEY_WORKTYPENAME, workTypeName);
		intent.putExtra(Constants.KEY_WARGAID, wargaId);
		intent.putExtra(Constants.KEY_BARANGID, barangId);
		context.startActivity(intent);
	}

	public static void navigateToCheckinActivity(Context context, String userId, String scheduleId, int siteId, String dayDate, int workTypeId, String workTypeName) {
		Intent intent = new Intent(context, CheckInActivity.class);
		intent.putExtra(Constants.KEY_USERID, userId);
		intent.putExtra(Constants.KEY_SCHEDULEID, scheduleId);
		intent.putExtra(Constants.KEY_SITEID, siteId);
		intent.putExtra(Constants.KEY_DAYDATE, dayDate);
		intent.putExtra(Constants.KEY_WORKTYPEID, workTypeId);
		intent.putExtra(Constants.KEY_WORKTYPENAME, workTypeName);
		context.startActivity(intent);
	}

	/**
	 * ===== list all handlers ======
	 *
	 * */
	@SuppressLint("HandlerLeak")
	public Handler correctiveScheduleHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			hideDialog();

			Bundle bundle = msg.getData();
			Gson gson = new Gson();

			boolean isResponseOK = bundle.getBoolean("isresponseok");

			if (isResponseOK) {

				if (bundle.getString("json") != null) {
					String jsonCorrectiveSchedule = bundle.getString("json");

					CorrectiveScheduleResponseModel correctiveData = gson.fromJson(jsonCorrectiveSchedule, CorrectiveScheduleResponseModel.class);
					if (correctiveData != null) {

						CorrectiveScheduleConfig.setCorrectiveScheduleConfig(correctiveData);

						DebugLog.d("save corrective schedule config");
						MyApplication.getInstance().toast("Corrective schedules data berhasil diunduh", Toast.LENGTH_SHORT);
					}
				} else {

					MyApplication.getInstance().toast("JSON == null. Gagal mengunduh data schedule Corrective", Toast.LENGTH_LONG);
				}

			} else {

				MyApplication.getInstance().toast("Response not OK. Gagal mengunduh data schedule Corrective", Toast.LENGTH_LONG);
			}
		}
	};

	@SuppressLint("HandlerLeak")
	Handler scheduleHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {

			Bundle bundle = msg.getData();
			Gson gson = new Gson();

			boolean isResponseOK = bundle.getBoolean("isresponseok");

			if (isResponseOK) {

				if (bundle.getString("json") != null) {
					ScheduleResponseModel scheduleResponseModel = gson.fromJson(bundle.getString("json"), ScheduleResponseModel.class);
					if (scheduleResponseModel.status == 200){
						ScheduleSaver scheduleSaver = new ScheduleSaver();
						scheduleSaver.execute(scheduleResponseModel.data.toArray());
					}
				} else {
					hideDialog();
					setFlagScheduleSaved(true);
					Toast.makeText(activity, getString(R.string.failed_downloadschedule),Toast.LENGTH_LONG).show();
				}

			} else {
				hideDialog();
				DebugLog.d("repsonse not ok");
			}
		}
	};

	@SuppressLint("HandlerLeak")
	protected Handler scheduleHandlerTemp = new Handler() {
		public void handleMessage(android.os.Message msg) {
			Bundle bundle = msg.getData();
			Gson gson = new Gson();
			if (bundle.getString("json") != null) {
				ScheduleResponseModel scheduleResponseModel = gson.fromJson(bundle.getString("json"), ScheduleResponseModel.class);
				DebugLog.d("scheduleResponseModel.status : " + scheduleResponseModel.status);
				if (scheduleResponseModel.status == 200) {
					DeleteAllScheduleEvent deleteAllScheduleEvent = new DeleteAllScheduleEvent();
					deleteAllScheduleEvent.scheduleResponseModel = scheduleResponseModel;
					EventBus.getDefault().post(deleteAllScheduleEvent);
				}
			} else {
				hideDialog();
				Toast.makeText(activity, getString(R.string.failed_downloadschedule), Toast.LENGTH_LONG).show();
			}
		}
	};

    @SuppressLint("HandlerLeak")
    private Handler apkHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {

            Bundle bundle = msg.getData();
            Gson gson = new Gson();

            boolean isResponseOK = bundle.getBoolean("isresponseok");

            if (isResponseOK) {

                if (bundle.getString("json") != null){
                    VersionModel versionModel = gson.fromJson(msg.getData().getString("json"), VersionModel.class);
                    DebugLog.d("latest_version from server : " + versionModel.version);
                    writePreference(R.string.latest_version, versionModel.version);
                    writePreference(R.string.url_update, versionModel.download);

                    if (CommonUtil.isUpdateAvailable(getApplicationContext())) {

                        //update is mandatory, go to Settings screen to manually update apk
                        Toast.makeText(activity, getString(R.string.newUpdateSTPapplication), Toast.LENGTH_LONG).show();
                        startActivity(new Intent(BaseActivity.this, SettingActivity.class));

                    } else {

                        // lakukan cek dan unduh form ketika belum update apk
                        checkFormVersion();
                    }

                }else{

                    hideDialog();
                    Toast.makeText(activity, getString(R.string.memriksaUpdateGagal), Toast.LENGTH_LONG).show();
                }

            } else {

                hideDialog();
                DebugLog.d("repsonse not ok");
            }
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler formVersionHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {

            Bundle bundle = msg.getData();
            Gson gson = new Gson();

            boolean isResponseOK = bundle.getBoolean("isresponseok");

            if (isResponseOK) {

                if (bundle.getString("json") != null){
                    VersionModel model = gson.fromJson(msg.getData().getString("json"), VersionModel.class);
                    formVersion = model.version;
                    DebugLog.d("check version : "+PrefUtil.getStringPref(R.string.user_id, "")+getString(R.string.latest_version_form));
                    DebugLog.d("check version value : "+getPreference(PrefUtil.getStringPref(R.string.user_id, "")+getString(R.string.latest_version_form), "no value"));
                    DebugLog.d("check version value from web: "+formVersion);

                    if (!formVersion.equals(getPreference(PrefUtil.getStringPref(R.string.user_id, "")+getString(R.string.latest_version_form), "no value"))){

                        DebugLog.d("form needs update");
                        downloadNewForm();

                    }else{

                        DebugLog.d("form doesn't need to be updated");
                        if (!MyApplication.getInstance().getDEVICE_REGISTER_STATE()) {

                            // have never register the device, then register it now
                            requestReadPhoneStatePermission();

                        }
                        downloadSchedules();
                    }

                }else{

                    hideDialog();
                    Toast.makeText(activity, getString(R.string.formUpdateFailedFastInternet), Toast.LENGTH_LONG).show();
                }

            } else {

                hideDialog();
                DebugLog.d("repsonse not ok");
            }
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler formSaverHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {

            Bundle bundle = msg.getData();

            boolean isResponseOK = bundle.getBoolean("isresponseok");

            if (isResponseOK) {

                if (bundle.getString("json") != null){
                    initForm(bundle.getString("json"));
                }else{
                    hideDialog();
                    Toast.makeText(activity, getString(R.string.formUpdateFailedFastInternet), Toast.LENGTH_LONG).show();
                }

            } else {

                hideDialog();
                DebugLog.d("repsonse not ok");
            }
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler formImbasPetirSaverHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {

            Bundle bundle = msg.getData();

            boolean isResponseOK = bundle.getBoolean("isresponseok");

            if (isResponseOK) {
                if (bundle.getString("json") != null){
                    initFormImbasPetir(bundle.getString("json"));
                }else{
                    hideDialog();
                    Toast.makeText(activity, getString(R.string.formUpdateFailedFastInternet), Toast.LENGTH_LONG).show();
                }
            } else {

                hideDialog();
                DebugLog.d("repsonse not ok");
            }
        }
    };

	/**
	 * Permission
	 *
	 **/
	@AfterPermissionGranted(Constants.RC_STORAGE_PERMISSION)
	 void updateAPKwithStoragePermission() {

		String[] perms = new String[]{PermissionUtil.READ_EXTERNAL_STORAGE, PermissionUtil.WRITE_EXTERNAL_STORAGE};
		if (PermissionUtil.hasPermission(this, perms)) {

			// Already has permission
			DebugLog.d("Already have permission, do the thing");
			updateAPK();

		} else {

			// Do not have permissions, request them now
			DebugLog.d("Do not have permissions, request them now");
			PermissionUtil.requestPermission(this, getString(R.string.rationale_externalstorage), Constants.RC_STORAGE_PERMISSION, perms);
		}
	}

	@AfterPermissionGranted(Constants.RC_READ_PHONE_STATE)
	protected void requestReadPhoneStatePermission() {

		DebugLog.d("request read phone state permisson");
		if (PermissionUtil.hasPermission(this, PermissionUtil.READ_PHONE_STATE_PERMISSION)) {

			// Already has permission, do the thing
			DebugLog.d("Already have permission, do the thing");
			setFCMTokenRegistration();

		} else {

			// Do not have permissions, request them now
			DebugLog.d("Do not have permissions, request them now");
			PermissionUtil.requestPermission(this, getString(R.string.rationale_readphonestate), Constants.RC_READ_PHONE_STATE, PermissionUtil.READ_PHONE_STATE_PERMISSION);
		}
	}


	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		DebugLog.d("request permission result");

		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (requestCode == Constants.RC_READ_PHONE_STATE) {

			if (PermissionUtil.hasPermission(this, PermissionUtil.READ_PHONE_STATE_PERMISSION)) {

				DebugLog.d("read phone state allowed, start sending FCM token");
				setFCMTokenRegistration();
			}

		} else if (requestCode == Constants.RC_STORAGE_PERMISSION) {

			for (String permission : permissions) {

				if (permission.equalsIgnoreCase(PermissionUtil.READ_EXTERNAL_STORAGE) && PermissionUtil.hasPermission(this, PermissionUtil.READ_EXTERNAL_STORAGE)) {
					DebugLog.d("read external storage allowed");
					isReadStorageAllowed = true;
				}

				if (permission.equalsIgnoreCase(PermissionUtil.WRITE_EXTERNAL_STORAGE) && PermissionUtil.hasPermission(this, PermissionUtil.WRITE_EXTERNAL_STORAGE)) {
					DebugLog.d("write external storage allowed");
					isWriteStorageAllowed = true;
				}
			}

			isAccessStorageAllowed = isReadStorageAllowed & isWriteStorageAllowed;

			if (isAccessStorageAllowed) {
				updateAPK();
			} else {
				DebugLog.e(getString(R.string.failed_update_apk));
				Toast.makeText(this, getString(R.string.failed_update_apk), Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
		DebugLog.d("permission granted");

		for (String permission : perms) {

			if (permission.equalsIgnoreCase(Manifest.permission.READ_PHONE_STATE)) {
				// read phone state allowed, i.e. read IMEI

				DebugLog.d("read phone state allowed, start sending FCM token");
				setFCMTokenRegistration();
			}
		}
	}

	@Override
	public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
		DebugLog.d("onPermissionsDenied:" + requestCode + ":" + perms.size());

		// (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
		// This will display a dialog directing them to enable the permission in app settings.
		if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
			new AppSettingsDialog.Builder(this).build().show();
		}
	}

	private void setFCMTokenRegistration() {

		String FCMRegToken = PrefUtil.getStringPref(R.string.app_fcm_reg_id, "");
		String AccessToken = APIHelper.getAccessToken(this);

		if (!TextUtils.isEmpty(AccessToken)) {

			if (!TextUtils.isEmpty(FCMRegToken)) {

				MyApplication.sendRegIdtoServer(FCMRegToken);
				MyApplication.getInstance().setDEVICE_REGISTER_STATE(false);

			} else {

				DebugLog.e("FCM TOKEN is empty");
				Crashlytics.log("FCM TOKEN is empty");
			}

		} else {

			DebugLog.e("ACCESS TOKEN is empty, unable to send FCM TOKEN to server");
			Crashlytics.log("ACCESS TOKEN is empty, unable to send FCM TOKEN to server");

		}
	}

	protected void updateAPK() {

		if (GlobalVar.getInstance().isNetworkOnline(this)) {
			new DownloadFileFromURL().execute(Constants.URL_APK);
		} else {
			Toast.makeText(this, getString(R.string.failed_disconnected), Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * async task class
	 *
	 * */
	private class FormSaver extends AsyncTask<Object, Integer, Void> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showMessageDialog("Persiapan menyimpan forms");
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
				if (((WorkFormModel)params[i]).groups != null)
					for (WorkFormGroupModel group : ((WorkFormModel)params[i]).groups) {
						if (group.table == null){
							continue;
						}
						DebugLog.d("group name : "+group.name);
						DebugLog.d("group table : "+group.table.toString());
						DebugLog.d("group table header : "+group.table.headers.toString());
						sum += group.table.headers.size();
						sum += group.table.rows.size();
					}
			}

			int curr = 0;
			for (int i = 0; i < params.length; i++) {
				((WorkFormModel)params[i]).save();
				if (((WorkFormModel)params[i]).groups != null)
					for (WorkFormGroupModel group : ((WorkFormModel)params[i]).groups) {
						if (group.table == null){
							continue;
						}
						for (ColumnModel columnModel : group.table.headers) {
							curr ++;
							publishProgress(curr*100/sum);
							columnModel.save();
						}

						for (RowModel rowModel : group.table.rows) {
							curr ++;
							publishProgress(curr*100/sum);
							rowModel.save();
						}
					}
			}
			DebugLog.d("version saved : "+PrefUtil.getStringPref(R.string.user_id, "")+getString(R.string.latest_version_form));
			DebugLog.d("version saved value : "+getPreference(PrefUtil.getStringPref(R.string.user_id, "")+getString(R.string.latest_version_form), "no value"));
			DebugLog.d("version saved value from web: "+ formVersion);
			writePreference(PrefUtil.getStringPref(R.string.user_id, "")+getString(R.string.latest_version_form), formVersion);
			writePreference(PrefUtil.getStringPref(R.string.user_id, "")+getString(R.string.offline_form),"not null");
			DebugLog.d("form ofline user pref: "+PrefUtil.getStringPref(R.string.user_id, "")+getString(R.string.offline_form));
			DebugLog.d("form ofline user : "+getPreference(PrefUtil.getStringPref(R.string.user_id, "")+getString(R.string.offline_form), null));
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			DebugLog.d("saving forms "+values[0]+" %...");
			showMessageDialog("saving forms "+values[0]+" %...");
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			//			setFlagFormSaved(true);
			showMessageDialog("saving forms complete");

			if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP))
                downloadNewFormImbasPetir();
			else
				downloadSchedules();

		}
	}

    private class FormImbasPetirSaver extends AsyncTask<Object, Integer, Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showMessageDialog("Persiapan menyimpan form imbas petir");
        }

        @Override
        protected Void doInBackground(Object... params) {
            int sum = 0;
            for (int i = 0; i < params.length; i++) {
                if (((WorkFormModel)params[i]).groups != null)
                    for (WorkFormGroupModel group : ((WorkFormModel)params[i]).groups) {
                        if (group.table == null){
                            continue;
                        }
                        DebugLog.d("group name : "+group.name);
                        DebugLog.d("group table : "+group.table.toString());
                        DebugLog.d("group table header : "+group.table.headers.toString());
                        sum += group.table.headers.size();
                        sum += group.table.rows.size();
                    }
            }

            int curr = 0;
            for (int i = 0; i < params.length; i++) {
                ((WorkFormModel)params[i]).save();
                if (((WorkFormModel)params[i]).groups != null)
                    for (WorkFormGroupModel group : ((WorkFormModel)params[i]).groups) {
                        if (group.table == null){
                            continue;
                        }
                        for (ColumnModel columnModel : group.table.headers) {
                            curr ++;
                            publishProgress(curr*100/sum);
                            columnModel.save();
                        }

                        for (RowModel rowModel : group.table.rows) {
                            curr ++;
                            publishProgress(curr*100/sum);
                            rowModel.save();
                        }
                    }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            DebugLog.d("saving forms "+values[0]+" %...");
            showMessageDialog("saving forms "+values[0]+" %...");
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            showMessageDialog("saving form imbas petir is complete");
            downloadSchedules();
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
			try {
				URL url = new URL(f_url[0]);
				URLConnection conection = url.openConnection();
				conection.connect();

				// this will be useful so that you can show a tipical 0-100% progress bar
				int lenghtOfFile = conection.getContentLength();

				File apkFile = new File(Constants.PATH_APK);
				File apkDir = new File(Constants.DIR_APK);
				if (!apkDir.exists()) apkDir.mkdirs();

				// download the file
				BufferedInputStream input = new BufferedInputStream(url.openStream(), 8192);
				OutputStream output = new FileOutputStream(apkFile);
				byte data[] = new byte[1024];
				long total = 0;

				DebugLog.d("start download with size " + lenghtOfFile);
				int count;
				while ((count = input.read(data, 0, 1024)) != -1) {
					total += count;
					// publishing the progress....
					// After this onProgressUpdate will be called
					publishProgress("" + (int) ((total * 100) / lenghtOfFile));
					DebugLog.d("(count, total, progress) : (" + count + ", " + total + ", " + (int) ((total * 100) / lenghtOfFile) + ")");
					// writing data to file
					output.write(data, 0, count);
				}
				DebugLog.d("finish download");
				output.flush();
				output.close();
				input.close();
				return true;
			} catch (MalformedURLException mae) {
				DebugLog.e(mae.getMessage(), mae);
			} catch (IOException ioe) {
				DebugLog.e(ioe.getMessage(), ioe);
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
				Toast.makeText(BaseActivity.this, BaseActivity.this.getString(R.string.failed_update_apk), Toast.LENGTH_LONG).show();
			} else {

				// just install the new APK
				CommonUtil.installAPK(activity, BaseActivity.this);
			}
		}
	}
}
