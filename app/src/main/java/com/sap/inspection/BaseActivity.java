package com.sap.inspection;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

/*import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;*/
import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
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
import com.sap.inspection.model.form.ColumnModel;
import com.sap.inspection.model.form.RowModel;
import com.sap.inspection.model.form.WorkFormGroupModel;
import com.sap.inspection.model.form.WorkFormModel;
import com.sap.inspection.model.responsemodel.FormResponseModel;
import com.sap.inspection.model.responsemodel.ScheduleResponseModel;
import com.sap.inspection.model.responsemodel.VersionModel;
import com.sap.inspection.model.value.DbRepositoryValue;
import com.sap.inspection.task.ScheduleSaver;
import com.sap.inspection.task.ScheduleTempSaver;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.CommonUtil;
import com.sap.inspection.util.PermissionUtil;
import com.sap.inspection.util.PrefUtil;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import de.greenrobot.event.EventBus;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

//import com.sap.inspection.gcm.GCMService;

public abstract class BaseActivity extends FragmentActivity implements EasyPermissions.PermissionCallbacks {

	protected FragmentActivity activity;
	public static final int ACTIVITY_REQUEST_CODE = 311; 
	protected SharedPreferences mPref;

	public static ImageLoader imageLoader = ImageLoader.getInstance();

	// Progress dialog type (0 - for Horizontal progress bar)
	public static final int progress_bar_type = 0;

