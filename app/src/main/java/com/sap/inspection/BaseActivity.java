package com.sap.inspection;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
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
import com.sap.inspection.event.DeleteAllScheduleEvent;
import com.sap.inspection.event.ScheduleTempProgressEvent;
import com.sap.inspection.event.UploadProgressEvent;
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

import java.io.IOException;
import java.io.InputStream;
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
	public static DisplayImageOptions  avatarOptions = new DisplayImageOptions.Builder()
															.showStubImage(R.drawable.logo_app)
															.cacheInMemory()
															.cacheOnDisc()
															.build();
	
	public static DisplayImageOptions  itemOptions = new DisplayImageOptions.Builder()
															.cacheInMemory()
															.cacheOnDisc()
															.build();

	private boolean instanceStateSaved;
	private ProgressDialog progressDialog;


	private String formVersion;
	private boolean flagScheduleSaved = false;
	private boolean flagFormSaved = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		progressDialog = new ProgressDialog(this);
		progressDialog.setCancelable(false);

		activity = this;
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
			Toast.makeText(activity, "Schedule diperbaharui", Toast.LENGTH_SHORT).show();
		} else
			showMessageDialog("menyimpan schedule " + event.progress + " %...");
	}

	public void onEvent(DeleteAllScheduleEvent event) {
		DbRepository.getInstance().open(MyApplication.getInstance());
		DbRepository.getInstance().clearData(DbManager.mSchedule);
		DbRepository.getInstance().close();
		ScheduleTempSaver scheduleSaver = new ScheduleTempSaver();
		scheduleSaver.setActivity(activity);
		scheduleSaver.execute(event.scheduleResponseModel.data.toArray());
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

	public void hideDialog() {

		if (progressDialog != null && progressDialog.isShowing())
			progressDialog.dismiss();

	}

	public void showMessageDialog(String message) {

		if (progressDialog != null) {
			progressDialog.setMessage(message);

			if (!progressDialog.isShowing())
				progressDialog.show();
		}
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
		//		progress_dialog.setMessage("initialize default form template");
		//		progress_dialog.setCancelable(false);
		//		progress_dialog.show();
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
			showMessageDialog(getString(R.string.getScheduleFromServer));
			APIHelper.getSchedules(activity, scheduleHandler, getPreference(R.string.user_id, ""));
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

	public boolean isFlagFormSaved() {
		return flagFormSaved;
	}

	public boolean isFlagScheduleSaved() {
		return flagScheduleSaved;
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
	protected Handler apkHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			CommonUtil.fixVersion(getApplicationContext());
			if (msg.getData() != null && msg.getData().getString("json") != null){
				VersionModel model = new Gson().fromJson(msg.getData().getString("json"), VersionModel.class);
				DebugLog.d("latest_version from server : " + model.version);
				writePreference(R.string.latest_version, model.version);
				writePreference(R.string.url_update, model.download);
				String version = null;
				try {
					version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
					DebugLog.d(version);
				} catch (PackageManager.NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				if (!version.equalsIgnoreCase(getPreference(R.string.latest_version, "")) /*&& !getPreference(R.string.url_update, "").equalsIgnoreCase("")*/){

				if (CommonUtil.isUpdateAvailable(getApplicationContext())) {
					//String update STP version
					Toast.makeText(activity, getString(R.string.newUpdateSTPapplication), Toast.LENGTH_LONG).show();
					startActivity(new Intent(BaseActivity.this, SettingActivity.class));
				} else {

					// lakukan cek dan unduh form ketka belum update apk
					checkFormVersion();
				}

			}else{
				Toast.makeText(activity, getString(R.string.memriksaUpdateGagal), Toast.LENGTH_LONG).show();
			}
			// jangan lakukan cek dan unduh form ketika belum update apk
			//checkFormVersion();
			//checkFormVersionOffline();
		};
	};

	@SuppressLint("HandlerLeak")
	protected Handler formVersionHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {

			hideDialog();

			Bundle bundle = msg.getData();
			Gson gson = new Gson();

			boolean isResponseOK = bundle.getBoolean("isresponseok");

			if (isResponseOK) {

				if (bundle.getString("json") != null){
					VersionModel model = gson.fromJson(msg.getData().getString("json"), VersionModel.class);
					formVersion = model.version;
					DebugLog.d("check version : "+ PrefUtil.getStringPref(R.string.user_id, "")+getString(R.string.latest_version_form));
					DebugLog.d("check version value : "+getPreference(PrefUtil.getStringPref(R.string.user_id, "")+getString(R.string.latest_version_form), "no value"));
					DebugLog.d("check version value from web: "+formVersion);

					if (!formVersion.equals(getPreference(PrefUtil.getStringPref(R.string.user_id, "")+getString(R.string.latest_version_form), "no value"))){

						DebugLog.d("form needs update");
						showMessageDialog(getString(R.string.getNewfromServer));
						APIHelper.getForms(activity, formSaverHandler, getPreference(R.string.user_id, ""));

					}else{

						DebugLog.d("form doesn't need to be updated");
						if (!MyApplication.getInstance().getDEVICE_REGISTER_STATE()) {

							// haven't yet register device, do device registration
							requestReadPhoneStatePermission();

						}
						showMessageDialog(getString(R.string.getScheduleFromServer));
						APIHelper.getSchedules(activity, scheduleHandler, getPreference(R.string.user_id, ""));
					}

				}else{

					Toast.makeText(activity, getString(R.string.formUpdateFailedFastInternet), Toast.LENGTH_LONG).show();
				}

			} else {

				DebugLog.d("response is not OK");
			}
		}
	};

	@SuppressLint("HandlerLeak")
	protected Handler formSaverHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {

			hideDialog();

			Bundle bundle = msg.getData();

			boolean isResponseOK = bundle.getBoolean("isresponseok");

			if (isResponseOK) {

				if (bundle.getString("json") != null){
					initForm(bundle.getString("json"));
				}else{
					Toast.makeText(activity, getString(R.string.formUpdateFailedFastInternet), Toast.LENGTH_LONG).show();
				}

			} else {

			}
		}
	};

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
			APIHelper.getSchedules(activity, scheduleHandler, getPreference(R.string.user_id, ""));
		}
	}
}
