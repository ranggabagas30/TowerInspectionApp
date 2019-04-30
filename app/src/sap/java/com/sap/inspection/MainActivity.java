package com.sap.inspection;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.sap.inspection.connection.APIHelper;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.constant.GlobalVar;
import com.sap.inspection.event.ScheduleProgressEvent;
import com.sap.inspection.fragments.BaseFragment;
import com.sap.inspection.fragments.ScheduleFragment;
import com.sap.inspection.mainmenu.MainMenuFragment;
import com.sap.inspection.manager.AlertDialogManager;
import com.sap.inspection.model.DbManager;
import com.sap.inspection.model.DbRepository;
import com.sap.inspection.model.form.ColumnModel;
import com.sap.inspection.model.form.RowModel;
import com.sap.inspection.model.form.WorkFormGroupModel;
import com.sap.inspection.model.form.WorkFormModel;
import com.sap.inspection.model.responsemodel.FormResponseModel;
import com.sap.inspection.model.responsemodel.ScheduleResponseModel;
import com.sap.inspection.model.responsemodel.VersionModel;
import com.sap.inspection.task.DownloadFileFromURL;
import com.sap.inspection.task.ScheduleSaver;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.PrefUtil;
import com.sap.inspection.util.PermissionUtil;
import com.sap.inspection.util.CommonUtil;
import com.slidinglayer.SlidingLayer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks {

	private SlidingLayer mSlidingLayer;
	public static final int REQUEST_CODE = 100;

	private static String formVersion;

	private MainMenuFragment mainMenuFragment = MainMenuFragment.newInstance();
	private ScheduleFragment scheduleFragment = ScheduleFragment.newInstance();
	private BaseFragment currentFragment;

	private ProgressDialog progressDialog;
	private boolean flagScheduleSaved = false;
	private boolean flagFormSaved = false;
	private int lastClicked = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		progressDialog = new ProgressDialog(this);
		progressDialog.setCancelable(false);

		boolean isLoadSchedule   = getIntent().getBooleanExtra(Constants.LOADSCHEDULE,false);
		boolean isLoadAfterLogin = getIntent().getBooleanExtra(Constants.LOADAFTERLOGIN,false);

		DebugLog.d("Constants.LOADSCHEDULE : " + isLoadSchedule);
		DebugLog.d("Constants.LOADAFTERLOGIN : " + isLoadAfterLogin);

		if (isLoadSchedule) {

			DebugLog.d("load schedule");
			if (!MyApplication.getInstance().getDEVICE_REGISTER_STATE()) {

				// haven't yet register device, do device registration
				requestReadPhoneStatePermission();

			}
			showMessageDialog(getString(R.string.getScheduleFromServer));
			APIHelper.getSchedules(activity, scheduleHandler, getPreference(R.string.user_id, ""));

		} else if (isLoadAfterLogin) {

			DebugLog.d("load after login");
			setFlagScheduleSaved(true);
		}

		/**
		 * added by : Rangga
		 * date : 26/02/2019
		 * reason : every time application launched, should check app's latest version
		 *          in order to make sure that using only the latest version
		 * */
		checkAPKVersion();

		mSlidingLayer = (SlidingLayer) findViewById(R.id.slidingLayer1);
		mSlidingLayer.setStickTo(SlidingLayer.STICK_TO_LEFT);
		LayoutParams rlp = (LayoutParams) mSlidingLayer.getLayoutParams();
		rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		rlp.width = LayoutParams.MATCH_PARENT;
		mainMenuFragment.setMainMenuClickListener(mainMenuClick);

		navigateToFragment(mainMenuFragment, R.id.fragment_behind);
		navigateToFragment(scheduleFragment, R.id.fragment_front);
		trackThisPage("Main");
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (lastClicked != -1){
			scheduleFragment.setScheduleBy(lastClicked);
		}
	}

	@Override
	public void onBackPressed() {
		if (mSlidingLayer.isOpened())
			mSlidingLayer.closeLayer(true);
		else
			finish();
	}

	OnClickListener mainMenuClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			MyApplication.getInstance().setIsInCheckHasilPm(false);
			int i = (Integer) v.getTag();
			switch (i) {
				case R.string.schedule: // R.id.s1
					DebugLog.d("schedule");
					trackThisPage(getResources().getString(R.string.schedule));
					scheduleFragment.setScheduleBy(R.string.schedule);
					break;
				case R.string.site_audit: // R.id.s2
					DebugLog.d("site audit");
					trackThisPage(getResources().getString(R.string.site_audit));
					scheduleFragment.setScheduleBy(R.string.site_audit);
					break;
				case R.string.preventive: // R.id.s3
					DebugLog.d("preventive");
					trackThisPage(getResources().getString(R.string.preventive));
					scheduleFragment.setScheduleBy(R.string.preventive);
					break;
				case R.string.corrective: // R.id.s4
					DebugLog.d("corrective");
					trackThisPage(getResources().getString(R.string.corrective));
					scheduleFragment.setScheduleBy(R.string.corrective);
					break;
				case R.string.foto_imbas_petir: //R.id.s5
					DebugLog.d("foto imbas petir");
					trackEvent(getString(R.string.foto_imbas_petir));
					scheduleFragment.setScheduleBy(R.string.foto_imbas_petir);
					break;
				case R.string.settings: // R.id.s6
					DebugLog.d("settings");
					trackThisPage(getResources().getString(R.string.settings));
					Intent intent = new Intent(activity, SettingActivity.class);
					startActivity(intent);
					return;
				case R.string.hasil_PM: // R.id.s7
					DebugLog.d("hasil PM");
					trackEvent(getResources().getString(R.string.hasil_PM));
					scheduleFragment.setScheduleBy(R.string.hasil_PM);
					break;

				default:
					break;
			}
			mSlidingLayer.openLayer(true);
		}
	};

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

	private void navigateToFragment(BaseFragment fragment, int viewContainerResId) {
		if (fragment.equals(currentFragment))
			return;
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(viewContainerResId, fragment);
		ft.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.commit();
	}

	public void setFlagScheduleSaved(boolean flagScheduleSaved) {
		this.flagScheduleSaved = flagScheduleSaved;
		if (progressDialog != null && progressDialog.isShowing())
			progressDialog.dismiss();
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

	public void initForm(String json){
		Gson gson = new Gson();
		FormResponseModel formResponseModel = gson.fromJson(json,FormResponseModel.class);
		if (formResponseModel.status == 200){
			new FormSaver().execute(formResponseModel.data.toArray());
		}
	}

	private void initFormImbasPetir(String json) {
		Gson gson = new Gson();
		FormResponseModel formResponseModel = gson.fromJson(json, FormResponseModel.class);
		if (formResponseModel.status == 200) {
			new FormImbasPetirSaver().execute(formResponseModel.data.toArray());
		}
	}

	/*private void checkAPKVersion(){
		DebugLog.d("check apk version");
		showMessageDialog(getString(R.string.checkversionapplication));
		APIHelper.getAPKVersion(activity, apkHandler, getPreference(R.string.user_id, ""));
	}*/

	/*private void checkFormVersion(){
		DebugLog.d("check form version");
		showMessageDialog(getString(R.string.checkfromversion));
		APIHelper.getFormVersion(activity, formVersionHandler, getPreference(R.string.user_id, ""));
	}*/

	/*private void checkFormVersionOffline(){
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
	}*/

	public void onEvent(ScheduleProgressEvent event) {
		if (!event.done)
			showMessageDialog("Menyimpan jadwal " + event.progress + " %...");
		else
			hideDialog();
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
					} catch (NameNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Crashlytics.logException(e);
                        hideDialog();
						Toast.makeText(activity, "check apk version error : " + e.getMessage(), Toast.LENGTH_LONG).show();
					}

					if (CommonUtil.isUpdateAvailable(getApplicationContext())) {

						//update is mandatory, go to Settings screen to manually update apk
						Toast.makeText(activity, getString(R.string.newUpdateSTPapplication), Toast.LENGTH_LONG).show();
						startActivity(new Intent(MainActivity.this, SettingActivity.class));

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
	 *
	 * ====== list all async task ======
	 *
	 * */
	private class FormSaver extends AsyncTask<Object, Integer, Void>{
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
			DebugLog.d("version saved value from web: "+formVersion);
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
			showMessageDialog("saving forms complete");
			APIHelper.getFormImbasPetir(activity, formImbasPetirSaverHandler);

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
			showMessageDialog(getString(R.string.getScheduleFromServer));
			APIHelper.getSchedules(activity, scheduleHandler, getPreference(R.string.user_id, ""));
		}
	}

	/**
	 * Permission
	 *
	 **/
	/*@AfterPermissionGranted(Constants.RC_READ_PHONE_STATE)
	private void requestReadPhoneStatePermission() {

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
	}*/

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

	/**
	 *
	 * ====== functions which need permission ======
	 *
	 * */
	private void setFCMTokenRegistration() {

		String FCMRegToken = com.sap.inspection.util.PrefUtil.getStringPref(R.string.app_fcm_reg_id, "");
		String AccessToken = APIHelper.getAccessToken(this);

		if (!AccessToken.isEmpty()) {

			if (!FCMRegToken.equalsIgnoreCase("")) {

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
}