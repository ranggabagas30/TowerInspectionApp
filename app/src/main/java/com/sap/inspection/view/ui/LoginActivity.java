package com.sap.inspection.view.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.pixplicity.easyprefs.library.Prefs;
import com.rindang.zconfig.AppConfig;
import com.sap.inspection.R;
import com.sap.inspection.connection.rest.TowerAPIHelper;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.constant.GlobalVar;
import com.sap.inspection.model.DbManager;
import com.sap.inspection.model.LoginLogModel;
import com.sap.inspection.model.UserModel;
import com.sap.inspection.model.value.DbManagerValue;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.CommonUtil;
import com.sap.inspection.util.DialogUtil;
import com.sap.inspection.util.FileUtil;
import com.sap.inspection.util.NetworkUtil;
import com.sap.inspection.util.PermissionUtil;
import com.sap.inspection.util.StringUtil;

import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class LoginActivity extends BaseActivity implements EasyPermissions.RationaleCallbacks {

	//skipper
	private ImageView imagelogo;
	private Button login;
	private Button copy;
	private Button update;
	private Button change;
	private EditText username;
	private EditText password;
	private EditText endpoint;
	private TextView version;
	private CheckBox cbKeep;
	private LoginLogModel loginLogModel;
	private View developmentLayout;
	private ACTION_AFTER_GRANTED ACTION = ACTION_AFTER_GRANTED.NONE;

	private enum ACTION_AFTER_GRANTED {
		NONE,
		LOGIN,
		KEEP_LOGIN,
		UPDATE,
		COPYDB
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		DialogUtil.showEnableNetworkDialog(this);

		developmentLayout = findViewById(R.id.devLayout);
		imagelogo = findViewById(R.id.imagelogo);
		endpoint = findViewById(R.id.endPoint);
		change = findViewById(R.id.change);
		copy = findViewById(R.id.copy);
		login = findViewById(R.id.login);
		username = findViewById(R.id.username);
		password = findViewById(R.id.password);
		version = findViewById(R.id.app_version);
		cbKeep = findViewById(R.id.cbkeep);
		update = findViewById(R.id.update);

		endpoint.setText(AppConfig.getInstance().config.getHost());
		version.setText(StringUtil.getVersionName(this));
		if (!TextUtils.isEmpty(getPreference(R.string.user_name, null))) {
			username.setText(getPreference(R.string.user_name, null));
		}

		change.setOnClickListener(changeClickListener);
		copy.setOnClickListener(copyClickListener);
		login.setOnClickListener(loginClickListener);
		cbKeep.setOnCheckedChangeListener(cbKeepClickListener);
		update.setOnClickListener(updateClickListener);

		developmentLayout.setVisibility(AppConfig.getInstance().config.isProduction() ? View.GONE : View.VISIBLE);
		imagelogo.setVisibility(View.VISIBLE);
		endpoint.setVisibility(AppConfig.getInstance().config.isProduction() ? View.GONE : View.VISIBLE);
		change.setVisibility(AppConfig.getInstance().config.isProduction() ? View.GONE : View.VISIBLE);
		copy.setVisibility(AppConfig.getInstance().config.isProduction() ? View.GONE : View.VISIBLE);
		if (!CommonUtil.isUpdateAvailable(this))
			update.setVisibility(View.GONE);

		// initialization firebase FCM
		FirebaseInstanceId.getInstance().getInstanceId()
				.addOnSuccessListener(instanceIdResult -> {
					DebugLog.d("FIREBASE INSTANCE ID ; " + instanceIdResult.getId());
					DebugLog.d("FIREBASE TOKEN : " + instanceIdResult.getToken());
					Prefs.putString(getString(R.string.app_fcm_reg_id), instanceIdResult.getToken());
				}).addOnFailureListener(error -> {
					Toast.makeText(this, getString(R.string.error_retrieving_firebase_instance_id), Toast.LENGTH_LONG).show();
					DebugLog.e(error.getMessage(), error);
				});

		checkLoginSession();
	}

	@Override
	protected void onResume() {
		super.onResume();
		/** Ensure that Location GPS and Location Network had been enabled when app is resumed **/
		if (!CommonUtil.checkGpsStatus(this) && !CommonUtil.checkNetworkStatus(this)) {
			DialogUtil.showGPSdialog(this);
		}
	}

	/** Get login session from preference **/
	private void checkLoginSession() {
		if (getPreference(R.string.keep_login,false) && !getPreference(R.string.user_authToken,"").isEmpty()) {
			if (PermissionUtil.hasAllPermissions(this))
				navigateToMainMenu();
			else {
				ACTION = ACTION_AFTER_GRANTED.KEEP_LOGIN;
				requestAllPermissions();
			}
		} else {
			if (!PermissionUtil.hasAllPermissions(this)) {
				ACTION = ACTION_AFTER_GRANTED.NONE;
				requestAllPermissions();
			}
		}
	}

	private void processLogin(UserModel userModel){
		showMessageDialog("Masuk ke server, silahkan tunggu");
		compositeDisposable.add(
				TowerAPIHelper.login(userModel.username, userModel.password)
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(
								loginResponse -> {
									hideDialog();
									try {
										if (loginResponse == null || loginResponse.data == null) {
											throw new NullPointerException("login response is null");
										}

										if (loginResponse.status == HttpURLConnection.HTTP_CREATED) {
											Prefs.putString(getString(R.string.user_name), loginResponse.data.username);
											Prefs.putString(getString(R.string.password), loginResponse.data.password);
											Prefs.putString(getString(R.string.user_fullname), loginResponse.data.full_name);
											Prefs.putString(getString(R.string.user_id), loginResponse.data.id);
											Prefs.putString(getString(R.string.user_authToken), loginResponse.data.persistence_token);
											Prefs.putBoolean(getString(R.string.keep_login), cbKeep.isChecked());
											navigateToMainMenu();
										} else {
											loginLogModel.statusLogin = "failed";
											Toast.makeText(this, loginResponse.messages, Toast.LENGTH_LONG).show();
										}
									} catch (NullPointerException e) {
										DebugLog.e(e.getMessage(), e);
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

	/**
	 * Permission
	 * */
	@AfterPermissionGranted(Constants.RC_ALL_PERMISSION)
	private void requestAllPermissions() {
		PermissionUtil.requestAllPermissions(this, getString(R.string.rationale_allpermissions), Constants.RC_ALL_PERMISSION);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == Constants.RC_ALL_PERMISSION) {
			if (PermissionUtil.hasAllPermissions(this)) {
				permissionGranted();
			} else Toast.makeText(this, getString(R.string.rationale_allpermissions), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onRationaleAccepted(int requestCode) {

	}

	@Override
	public void onRationaleDenied(int requestCode) {
		if (requestCode == Constants.RC_ALL_PERMISSION) {
			Toast.makeText(this, getString(R.string.rationale_allpermissions), Toast.LENGTH_LONG).show();
		}
	}

	private void permissionGranted() {
		switch (ACTION) {
			case LOGIN: login(); break;
			case KEEP_LOGIN: navigateToMainMenu();break;
			case UPDATE: updateAPK(); break;
			case COPYDB: copyDB(); break;
			case NONE: DebugLog.d("permission granted"); break;
		}
	}

	private void login() {
		DebugLog.d("user_login");
		trackEvent("user_login");
		UserModel userModel = new UserModel();
		userModel.username = username.getText().toString();
		userModel.password = password.getText().toString();

		if (TextUtils.isEmpty(userModel.username) || TextUtils.isEmpty(userModel.password)) {
			Toast.makeText(activity, getString(R.string.error_login_field_blanked), Toast.LENGTH_SHORT).show();
			return;
		}

		//adding data for login log model
		if (loginLogModel == null)
			loginLogModel = new LoginLogModel();

		loginLogModel.id = String.valueOf(System.currentTimeMillis());
		loginLogModel.userName = username.getText().toString();

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		loginLogModel.time = simpleDateFormat.format(new Date());
		loginLogModel.fileName = loginLogModel.time + " " + loginLogModel.userName;

		if (GlobalVar.getInstance().anyNetwork(this)){
			processLogin(userModel);
		} else{
			hideDialog();
			Toast.makeText(activity, R.string.error_network_connection_problem, Toast.LENGTH_SHORT).show();
		}
	}

	private void navigateToMainMenu() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}

	private void copyDB() {
		FileUtil.copyDB(this, getPreference(R.string.user_id, "")+"_"+DbManagerValue.dbName, "value.db");
		FileUtil.copyDB(this, getPreference(R.string.user_id, "")+"_"+DbManager.dbName,"general.db");
	}

	/**
	 *
	 * EVENT CLICK LISTENERS
	 *
	 * */
	OnClickListener changeClickListener = view -> {
		AppConfig.getInstance().config.setHost(endpoint.getText().toString());
		Toast.makeText(activity, "Perubahan endpoint ke: " + AppConfig.getInstance().getV1(), Toast.LENGTH_SHORT).show();
	};

	OnClickListener copyClickListener = view -> {
		ACTION = ACTION_AFTER_GRANTED.COPYDB;
		if (!PermissionUtil.hasAllPermissions(this))
			requestAllPermissions();
		else
			copyDB();
	};

	OnClickListener loginClickListener = view -> {
		ACTION = ACTION_AFTER_GRANTED.LOGIN;
		if (!PermissionUtil.hasAllPermissions(this))
			requestAllPermissions();
		else
			login();
	};

	OnClickListener updateClickListener = view -> {
		ACTION = ACTION_AFTER_GRANTED.UPDATE;
		if (!PermissionUtil.hasAllPermissions(this))
			requestAllPermissions();
		else
			updateAPK();
	};

	OnCheckedChangeListener cbKeepClickListener = (compoundButton, b) -> DebugLog.d("checked="+b);

}