	// File url to download
	private static String file_url;

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
		file_url = mPref.getString(this.getString(R.string.url_update), "");

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
	}

	@Override
	protected void onResume() {
		super.onResume();
		EventBus.getDefault().register(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		EventBus.getDefault().unregister(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
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
	
	public int getPreference(int key, int defaultValue) {
		return mPref.getInt(getString(key), defaultValue);
	}
	
	public boolean getPreference(int key, boolean defaultValue) {
		return mPref.getBoolean(getString(key), defaultValue);
	}

	protected int color(int colorRes) {
		return ContextCompat.getColor(this, colorRes);
	}

	protected void trackThisPage(String name) {
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
		MyApplication myApplication = (MyApplication) getApplication();
		FirebaseAnalytics mFirebaseAnalytics = myApplication.getDefaultAnalytics();
		mFirebaseAnalytics.logEvent("track_page", bundle);
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

    private void initFormImbasPetir(String json) {
        Gson gson = new Gson();
        FormResponseModel formResponseModel = gson.fromJson(json, FormResponseModel.class);
        if (formResponseModel.status == 200) {
            new FormImbasPetirSaver().execute(formResponseModel.data.toArray());
        }
    }

	protected void checkAPKVersion(){
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

	/** Dialog for asking GPS permission to user **/
	protected LovelyStandardDialog gpsDialog() {
		return new LovelyStandardDialog(this, R.style.CheckBoxTintTheme)
				.setTopColor(color(R.color.theme_color))
				.setButtonsColor(color(R.color.theme_color))
				.setIcon(R.drawable.logo_app)
				//string title information GPS
				.setTitle(getString(R.string.informationGPS))
				.setMessage("Silahkan aktifkan GPS")
				.setCancelable(false)
				.setPositiveButton(android.R.string.yes, v -> {
					Intent gpsOptionsIntent = new Intent(
							Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					startActivity(gpsOptionsIntent);
				});
	}

	/** checking any network is available or not **/
	protected boolean isNetworkAvailable() {
		return GlobalVar.getInstance().anyNetwork(this);
	}

	/** Showing network permission dialog if network is not available **/
	protected void networkPermissionDialog() {
		if (!isNetworkAvailable()){
			new LovelyStandardDialog(this, R.style.CheckBoxTintTheme)
					.setTopColor(color(R.color.theme_color))
					.setButtonsColor(color(R.color.theme_color))
					.setIcon(R.drawable.logo_app)
					.setTitle("Information")
					.setMessage("No internet connection. Please connect your network.")
					.setCancelable(false)
					.setPositiveButton(android.R.string.yes, v -> {
						startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
					})
					.show();
		}
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

    protected void navigateToFragment(BaseFragment fragment, int viewContainerResId) {
		FragmentManager fm = getSupportFragmentManager();
		navigateToFragment(fm, fragment, viewContainerResId);
    }

	protected void navigateToFragment(FragmentManager fragmentManager, BaseFragment fragment, int viewContainerResId) {
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.replace(viewContainerResId, fragment);
		ft.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.commit();
	}

	protected void navigateToLoginActivity() {
		Intent i = new Intent(BaseActivity.this, LoginActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(i);
	}

	/**
	 * ===== list all handlers ======
	 *
	 * */
	@SuppressLint("HandlerLeak")
	Handler scheduleHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {

			Bundle bundle = msg.getData();
			Gson gson = new Gson();

			boolean isResponseOK = bundle.getBoolean("isresponseok");

			if (isResponseOK) {

				if (bundle.getString("json") != null){
					ScheduleResponseModel scheduleResponseModel = gson.fromJson(bundle.getString("json"), ScheduleResponseModel.class);
					if (scheduleResponseModel.status == 200){
						ScheduleSaver scheduleSaver = new ScheduleSaver();
						scheduleSaver.execute(scheduleResponseModel.data.toArray());
					}
				} else{
					hideDialog();
					setFlagScheduleSaved(true);
					Toast.makeText(activity, getString(R.string.cantgetschedulefastinternet),Toast.LENGTH_LONG).show();
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
				Toast.makeText(activity, getString(R.string.cantgetschedulefastinternet), Toast.LENGTH_LONG).show();
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

                CommonUtil.fixVersion(getApplicationContext());
                if (bundle.getString("json") != null){
                    VersionModel model = gson.fromJson(msg.getData().getString("json"), VersionModel.class);
                    DebugLog.d("latest_version from server : " + model.version);
                    writePreference(R.string.latest_version, model.version);
                    writePreference(R.string.url_update, model.download);
                    String version;
                    try {
                        version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                        DebugLog.d(version);
                    } catch (PackageManager.NameNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        Crashlytics.logException(e);
                        hideDialog();
                        Toast.makeText(activity, "check apk version error : " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    if (CommonUtil.isUpdateAvailable(getApplicationContext())) {

                        //update is mandatory, go to Settings screen to manually update apk
                        Toast.makeText(activity, getString(R.string.newUpdateSTPapplication), Toast.LENGTH_LONG).show();
                        startActivity(new Intent(BaseActivity.this, SettingActivity.class));

                    } else {

                        // lakukan cek dan unduh form ketka belum update apk
                        checkFormVersion();
                    }

                }else{

                    hideDialog();
                    Toast.makeText(activity, getString(R.string.memriksaUpdateGagal), Toast.LENGTH_LONG).show();
                }
                // jangan lakukan cek dan unduh form ketika belum update apk
                //checkFormVersion();
                //checkFormVersionOffline();
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

                            // haven't yet register device, do device registration
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
	 void requestStoragePermission() {

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

				Toast.makeText(this, "Gagal mengunduh APK karena tidak ada izin akses storage", Toast.LENGTH_LONG).show();

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

	private void updateAPK() {

		if (GlobalVar.getInstance().isNetworkOnline(this)) {
			new SettingActivity.DownloadFileFromURL().execute(file_url);
		} else {
			Toast.makeText(this, getString(R.string.disconnected), Toast.LENGTH_LONG).show();
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
				OutputStream output = new FileOutputStream(tempDir.getAbsolutePath() + "/sapInspection" + mPref.getString(BaseActivity.this.getString(R.string.latest_version), "") + ".apk");
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
				Toast.makeText(BaseActivity.this, "Gagal mengunduh APK terbaru. Periksa jaringan Anda", Toast.LENGTH_LONG).show();
			} else {

				// just install the new APK
				CommonUtil.installAPK(activity, BaseActivity.this);
			}
		}
	}
}
