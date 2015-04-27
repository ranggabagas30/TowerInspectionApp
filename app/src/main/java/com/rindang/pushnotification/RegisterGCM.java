package com.rindang.pushnotification;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.rindang.zconfig.AppConfig;
import com.sap.inspection.MyApplication;
import com.sap.inspection.R;
import com.sap.inspection.connection.APIHelper;
import com.sap.inspection.util.PrefUtil;

public class RegisterGCM extends AsyncTask<Void, Void, String>{

	private GoogleCloudMessaging gcm;
	private Handler myHandler;

	public RegisterGCM(Handler handler) {
		myHandler = handler;
	}

	@Override
	protected String doInBackground(Void... params) {
		Log.d(RegisterGCM.class.getSimpleName(), "Trying get registration ID for GCM");
		try {
			if(gcm == null){
				gcm = GoogleCloudMessaging.getInstance(MyApplication.getInstance());
			}
			String registrationID = gcm.register(AppConfig.getInstance().config.getGCMSenderId());
			Log.d(RegisterGCM.class.getSimpleName(), "RegistrationID: " + registrationID);
			PrefUtil.putStringPref(R.string.app_reg_id, registrationID);
			return registrationID;
		} catch (Exception e) {
			Log.d(RegisterGCM.class.getSimpleName(), "Exception: " + e.getMessage());
			return null;
		}
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		Log.d(RegisterGCM.class.getSimpleName(), "result in gcm : " + result);
		try {
			if (!PrefUtil.getStringPref(R.string.user_authToken, "").equalsIgnoreCase("")){
//				APIHelper.registerGCMToken(MyApplication.getInstance(), myHandler,  result);
			}
		} catch (UnsupportedOperationException e){
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	private int getAppVersionCode(){
		PackageInfo pInfo = null;
		try {
			pInfo = MyApplication.getInstance().getPackageManager().getPackageInfo(MyApplication.getInstance().getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return pInfo.versionCode;
	}

}