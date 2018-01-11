package com.sap.inspection;

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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rindang.pushnotification.RegisterGCM;
import com.rindang.zconfig.AppConfig;
import com.sap.inspection.connection.APIHelper;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.constant.GlobalVar;
import com.sap.inspection.manager.AlertDialogManager;
import com.sap.inspection.model.DbManager;
import com.sap.inspection.model.DbRepository;
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
import com.sap.inspection.util.Utility;
import com.slidinglayer.util.CommonUtils;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LoginActivity extends BaseActivity {

	//skipper
	private Class jumto = MainActivity.class;
	private boolean isJump = false;
	ProgressDialog progressDialog;
	AlertDialogManager alert = new AlertDialogManager();

	private Button submit;
	private EditText username;
	private EditText password;
	private TextView version;
	private CheckBox cbKeep;
	public static final String SENDER_ID = "494949404342";
	private LoginLogModel loginLogModel;

	//camera properties
	//	private CameraPreview preview;
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
	private LovelyStandardDialog gpsDialog;

	// Progress dialog type (0 - for Horizontal progress bar)
	public static final int progress_bar_type = 0; 

	// File url to download
	private static String file_url = "http://api.androidhive.info/progressdialog/hive.jpg";
	
	private void copyDB(String dbname,String dstName){
	    try {
	        File sd = Environment.getExternalStorageDirectory();
	        File data = Environment.getDataDirectory();
	        if (sd.canWrite()) {
	            String currentDBPath = "//data//"+ BuildConfig.APPLICATION_ID+ "//databases//"+dbname;
	            String backupDBPath = dstName;
	            File currentDB = new File(data, currentDBPath);
	            File backupDB = new File(sd, backupDBPath);
				DebugLog.d("external dir : "+backupDB.getPath());
				DebugLog.d("database path : "+currentDB.getPath());

	            if (currentDB.exists()) {
	                FileChannel src = new FileInputStream(currentDB).getChannel();
	                FileChannel dst = new FileOutputStream(backupDB).getChannel();
	                dst.transferFrom(src, 0, src.size());
	                src.close();
	                dst.close();
	            }
	        }
			//string copy database sukses
			Toast.makeText(activity, getString(R.string.copydatabasesuccess), Toast.LENGTH_SHORT).show();

	    } catch (Exception e) {
			DebugLog.e(e.getMessage());
			DebugLog.e(e.getCause().getMessage());
			//string copy database gagal
			Toast.makeText(activity, getString(R.string.copydatabasefailed), Toast.LENGTH_SHORT).show();
	    }
	}
	
	private void copyDB2(String dbname,String dstName){
	    try {
//	        File sd = Environment.getExternalStorageDirectory();
//	        File data = Environment.getDataDirectory();
	    	File sd = Environment.getExternalStorageDirectory();
	        File data = Environment.getDataDirectory();
	        if (sd.canWrite()) {
	            String currentDBPath = "//data//"+ BuildConfig.APPLICATION_ID+ "//databases//"+dbname;
	            String backupDBPath = dstName;
	            File currentDB = new File(data, currentDBPath);
	            File backupDB = new File(sd, backupDBPath);
				DebugLog.d("external dir : "+backupDB.getPath());
				DebugLog.d("database path : "+currentDB.getPath());

	            if (currentDB.exists()) {
	                FileChannel src = new FileInputStream(currentDB).getChannel();
	                FileChannel dst = new FileOutputStream(backupDB).getChannel();
	                dst.transferFrom(src, 0, src.size());
	                src.close();
	                dst.close();
	            }
	        }
	    } catch (Exception e) {
			DebugLog.e(e.getMessage());
			DebugLog.e(e.getCause().getMessage());
	    }
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!Utility.checkGpsStatus(this) && !Utility.checkNetworkStatus(this)) {
			gpsDialog.show();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DebugLog.d("");
		trackThisPage("Login");

		gpsDialog = new LovelyStandardDialog(this,R.style.CheckBoxTintTheme)
				.setTopColor(color(R.color.theme_color))
				.setButtonsColor(color(R.color.theme_color))
				.setIcon(R.drawable.logo_app)
				//string title information GPS
				.setTitle(getString(R.string.informationGPS))
				.setMessage("Silahkan aktifkan GPS")
				.setCancelable(false)
				.setPositiveButton(android.R.string.yes, new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent gpsOptionsIntent = new Intent(
								Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						startActivity(gpsOptionsIntent);
						finish();
					}
				});
		/*
		if (!GlobalVar.getInstance().anyNetwork(this)){
			new LovelyStandardDialog(this,R.style.CheckBoxTintTheme)
					.setTopColor(color(R.color.theme_color))
					.setButtonsColor(color(R.color.theme_color))
					.setIcon(R.drawable.logo_app)
					.setTitle("Information")
					.setMessage("No internet connection. Please connect your network.")
					.setCancelable(false)
					.setPositiveButton(android.R.string.yes, new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
							finish();
						}
					})
					.show();
			return;
		}*/

		if (getPreference(R.string.keep_login,false) && !getPreference(R.string.user_authToken,"").isEmpty()) {
			Intent intent = new Intent(this, jumto);
			if (getIntent().getBooleanExtra(Constants.LOADSCHEDULE,false))
				intent.putExtra(Constants.LOADSCHEDULE,true);
			startActivity(intent);
			finish();
			return;
		}

		progressDialog = new ProgressDialog(activity);
		setContentView(R.layout.activity_login);
		developmentLayout = findViewById(R.id.devLayout);
		developmentLayout.setVisibility(AppConfig.getInstance().config.isProduction() ? View.GONE : View.VISIBLE);
		
		endpoint = (EditText) findViewById(R.id.endPoint);
		endpoint.setText(AppConfig.getInstance().config.getHost());
		change = (Button) findViewById(R.id.change);
		change.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Toast.makeText(activity, "Endpoint diganti!!!", Toast.LENGTH_SHORT).show();
				AppConfig.getInstance().config.setHost(endpoint.getText().toString());
			}
		});
		
		Button copy = (Button) findViewById(	R.id.copy);
		copy.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				copyDB(getPreference(R.string.user_id, "")+"_"+DbManagerValue.dbName, "value.db");
				copyDB(getPreference(R.string.user_id, "")+"_"+DbManager.dbName,"general.db");
			}
		});

		if (isJump){
			Intent intent = new Intent(this, jumto);
			startActivity(intent);
			finish();
		}
		
		writePreference("cook", "value");
		submit = (Button) findViewById(R.id.submit);
		submit.setOnClickListener(onClickListener);
		username = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);
		version =  (TextView) findViewById(R.id.app_version);
		version.setText(getVersionName());

		DebugLog.d("tster" + getVersionName());

		cbKeep = (CheckBox)findViewById(R.id.cbkeep);
		cbKeep.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				DebugLog.d("checked="+b);
			}
		});
		
		if (getPreference(R.string.user_name, null) != null)
			username.setText(getPreference(R.string.user_name, null));
