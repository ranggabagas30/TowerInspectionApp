package com.sap.inspection;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.facebook.stetho.Stetho;
/*import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;*/
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.rindang.pushnotification.notificationchannel.DefaultNotificationChannel;
import com.sap.inspection.connection.APIHelper;
import com.sap.inspection.listener.ActivityLifecycleHandler;
import com.sap.inspection.model.CheckinDataModel;
import com.sap.inspection.model.DbRepository;
import com.sap.inspection.model.TextMarkDisplayOptionsModel;
import com.sap.inspection.model.TextMarkModel;
import com.sap.inspection.model.responsemodel.DeviceRegisterResponseModel;
import com.sap.inspection.model.value.DbRepositoryValue;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.PrefUtil;
import com.sap.inspection.util.CommonUtil;
import com.scottyab.aescrypt.AESCrypt;

import java.lang.Thread.UncaughtExceptionHandler;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractMap;
import java.util.HashMap;

import javax.crypto.KeyGenerator;

import io.fabric.sdk.android.Fabric;

/**
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class MyApplication extends Application implements ActivityLifecycleHandler.LifecycleListener {

	private UncaughtExceptionHandler defaultUEH;
	private static MyApplication instance;
	private HashMap< String, AbstractMap.SimpleEntry<String, String> > hashMapSiteLocation;
	private boolean IS_CHECKING_HASIL_PM;
	private boolean ON_FORM_IMBAS_PETIR;
	private boolean SCHEDULE_NEED_CHECK_IN;
	private boolean CHECK_APP_VERSION_STATE;
	private boolean DEVICE_REGISTER_STATE;

	public static Key key;

	public CheckinDataModel checkinDataModel;

	public MyApplication() {
		instance = this;
	}

	public static MyApplication getInstance() {
		return instance;
	}

	public static Context getContext() {
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		//1. initialization crashlytics
		Fabric.with(this, new Crashlytics());

		//2. initialization stetho facebook debug
		if (BuildConfig.DEBUG) {
			Stetho.initializeWithDefaults(this);
			AESCrypt.DEBUG_LOG_ENABLED = true;
		}

		//3. initialization image loader configuration
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
				.memoryCacheSize(20 * 1024 * 1024)
				.discCacheSize(104857600)
				.threadPoolSize(10)
				.build();

		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);

		//4. initialization text mark settings for photo item
		TextMarkDisplayOptionsModel textOption = new TextMarkDisplayOptionsModel.Builder(getApplicationContext())
				.setTextColor(Color.WHITE)
				.setTextColorStyle(Paint.Style.FILL)
				.setTextAlign(Paint.Align.LEFT)
				.setTextStyle(Typeface.BOLD)
				.setTextFamilyName("Helvetica")
				.build();

		TextMarkModel.getInstance().init(textOption);

		//5. initialization firebase FCM
		FirebaseInstanceId.getInstance().getInstanceId()
				.addOnSuccessListener(instanceIdResult -> {

					DebugLog.d("FIREBASE INSTANCE ID ; " + instanceIdResult.getId());
					DebugLog.d("FIREBASE TOKEN : " + instanceIdResult.getToken());

				}).addOnFailureListener(Throwable::printStackTrace);


		//6.initialization SQLite DB manager
		DbRepository.initializedInstance();
		DbRepositoryValue.initializedInstance();

		//7. register activity lifecycle callbacks
		registerActivityLifecycleCallbacks(new ActivityLifecycleHandler(this));

		//8. create notification channels
		DefaultNotificationChannel defaultNotificationChannel = new DefaultNotificationChannel();
		defaultNotificationChannel.createNotificationChannel(this);

		DebugLog.d("Storage dirs list : \n");
		String[] storageDirectories = CommonUtil.getStorageDirectories(getApplicationContext());
		for (String dir : storageDirectories) {
			DebugLog.d(dir + "\n");
		}

		IS_CHECKING_HASIL_PM = false;
		SCHEDULE_NEED_CHECK_IN = false;
		CHECK_APP_VERSION_STATE = false;
		DEVICE_REGISTER_STATE = false;
		ON_FORM_IMBAS_PETIR = false;
		checkinDataModel = new CheckinDataModel();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		unregisterActivityLifecycleCallbacks(new ActivityLifecycleHandler(this));
	}

	@Override
	public void onApplicationStopped() {
		DebugLog.d("application stopped");
	}

	@Override
	public void onApplicationStarted() {
		DebugLog.d("application started");
	}

	@Override
	public void onApplicationPaused() {
		DebugLog.d("application paused");
	}

	@Override
	public void onApplicationResumed() {
		DebugLog.d("application resumed");
	}

	/**
	 * for persistent site location pref
	 *
	 * */
	public boolean isHashMapInitialized() {
		if (hashMapSiteLocation == null) {
			hashMapSiteLocation = new HashMap<>();
			return false;
		} else {
			return true;
		}
	}

	public HashMap<String, AbstractMap.SimpleEntry<String, String>> getHashMapSiteLocation() {
		return hashMapSiteLocation;
	}

	public void setHashMapSiteLocation(HashMap<String, AbstractMap.SimpleEntry<String, String>> hashMapSiteLocation) {
		this.hashMapSiteLocation = hashMapSiteLocation;
	}
	/**
	 * end of persistent site location hash map initialization
	 *
	 * */

	public void toast(String message,int duration){
		Message msg = new Message();
		Bundle bundle = new Bundle();
		bundle.putString("message", message);
		bundle.putInt("duration", duration);
		msg.setData(bundle);
		toastHandler.sendMessage(msg);
	}

	// handler listeners
	@SuppressLint("HandlerLeak")
	private static Handler toastHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			Toast.makeText(instance, msg.getData().getString("message"), msg.getData().getInt("duration")).show();
		};
	};

	@SuppressLint("HandlerLeak")
	private static Handler deviceRegisterHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {

			Bundle bundle = msg.getData();
			boolean isResponseOK = bundle.getBoolean("isresponseok");

			DebugLog.d("response status : " + isResponseOK);
			if (isResponseOK) {
				if (bundle.getString("json") != null) {
					DeviceRegisterResponseModel responseModel = new Gson().fromJson(bundle.getString("json"), DeviceRegisterResponseModel.class);
					PrefUtil.putBoolPref(R.string.key_should_update, responseModel.should_update);
					DebugLog.d(responseModel.toString());
				}
			} else {
				DebugLog.e("response not ok");
			}
		}
	};


	private Thread.UncaughtExceptionHandler _unCaughtExceptionHandler =
			new Thread.UncaughtExceptionHandler() {
		@Override
		public void uncaughtException(Thread thread, Throwable ex) {

			// here I do logging of exception to a db
			PendingIntent myActivity = PendingIntent.getActivity(getContext(),
					192837, new Intent(getContext(), MainActivity.class),
					PendingIntent.FLAG_ONE_SHOT);

//			AlarmManager alarmManager;
//			alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
//			alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
//					1500, myActivity );
//			System.exit(2);

            sendEmail(ex.getMessage());
            System.exit(2);
			// re-throw critical exception further to the os (important)
			defaultUEH.uncaughtException(thread, ex);
		}
	};

    private void sendEmail(String errorLog){
        final Intent emailIntent = new Intent( android.content.Intent.ACTION_SEND);

        emailIntent.setType("plain/text");

        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                new String[] { "rindang@domikado.com" });

        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                "Error Report");

        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                errorLog);

        startActivity(Intent.createChooser(
                emailIntent, "Send mail..."));
    }


	/**
     * new analytics tracker from Firebase
     * @return firebaseAnalytics instance
     * */
	private FirebaseAnalytics mFirebaseAnalytics;

	synchronized public FirebaseAnalytics getDefaultAnalytics() {

	    if (mFirebaseAnalytics == null) {
	        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        }

        return mFirebaseAnalytics;
    }


    public static void sendRegIdtoServer(String token) {
		try {
			if (!PrefUtil.getStringPref(R.string.user_authToken, "").equalsIgnoreCase("")){

				DebugLog.d("send FCM TOKEN to server : " + token);
				APIHelper.registerFCMToken(MyApplication.getInstance(), deviceRegisterHandler, token);
			}
		} catch (Exception e){
			e.printStackTrace();
			Crashlytics.log("FAILED to send FCM TOKEN to server");
			DebugLog.e("FAILED to send FCM TOKEN to server");
			DebugLog.e("ERROR : " + e.getMessage());
		}
	}

	public void setON_FORM_IMBAS_PETIR(boolean onFormImbasPetir) {
		this.ON_FORM_IMBAS_PETIR = onFormImbasPetir;
	}

	public boolean isON_FORM_IMBAS_PETIR() {
		return ON_FORM_IMBAS_PETIR;
	}

	public boolean IS_CHECKING_HASIL_PM() {
		return IS_CHECKING_HASIL_PM;
	}

	public void setIS_CHECKING_HASIL_PM(boolean isCheckingHasilPm) {
		this.IS_CHECKING_HASIL_PM = isCheckingHasilPm;
	}

	public boolean isScheduleNeedCheckIn() {
		return SCHEDULE_NEED_CHECK_IN;
	}

	public void setIsScheduleNeedCheckIn(boolean isScheduleNeedCheckIn) {
		this.SCHEDULE_NEED_CHECK_IN = isScheduleNeedCheckIn;
	}

	public boolean getCHECK_APP_VERSION_STATE() {
		return CHECK_APP_VERSION_STATE;
	}

	public void setCHECK_APP_VERSION_STATE(boolean checkAppVersionState) {
		this.CHECK_APP_VERSION_STATE = checkAppVersionState;
	}

	public boolean getDEVICE_REGISTER_STATE() {
		return DEVICE_REGISTER_STATE;
	}

	public void setDEVICE_REGISTER_STATE(boolean DEVICE_REGISTER_STATE) {
		this.DEVICE_REGISTER_STATE = DEVICE_REGISTER_STATE;
	}
}