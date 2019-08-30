package com.sap.inspection.view.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

import com.google.gson.Gson;
import com.rindang.zconfig.AppConfig;
import com.sap.inspection.R;
import com.sap.inspection.connection.APIHelper;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.constant.GlobalVar;
import com.sap.inspection.model.DbManager;
import com.sap.inspection.model.LoginLogModel;
import com.sap.inspection.model.UserModel;
import com.sap.inspection.model.responsemodel.UserResponseModel;
import com.sap.inspection.model.value.DbManagerValue;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.CommonUtil;
import com.sap.inspection.util.FileUtil;
import com.sap.inspection.util.PermissionUtil;
import com.sap.inspection.util.StringUtil;
import com.sap.inspection.util.DialogUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class LoginActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks {

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
		if (!CommonUtil.isUpdateAvailable(this)) update.setVisibility(View.GONE);

		if (!PermissionUtil.hasAllPermissions(this))
			requestAllPermissions();
	}

	/** Ensure that Location GPS and Location Network had been enabled when app is resumed **/
	@Override
	protected void onResume() {
		super.onResume();
		if (!CommonUtil.checkGpsStatus(this) && !CommonUtil.checkNetworkStatus(this)) {
			DialogUtil.showGPSdialog(this);
		} else {
			getLoginSessionFromPreference();
		}
	}

	/** Get login session from preference **/
	private void getLoginSessionFromPreference() {
		if (getPreference(R.string.keep_login,false) && !getPreference(R.string.user_authToken,"").isEmpty()) {
			Intent intent = new Intent(this, MainActivity.class);
			if (getIntent().getBooleanExtra(Constants.LOADSCHEDULE,false))
				intent.putExtra(Constants.LOADSCHEDULE,true);
			startActivity(intent);
			finish();
		}
	}

	/** Get version, checking update, and triggering update if need update **/
	@SuppressLint("HandlerLeak")
	Handler loginHandler = new Handler(){

		public void handleMessage(android.os.Message msg) {

			Bundle bundle = msg.getData();
			Gson gson = new Gson();

			if (bundle.getString("json") != null){
				hideDialog();
				UserResponseModel userResponseModel = gson.fromJson(bundle.getString("json"), UserResponseModel.class);
				if (userResponseModel.status == 201){
					writePreference(R.string.user_name, username.getText().toString());
					writePreference(R.string.password, password.getText().toString());
					writePreference(R.string.user_fullname, userResponseModel.data.full_name);
					writePreference(R.string.user_id, userResponseModel.data.id);
					writePreference(R.string.user_authToken, userResponseModel.data.persistence_token);
					writePreference(R.string.keep_login,cbKeep.isChecked());
					checkLoginState(true);
				} else
					checkLoginState(false);

				DebugLog.d("userReponseModel.status = " + userResponseModel.status);
			}else{
				DebugLog.e(getString(R.string.failed_network_connection_problem));
				hideDialog();
				Toast.makeText(activity, R.string.failed_network_connection_problem, Toast.LENGTH_SHORT).show();
			}
		}
	};

	private void checkLoginState(boolean canLogin) {
		if (canLogin) {
			Intent intent = new Intent(LoginActivity.this, MainActivity.class);
			intent.putExtra(Constants.LOADAFTERLOGIN, true);
			startActivityForResult(intent, Constants.DEFAULT_REQUEST_CODE);
			finish();
		} else {
			loginLogModel.statusLogin = "failed";
			Toast.makeText(LoginActivity.this, R.string.failed_pasword_doesnt_match, Toast.LENGTH_SHORT).show();
		}
	}

	private void onlineLogin(UserModel userModel){
		showMessageDialog("Masuk ke server, silahkan tunggu");
		APIHelper.login(activity, loginHandler, userModel.username, userModel.password);
	}

	/**
	 * Permission
	 * */
	@AfterPermissionGranted(Constants.RC_ALL_PERMISSION)
	private void requestAllPermissions() {
		// Do not have permissions, request them now
		DebugLog.d("Do not have permissions, request them now");
		PermissionUtil.requestAllPermissions(this, getString(R.string.rationale_allpermissions), Constants.RC_ALL_PERMISSION);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		DebugLog.d("request permission result");
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (requestCode == Constants.RC_ALL_PERMISSION) {
			if (PermissionUtil.hasAllPermissions(this)) {
				permissionGranted();
			} else requestAllPermissions(); // ask permission again
		}
	}

	@Override
	public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
		permissionGranted();
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

	private void permissionGranted() {
		switch (ACTION) {
			case LOGIN: login(); break;
			case UPDATE: updateAPK(); break;
			case COPYDB: copyDB(); break;
			case NONE: DebugLog.d("permission granted");
				Toast.makeText(LoginActivity.this, this.getString(R.string.success_permissions_granted), Toast.LENGTH_SHORT).show(); break;
		}
	}

	private void login() {
		DebugLog.d("user_login");
		trackEvent("user_login");
		UserModel userModel = new UserModel();
		userModel.username = username.getText().toString();
		userModel.password = password.getText().toString();

		if (TextUtils.isEmpty(userModel.username) || TextUtils.isEmpty(userModel.password)) {
			Toast.makeText(activity, getString(R.string.failed_login_field_blanked), Toast.LENGTH_SHORT).show();
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
			onlineLogin(userModel);
		}else{
			DebugLog.e(getString(R.string.failed_network_connection_problem));
			hideDialog();
			Toast.makeText(activity, R.string.failed_network_connection_problem, Toast.LENGTH_SHORT).show();
		}
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
		Toast.makeText(activity, "Endpoint diganti", Toast.LENGTH_SHORT).show();
		AppConfig.getInstance().config.setHost(endpoint.getText().toString());
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