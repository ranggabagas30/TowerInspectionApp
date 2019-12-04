package com.sap.inspection.view.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.pixplicity.easyprefs.library.Prefs;
import com.sap.inspection.BuildConfig;
import com.sap.inspection.R;
import com.sap.inspection.TowerApplication;
import com.sap.inspection.connection.APIHelper;
import com.sap.inspection.connection.rest.TowerAPIHelper;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.mainmenu.MainMenuFragment;
import com.sap.inspection.tools.AndroidUID;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.CommonUtil;
import com.sap.inspection.util.NetworkUtil;
import com.sap.inspection.util.PermissionUtil;
import com.sap.inspection.util.PrefUtil;
import com.sap.inspection.view.ui.fragments.ScheduleFragment;
import com.slidinglayer.SlidingLayer;

import java.net.HttpURLConnection;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks {

	private SlidingLayer mSlidingLayer;
	public static final int REQUEST_CODE = 100;

	private MainMenuFragment mainMenuFragment = MainMenuFragment.newInstance();
	private ScheduleFragment scheduleFragment = ScheduleFragment.newInstance();
	private int lastClicked = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mSlidingLayer = findViewById(R.id.slidingLayer1);
		mSlidingLayer.setStickTo(SlidingLayer.STICK_TO_LEFT);

		LayoutParams rlp = (LayoutParams) mSlidingLayer.getLayoutParams();
		rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		rlp.width = LayoutParams.MATCH_PARENT;

		mainMenuFragment.setMainMenuClickListener(mainMenuClick);
		replaceFragmentWith(getSupportFragmentManager(), mainMenuFragment, R.id.fragment_behind);
		replaceFragmentWith(getSupportFragmentManager(), scheduleFragment, R.id.fragment_front);

		// remove legacy sharedpref keys
		String latestFormVersion = Prefs.getString(Prefs.getString(getString(R.string.user_id), "") + "latest_version_form", null);
		String latestAPKversion  = Prefs.getString("latest_version", null);
		String apkUpdateUrl		 = Prefs.getString("url_update", null);

		if (!TextUtils.isEmpty(latestFormVersion)) Prefs.putString(Constants.KEY_LATEST_FORM_VERSION, latestFormVersion);
		if (!TextUtils.isEmpty(latestAPKversion)) Prefs.putString(Constants.KEY_LATEST_APK_VERSION, latestAPKversion);
		if (!TextUtils.isEmpty(apkUpdateUrl)) Prefs.putString(Constants.KEY_APK_UPDATE_URL, apkUpdateUrl);

		Prefs.remove(Prefs.getString(getString(R.string.user_id), "")+ "latest_version_form");
		Prefs.remove("latest_version");
		Prefs.remove("url_update");

		requestReadPhoneStatePermission();

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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE && resultCode == RESULT_CANCELED) {
			navigateToLoginActivity(this);
		}
	}

	@RequiresPermission(Manifest.permission.READ_PHONE_STATE)
	private void registerDevice() throws NullPointerException{
		String fcmRegToken = PrefUtil.getStringPref(R.string.app_fcm_reg_id, null);
		String accessToken = APIHelper.getAccessToken(this);
		String deviceId	   = AndroidUID.getDeviceID(this);

		if (TextUtils.isEmpty(accessToken))
			throw new NullPointerException(getString(R.string.error_access_token_empty));

		if (TextUtils.isEmpty(fcmRegToken))
			throw new NullPointerException(getString(R.string.error_fcm_token_empty));

		if (TextUtils.isEmpty(deviceId))
			throw new NullPointerException(getString(R.string.error_device_id_empty));

		sendRegIdtoServer(fcmRegToken, deviceId, BuildConfig.VERSION_NAME);
		TowerApplication.getInstance().setDEVICE_REGISTRATION_STATE(false);
	}

	@AfterPermissionGranted(Constants.RC_READ_PHONE_STATE)
	private void requestReadPhoneStatePermission() {
		if (PermissionUtil.hasPermission(this, PermissionUtil.READ_PHONE_STATE_PERMISSION)) {
			try {
				registerDevice();
				checkLatestAppVersion();
			} catch (NullPointerException e) {
				DebugLog.e(e.getMessage(), e);
			}
		} else {
			PermissionUtil.requestPermission(this, getString(R.string.rationale_readphonestate), Constants.RC_READ_PHONE_STATE, PermissionUtil.READ_PHONE_STATE_PERMISSION);
		}
	}

	@SuppressLint("MissingPermission")
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	@Override
	public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
		if (requestCode == Constants.RC_READ_PHONE_STATE) {
			if (PermissionUtil.hasPermission(this, PermissionUtil.READ_PHONE_STATE_PERMISSION)) {
				try {
					registerDevice();
					checkLatestAppVersion();
				} catch (NullPointerException e) {
					DebugLog.e(e.getMessage(), e);
				}
			}
		}
	}

	@Override
	public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
		if (requestCode == Constants.RC_READ_PHONE_STATE) {
			if (!PermissionUtil.hasPermission(this, PermissionUtil.READ_PHONE_STATE_PERMISSION)) {
				new AppSettingsDialog.Builder(this).build().show();
			}
		}
	}

	private OnClickListener mainMenuClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			TowerApplication.getInstance().setIS_CHECKING_HASIL_PM(false);
			int idMenu = (Integer) v.getTag();
			if (idMenu == R.string.settings) {
				DebugLog.d("settings");
				Intent intent = new Intent(activity, SettingActivity.class);
				startActivity(intent);
			} else {
				scheduleFragment.setScheduleBy(idMenu);
				mSlidingLayer.openLayer(true);
			}
		}
	};

	private void checkLatestAppVersion() {
		DebugLog.d("--> CHECK LATEST APK VERSION");
		showMessageDialog(getString(R.string.check_app_version));
		compositeDisposable.add(
				TowerAPIHelper.getAPKVersion()
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(
								apkVersionResponse -> {
									if (apkVersionResponse != null) {
										Prefs.putString(getString(R.string.latest_apk_version), apkVersionResponse.version);
										Prefs.putString(getString(R.string.apk_update_url), apkVersionResponse.download);
										if (CommonUtil.isUpdateAvailable(getApplicationContext())) {
											//update is mandatory, go to Settings screen to manually update apk
											DebugLog.d("-- terdapat versi aplikasi terbaru --");
											hideDialog();
											Toast.makeText(this, getString(R.string.newUpdateSTPapplication), Toast.LENGTH_LONG).show();
											startActivity(new Intent(this, SettingActivity.class));

										} else {
											DebugLog.d("-- tidak ada pembaruan aplikasi --");

											// lakukan cek dan unduh form ketika belum update apk
											checkWorkFormVersion();
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

	private void checkWorkFormVersion() {
		DebugLog.d("--> CHECK WORK FORM VERSION");
		showMessageDialog(getString(R.string.check_form_version));
		compositeDisposable.add(
				TowerAPIHelper.getFormVersion()
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(
								workFormVersionResponse -> {
									if (workFormVersionResponse != null) {
										String latestVersion = workFormVersionResponse.version;
										String currentVersion = getPreference(Constants.KEY_LATEST_FORM_VERSION, null);

										if (TextUtils.isEmpty(latestVersion)) {
											hideDialog();
											String message = "Data form version terakhir dari server tidak ada. Mohon laporan masalah";
											Toast.makeText(this, message, Toast.LENGTH_LONG).show();
											return;
										}

										if (TextUtils.isEmpty(currentVersion) || !latestVersion.equalsIgnoreCase(currentVersion)) {// download form
											DebugLog.d("-- terdapat versi form terbaru --");
											Prefs.putString(Constants.KEY_LATEST_FORM_VERSION, latestVersion);
											downloadWorkForms();
										} else {
											DebugLog.d("-- tidak ada pembaruan form --");
											downloadWorkSchedules();
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
														String response = message.getData().getString("response");
														if (TextUtils.isEmpty(response) || response.equals("failed")) {
															Toast.makeText(this, getString(R.string.error_failed_save_forms), Toast.LENGTH_LONG).show();
															DebugLog.d("-- " + getString(R.string.error_failed_save_forms) + " --");
														}

														// todo: tambahkan shared pref put form version
														downloadWorkSchedules();
														return true;
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