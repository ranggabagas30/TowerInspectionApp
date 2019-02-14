package com.sap.inspection;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.facebook.stetho.Stetho;
/*import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;*/
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.sap.inspection.connection.APIHelper;
import com.sap.inspection.model.CheckinDataModel;
import com.sap.inspection.model.DbManager;
import com.sap.inspection.model.DbRepository;
import com.sap.inspection.model.TextMarkDisplayOptionsModel;
import com.sap.inspection.model.TextMarkModel;
import com.sap.inspection.model.value.DbRepositoryValue;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.tools.PrefUtil;
import com.sap.inspection.util.ImageUtil;
import com.sap.inspection.util.Utility;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.AbstractMap;
import java.util.HashMap;

import io.fabric.sdk.android.Fabric;

/**
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class MyApplication extends Application {

	private UncaughtExceptionHandler defaultUEH;
	private static MyApplication instance;
	private HashMap< String, AbstractMap.SimpleEntry<String, String> > hashMapSiteLocation;
	private boolean IN_CHECK_HASIL_PM;
	private boolean SCHEDULE_NEED_CHECK_IN;
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
		handler.sendMessage(msg);
	}

	Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			Toast.makeText(instance, msg.getData().getString("message"), msg.getData().getInt("duration")).show();
		};
	};

	// handler listener
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

	@Override
	public void onCreate() {
		super.onCreate();

		//1. initialization crashlytics
		Fabric.with(this, new Crashlytics());

		//2. initialization stetho facebook debug
		if (BuildConfig.DEBUG) {
			Stetho.initializeWithDefaults(this);
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

					storeRegIdInpref(instanceIdResult.getToken());
					sendRegIdtoServer(instanceIdResult.getToken());

				}).addOnFailureListener(Throwable::printStackTrace);


		//6.initialization SQLite DB manager
		DbRepository.initializedInstance();
		DbRepositoryValue.initializedInstance();

		DebugLog.d("Storage dirs list : \n");
		String[] storageDirectories = Utility.getStorageDirectories(getApplicationContext());
		for (String dir : storageDirectories) {
			DebugLog.d(dir + "\n");
		}


		IN_CHECK_HASIL_PM = false;
		SCHEDULE_NEED_CHECK_IN = false;
		checkinDataModel = new CheckinDataModel();
	}

	/*private Tracker mTracker;

	*//**
	 * Gets the default {@link Tracker} for this {@link Application}.
	 * @return tracker
	 *//*
	synchronized public Tracker getDefaultTracker() {
		if (mTracker == null) {
			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
			// To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
			mTracker = analytics.newTracker(R.xml.global_tracker);
		}
		return mTracker;
	}*/

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

	private void storeRegIdInpref(String token) {
		PrefUtil.putStringPref(R.string.app_fcm_reg_id, token);
	}

	private void sendRegIdtoServer(String token) {
		try {
			if (!PrefUtil.getStringPref(R.string.user_authToken, "").equalsIgnoreCase("")){
				APIHelper.registerFCMToken(MyApplication.getInstance(), new Handler(),  token);
			}
		} catch (Exception e){
			e.printStackTrace();
			DebugLog.e("ERROR : " + e.getMessage());
		}
	}

	public boolean isInCheckHasilPm() {
		return IN_CHECK_HASIL_PM;
	}

	public void setIsInCheckHasilPm(boolean isInCheckHasilPm) {
		IN_CHECK_HASIL_PM = isInCheckHasilPm;
	}

	public boolean isScheduleNeedCheckIn() {
		return SCHEDULE_NEED_CHECK_IN;
	}

	public void setIsScheduleNeedCheckIn(boolean isScheduleNeedCheckIn) {
		SCHEDULE_NEED_CHECK_IN = isScheduleNeedCheckIn;
	}
}