//		if (getPreference(R.string.password, null) != null)
//			password.setText(getPreference(R.string.password, null).substring(0, getPreference(R.string.password, null).length() - 2));

		//update things
		String version = null;
		int versionCode = 0;
		try {
			version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CommonUtils.fixVersion(getApplicationContext());
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
//		file_url = prefs.getString(this.getString(R.string.url_update), "");
		update = (Button) findViewById(R.id.update);
		DebugLog.d("version Name = " + version+" versionCode = "+versionCode);
		DebugLog.d("pref version Name = " + getPreference(R.string.latest_version,""));
//		if (version != null && (version.equalsIgnoreCase(prefs.getString(this.getString(R.string.latest_version), ""))/* || prefs.getString(this.getString(R.string.url_update), "").equalsIgnoreCase("")*/)){
		if (!CommonUtils.isUpdateAvailable(getApplicationContext())) {
			update.setVisibility(View.GONE);
		}else{
			update.setVisibility(View.VISIBLE);
		}

		tempFile= Environment.getExternalStorageDirectory();
		tempFile=new File(tempFile.getAbsolutePath()+"/Download/sapInspection"+prefs.getString(LoginActivity.this.getString(R.string.latest_version), "")+".apk");

		update.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				//Chek if the file already downloaded before
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
			}
		});

