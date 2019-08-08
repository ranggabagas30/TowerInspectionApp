package com.sap.inspection;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rindang.zconfig.AppConfig;
import com.sap.inspection.connection.APIHelper;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.constant.GlobalVar;
import com.sap.inspection.manager.AlertDialogManager;
import com.sap.inspection.model.DbManager;
import com.sap.inspection.model.LoginLogModel;
import com.sap.inspection.model.UserModel;
import com.sap.inspection.model.form.ColumnModel;
import com.sap.inspection.model.form.RowModel;
import com.sap.inspection.model.form.WorkFormGroupModel;
import com.sap.inspection.model.form.WorkFormModel;
import com.sap.inspection.model.responsemodel.FormResponseModel;
import com.sap.inspection.model.responsemodel.UserResponseModel;
import com.sap.inspection.model.value.DbManagerValue;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.CommonUtil;
import com.sap.inspection.util.FileUtil;
import com.sap.inspection.util.PermissionUtil;
import com.sap.inspection.util.StringUtil;
import com.sap.inspection.view.dialog.DialogUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class LoginActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks {

	//skipper
	private Class jumto = MainActivity.class;
	private boolean isJump = false;
	private AlertDialogManager alert = new AlertDialogManager();

	private ImageView imagelogo;
	private Button login;
	private Button copy;
	private EditText username;
	private EditText password;
	private TextView version;
	private CheckBox cbKeep;
	private LoginLogModel loginLogModel;

	//camera properties
	//private CameraPreview preview;
	private Button buttonClick;
	private String fileName;

	// for update things
	Button update;
	SharedPreferences prefs;
	File tempFile;

	private ProgressDialog pDialog;
	
	//development
	private View developmentLayout;
	private Button change;
	private EditText endpoint;

	// Progress dialog type (0 - for Horizontal progress bar)
	public static final int progress_bar_type = 0;

	/** File url to download **/
	private static String file_url;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		DialogUtil.networkPermissionDialog(this);

		developmentLayout = findViewById(R.id.devLayout);
		developmentLayout.setVisibility(AppConfig.getInstance().config.isProduction() ? View.GONE : View.VISIBLE);
		imagelogo = findViewById(R.id.imagelogo);
		imagelogo.setVisibility(View.VISIBLE);
		endpoint = findViewById(R.id.endPoint);
		endpoint.setVisibility(AppConfig.getInstance().config.isProduction() ? View.GONE : View.VISIBLE);
		endpoint.setText(AppConfig.getInstance().config.getHost());
		change = findViewById(R.id.change);
		change.setVisibility(AppConfig.getInstance().config.isProduction() ? View.GONE : View.VISIBLE);
		change.setOnClickListener(v -> {
			Toast.makeText(activity, "Endpoint diganti", Toast.LENGTH_SHORT).show();
			AppConfig.getInstance().config.setHost(endpoint.getText().toString());
		});
		copy = findViewById(R.id.copy);
		copy.setVisibility(AppConfig.getInstance().config.isProduction() ? View.GONE : View.VISIBLE);
		copy.setOnClickListener(v -> {
			FileUtil.copyDB(this, getPreference(R.string.user_id, "")+"_"+DbManagerValue.dbName, "value.db");
			FileUtil.copyDB(this, getPreference(R.string.user_id, "")+"_"+DbManager.dbName,"general.db");
		});

		writePreference("cook", "value");
		login = findViewById(R.id.login);
		login.setOnClickListener(onBtnLoginClickListener);
		username = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);
		version =  (TextView) findViewById(R.id.app_version);
		version.setText(StringUtil.getVersionName(this));
		cbKeep = (CheckBox)findViewById(R.id.cbkeep);
		cbKeep.setOnCheckedChangeListener((compoundButton, b) -> DebugLog.d("checked="+b));

		if (getPreference(R.string.user_name, null) != null)
			username.setText(getPreference(R.string.user_name, null));

		getVersionCheckingUpdate();

		tempFile= Environment.getExternalStorageDirectory();
		tempFile=new File(tempFile.getAbsolutePath()+"/Download/sapInspection"+prefs.getString(LoginActivity.this.getString(R.string.latest_version), "")+".apk");

		update.setOnClickListener(v -> {

			//Check if the file already downloaded before
			trackEvent("user_update_apk");
			if(!tempFile.exists()){
				trackEvent("user_download_apk");
				new DownloadFileFromURL().execute(file_url);
			}
			else{
				trackEvent("user_install_apk");
				Intent intent = new Intent(Intent.ACTION_VIEW)
						.setDataAndType(Uri.fromFile(tempFile),"application/vnd.android.package-archive");
				intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		});

		trackThisPage("Login");
	}

	/** Ensure that Location GPS and Location Networkf had been enabled when app is resumed **/
	@Override
	protected void onResume() {
		super.onResume();
		if (!CommonUtil.checkGpsStatus(this) && !CommonUtil.checkNetworkStatus(this)) {
			DialogUtil.gpsDialog(this).show();
		} else {
			getLoginSessionFromPreference();
		}
	}

	/** Get login session from preference **/
	private void getLoginSessionFromPreference() {
		if (getPreference(R.string.keep_login,false) && !getPreference(R.string.user_authToken,"").isEmpty()) {
			Intent intent = new Intent(this, jumto);
			if (getIntent().getBooleanExtra(Constants.LOADSCHEDULE,false))
				intent.putExtra(Constants.LOADSCHEDULE,true);
			startActivity(intent);
			finish();
		}
	}

	/** Get version, checking update, and triggering update if need update **/
	private void getVersionCheckingUpdate() {
		String version = null;
		int versionCode = 0;
		try {
			version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CommonUtil.fixVersion(getApplicationContext());
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		update = findViewById(R.id.update);
		update.setVisibility(View.GONE);
		DebugLog.d("version Name = " + version+" versionCode = "+versionCode);
		DebugLog.d("pref version Name = " + getPreference(R.string.latest_version,""));
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	private OnClickListener onBtnLoginClickListener = v -> {

		switch (v.getId()) {
		case R.id.login:
			requestAllPermissions();
			break;
		default:
			break;
		}
	};

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
				}
				else
					checkLoginState(false);

				DebugLog.d("userReponseModel.status = " + userResponseModel.status);
			}else{

				hideDialog();
				Toast.makeText(activity, R.string.failed_network_connection_problem, Toast.LENGTH_SHORT).show();
			}
		}
	};

	private void checkLoginState(boolean canLogin){
		if (canLogin){
			Intent intent = new Intent(LoginActivity.this, MainActivity.class);
			intent.putExtra(Constants.LOADAFTERLOGIN,true);
			startActivityForResult(intent, Constants.DEFAULT_REQUEST_CODE);
			finish();
		}
		else{
			loginLogModel.statusLogin = "failed";
			Toast.makeText(LoginActivity.this, R.string.failed_pasword_doesnt_match, Toast.LENGTH_SHORT).show();
		}
	}

	private boolean offlineLogin(String username, String password){
		return getPreference(R.string.user_name, null) != null 
				&& getPreference(R.string.password, null) != null 
				&& getPreference(R.string.password, null).equals(password)
				&& getPreference(R.string.user_name, null).equals(username);
	}

	private void onlineLogin(UserModel userModel){
		showMessageDialog("Masuk ke server, silahkan tunggu");
		APIHelper.login(activity, loginHandler, userModel.username, userModel.password);
	}

	private class FormSaver extends AsyncTask<Object, Integer, Void>{
		@Override
		protected Void doInBackground(Object... params) {
			int sum = 0;
			for (int i = 0; i < params.length; i++) {
				if (((WorkFormModel)params[i]).groups != null)
					for (WorkFormGroupModel group : ((WorkFormModel)params[i]).groups) {
						sum += group.table.headers.size();
						sum += group.table.rows.size();
					}
			}

			int curr = 0;
			for (int i = 0; i < params.length; i++) {
				((WorkFormModel)params[i]).save();
				if (((WorkFormModel)params[i]).groups != null)
					for (WorkFormGroupModel group : ((WorkFormModel)params[i]).groups) {
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
			showMessageDialog("menyimpan forms "+values[0]+" %...");

		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			hideDialog();
		}
	}

	/**
	 * Background Async Task to download file
	 * */
	class DownloadFileFromURL extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread
		 * Show Progress Bar Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(activity, ProgressDialog.STYLE_SPINNER);
			pDialog.show();
		}

		/**
		 * Downloading file in background thread
		 * */
		@Override
		protected String doInBackground(String... f_url) {
			int count;
			try {
				URL url = new URL(f_url[0]);
				URLConnection conection = url.openConnection();
				conection.connect();

				// this will be useful so that you can show a tipical 0-100% progress bar
				int lenghtOfFile = conection.getContentLength();

				// download the file
				InputStream input = new BufferedInputStream(url.openStream(), 8192);

				File tempDir= Environment.getExternalStorageDirectory();
				tempDir=new File(tempDir.getAbsolutePath()+"/Download");
				if(!tempDir.exists())
				{
					tempDir.mkdir();
				}

				// Output stream
				OutputStream output = new FileOutputStream(tempDir.getAbsolutePath()+"/sapInspection"+prefs.getString(LoginActivity.this.getString(R.string.latest_version), "")+".apk");

				byte data[] = new byte[1024];

				long total = 0;

				while ((count = input.read(data)) != -1) {
					total += count;
					// publishing the progress....
					// After this onProgressUpdate will be called
					publishProgress(""+(int)((total*100)/lenghtOfFile));

					// writing data to file
					output.write(data, 0, count);
				}

				// flushing output
				output.flush();

				// closing streams
				output.close();
				input.close();

			} catch (Exception e) {
				DebugLog.e(e.getMessage());
			}

			return null;
		}

		/**
		 * Updating progress bar
		 * */
		protected void onProgressUpdate(String... progress) {
			// setting progress percentage
			pDialog.setProgress(Integer.parseInt(progress[0]));
		}

		/**
		 * After completing background task
		 * Dismiss the progress dialog
		 * **/

		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(String file_url) {

			pDialog.dismiss();

			File tempFile;
			if (CommonUtil.isExternalStorageAvailable()) {
				DebugLog.d("external storage available");
				tempFile = Environment.getExternalStorageDirectory();
			} else {
				DebugLog.d("external storage not available");
				tempFile = getFilesDir();
			}
			tempFile = new File(tempFile.getAbsolutePath() + "/Download/sapInspection" + prefs.getString(LoginActivity.this.getString(R.string.latest_version), "") + ".apk");
			if (tempFile.exists()) {
				Intent intent = new Intent(Intent.ACTION_VIEW)
						.setDataAndType(Uri.fromFile(tempFile), "application/vnd.android.package-archive");
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			} else {
				MyApplication.getInstance().toast(getResources().getString(R.string.failed_apknotfound), Toast.LENGTH_LONG);
				finish();
			}
		}
	}

	/**
	 * Permission
	 *
	 * */

	@AfterPermissionGranted(Constants.RC_ALL_PERMISSION)
	private void requestAllPermissions() {

		if (PermissionUtil.hasAllPermissions(this)) {

			// login directly
			doLogin();
		} else {

			// Do not have permissions, request them now
			DebugLog.d("Do not have permissions, request them now");
			PermissionUtil.requestAllPermissions(this, getString(R.string.rationale_allpermissions), Constants.RC_ALL_PERMISSION);

		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		DebugLog.d("request permission result");

		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (requestCode == Constants.RC_ALL_PERMISSION) {

			if (PermissionUtil.hasAllPermissions(this)) {

				// proceed login
				DebugLog.d("all permissions are allowed, proceed login");
				doLogin();

			} else requestAllPermissions(); // ask permission again
		}

	}

	@Override
	public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

		boolean hasAllPermissionsGranted = true;
		DebugLog.d("permission granted");

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

	private void doLogin() {

		trackEvent("user_login");
		UserModel userModel = new UserModel();
		userModel.username = username.getText().toString();
		userModel.password = password.getText().toString();

		//adding data for login log model
		if (loginLogModel == null)
			loginLogModel = new LoginLogModel();

		loginLogModel.id = String.valueOf(System.currentTimeMillis());
		loginLogModel.userName = username.getText().toString();

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		loginLogModel.time = simpleDateFormat.format(new Date());
		loginLogModel.fileName = loginLogModel.time + " " + loginLogModel.userName;

		setFileName(loginLogModel.fileName);

		if (GlobalVar.getInstance().anyNetwork(this)){
			DebugLog.d("any network");
			onlineLogin(userModel);
		}else{
			DebugLog.d("no network");
			hideDialog();
			Toast.makeText(activity, R.string.failed_network_connection_problem, Toast.LENGTH_SHORT).show();
		}
	}
}