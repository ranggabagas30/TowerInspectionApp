package com.sap.inspection;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
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
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.pixplicity.easyprefs.library.Prefs;
import com.rindang.pushnotification.notificationchannel.DefaultNotificationChannel;
import com.sap.inspection.listener.ActivityLifecycleHandler;
import com.sap.inspection.model.CheckinDataModel;
import com.sap.inspection.model.DbRepository;
import com.sap.inspection.model.TextMarkDisplayOptionsModel;
import com.sap.inspection.model.TextMarkModel;
import com.sap.inspection.model.value.DbRepositoryValue;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.CommonUtil;
import com.sap.inspection.view.ui.MainActivity;
import com.scottyab.aescrypt.AESCrypt;

import java.lang.Thread.UncaughtExceptionHandler;
import java.security.Key;
import java.util.AbstractMap;
import java.util.HashMap;

import io.fabric.sdk.android.Fabric;

/*import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;*/

/**
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class TowerApplication extends Application implements ActivityLifecycleHandler.LifecycleListener {

	private UncaughtExceptionHandler defaultUEH;
	private static TowerApplication instance;
	private HashMap< String, AbstractMap.SimpleEntry<String, String> > hashMapSiteLocation;
	private boolean IS_CHECKING_HASIL_PM;
	private boolean ON_FORM_IMBAS_PETIR;
	private boolean SCHEDULE_NEED_CHECK_IN;
	private boolean CHECK_APP_VERSION_STATE;
	private boolean DEVICE_REGISTRATION_STATE;

	public static Key key;
	public CheckinDataModel checkinDataModel;

	public TowerApplication() {
		instance = this;
	}

	public static TowerApplication getInstance() {
		return instance;
	}

	public static Context getContext() {
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// initialization crashlytics
		Fabric.with(this, new Crashlytics());

		// initialization stetho facebook debug
		if (BuildConfig.DEBUG) {
			Stetho.initializeWithDefaults(this);
			AESCrypt.DEBUG_LOG_ENABLED = true;
		}

		// Prefs initialization
		new Prefs.Builder()
				.setContext(this)
				.setMode(ContextWrapper.MODE_PRIVATE)
				.setPrefsName(getPackageName())
				.setUseDefaultSharedPreference(true)
				.build();

		// initialization image loader configuration
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
				.memoryCacheSize(20 * 1024 * 1024)
				.discCacheSize(104857600)
				.threadPoolSize(10)
				.build();

		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);

		// initialization text mark settings for photo item
		TextMarkDisplayOptionsModel textOption = new TextMarkDisplayOptionsModel.Builder(getApplicationContext())
				.setTextColor(Color.WHITE)
				.setTextColorStyle(Paint.Style.FILL)
				.setTextAlign(Paint.Align.LEFT)
				.setTextStyle(Typeface.BOLD)
				.setTextFamilyName("Helvetica")
				.build();

		TextMarkModel.getInstance().init(textOption);

		// initialization SQLite DB manager
		DbRepository.initializedInstance();
		DbRepositoryValue.initializedInstance();

		// register activity lifecycle callbacks
		registerActivityLifecycleCallbacks(new ActivityLifecycleHandler(this));

		// create notification channels
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
		DEVICE_REGISTRATION_STATE = false;
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
		handler.sendMessage(msg);
	}

	@SuppressLint("HandlerLeak")
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

	public boolean getDEVICE_REGISTRATION_STATE() {
		return DEVICE_REGISTRATION_STATE;
	}

	public void setDEVICE_REGISTRATION_STATE(boolean DEVICE_REGISTRATION_STATE) {
		this.DEVICE_REGISTRATION_STATE = DEVICE_REGISTRATION_STATE;
	}
}