//test crash for crashlytics
//		throw new RuntimeException("This is a crash");
	}

	private void initForm(){
		WorkFormModel form = new WorkFormModel();
		DbRepository.getInstance().open(this);
		if (form.countItem() != 0){
			DbRepository.getInstance().close();
			return;
		}
		byte[] buffer = null;
		InputStream is;
		try {
			is = this.getAssets().open("work_forms_full.txt");
			int size = is.available();
			buffer = new byte[size];
			is.read(buffer);
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String bufferString = new String(buffer);
		progressDialog.setMessage("initialize default form template");
		progressDialog.setCancelable(false);
		progressDialog.show();
		Gson gson = new Gson();
		FormResponseModel formResponseModel = gson.fromJson(bufferString,FormResponseModel.class);
		if (formResponseModel.status == 200){
			FormSaver formSaver = new FormSaver();
			formSaver.execute(formResponseModel.data.toArray());
		}
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	private OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
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
			//			preview.camera.takePicture(shutterCallback, rawCallback,jpegCallback);

			switch (v.getId()) {
			case R.id.submit:
				if (GlobalVar.getInstance().anyNetwork(activity)){
					onlineLogin(userModel);
					DebugLog.d("any network");
				}else{
					DebugLog.d("no network");
					if (progressDialog != null && progressDialog.isShowing())
						progressDialog.dismiss();
					Toast.makeText(activity, R.string.network_connection_problem, Toast.LENGTH_SHORT).show();
//					checkLoginState(offlineLogin(userModel.username, userModel.password));
				}
				break;

			default:
				break;
			}

//			throw new RuntimeException("This is a test crash");
		}
	};

	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
		}
	};

	/** Handles data for raw picture */
	PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
		}
	};

	/** Handles data for jpeg picture */
	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			FileOutputStream outStream = null;
			try {
				String root = Environment.getExternalStorageDirectory().toString();
				outStream = new FileOutputStream(String.format(
						root+"/%s.jpg", fileName));

				//				outStream.write(data);
				//				outStream.close();

				Bitmap bitmap = BitmapFactory.decodeByteArray(data , 0, data.length);
				bitmap.compress(CompressFormat.JPEG, 75, outStream);

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

		}
	};

	Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			Bundle bundle = msg.getData();
			Gson gson = new Gson();
			if (bundle.getString("json") != null){
				UserResponseModel userResponseModel = gson.fromJson(bundle.getString("json"), UserResponseModel.class);
				if (userResponseModel.status == 201){
					writePreference(R.string.user_name, username.getText().toString());
					writePreference(R.string.password, password.getText().toString());
					writePreference(R.string.user_fullname, userResponseModel.data.full_name);
					writePreference(R.string.user_id, userResponseModel.data.id);
					writePreference(R.string.user_authToken, userResponseModel.data.persistence_token);
					writePreference(R.string.keep_login,cbKeep.isChecked());
					if (progressDialog != null && progressDialog.isShowing())
						progressDialog.dismiss();
					checkLoginState(true);
				}
				else
					checkLoginState(false);
			}else{
				if (progressDialog != null && progressDialog.isShowing())
					progressDialog.dismiss();
				Toast.makeText(activity, R.string.network_connection_problem, Toast.LENGTH_SHORT).show();
			}
		}
	};

	private void checkLoginState(boolean canLogin){
		if (canLogin){
			RegisterGCM register = new RegisterGCM(new Handler());
			register.execute();
			Intent intent = new Intent(LoginActivity.this, MainActivity.class);
			intent.putExtra(Constants.LOADAFTERLOGIN,true);
			startActivityForResult(intent, Constants.DEFAULT_REQUEST_CODE);
			finish();
		}
		else{
			loginLogModel.statusLogin = "failed";
			Toast.makeText(LoginActivity.this, R.string.pasword_doesnt_match, Toast.LENGTH_SHORT).show();
			progressDialog.dismiss();
		}
	}

	private boolean offlineLogin(String username, String password){
		return getPreference(R.string.user_name, null) != null 
				&& getPreference(R.string.password, null) != null 
				&& getPreference(R.string.password, null).equals(password)
				&& getPreference(R.string.user_name, null).equals(username);
	}

	private void onlineLogin(UserModel userModel){
		progressDialog.setMessage("Masuk ke server, silakan tunggu");
		progressDialog.show();
		APIHelper.login(activity, handler, userModel.username, userModel.password);
	}

	//	private void takePicture(String fileName){
	//		if (preview.camera != null){
	//			if (loginLogModel == null)
	//				loginLogModel = new LoginLogModel();
	//			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	//			String currentDateandTime = simpleDateFormat.format(new Date());
	//			setFileName(fileName + " " +currentDateandTime);
	//			preview.camera.takePicture(shutterCallback, rawCallback,jpegCallback);
	//		}
	//	}

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
			progressDialog.setMessage("menyimpan forms "+values[0]+" %...");
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			DbRepository.getInstance().close();
			progressDialog.dismiss();
		}
	}

	Handler handler2 = new Handler();

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
//			ProgressDialog progressDialog = new ProgressDialog(activity);
//			progressDialog.
//			showDialog(progress_bar_type);
			//
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
				OutputStream output = new FileOutputStream(tempDir.getAbsolutePath()+"/sapInspection"+prefs.getString(LoginActivity.this.getString(R.string.latest_version), "")+".apk.temp");

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
			
//			dismissDialog(progress_bar_type);
			pDialog.dismiss();

			File tempFile= Environment.getExternalStorageDirectory();
			tempFile=new File(tempFile.getAbsolutePath()+"/Download/sapInspection"+prefs.getString(LoginActivity.this.getString(R.string.latest_version), "")+".apk.temp");
			if(!tempFile.exists())
			{
				finish();
			}

			File  existing = new File(tempFile.getAbsolutePath()+"/Download/sapInspection"+prefs.getString(LoginActivity.this.getString(R.string.latest_version), "")+".apk");
			tempFile.renameTo(existing);
			
			Intent intent = new Intent(Intent.ACTION_VIEW)
			.setDataAndType(Uri.fromFile(existing),"application/vnd.android.package-archive");
			intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);  
		}
	}
	
	private String getVersionName(){
		String version = null;
		try {
			version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			DebugLog.d(version);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return version;
	}

}