package com.sap.inspection;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.facebook.stetho.Stetho;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

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

	public MyApplication() {
		instance = this;
	}

	public static MyApplication getInstance() {
		return instance;
	}

	public static Context getContext() {
		return instance;
	}

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
		Fabric.with(this, new Crashlytics());
		if (BuildConfig.DEBUG) {
			Stetho.initializeWithDefaults(this);
		}

//		defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
//
//		setup handler for uncaught exception
//		Thread.setDefaultUncaughtExceptionHandler(_unCaughtExceptionHandler);

		// This configuration tuning is custom. You can tune every option, you may tune some of them, 
		// or you can create default configuration by
		//  ImageLoaderConfiguration.createDefault(this);
		// method.
		//		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
		//			.threadPoolSize(3)
		//			.threadPriority(Thread.NORM_PRIORITY - 2)
		//			.memoryCacheSize(2 * 1024 * 1024) // 2 Mb
		//			.denyCacheImageMultipleSizesInMemory()
		//			.discCacheFileNameGenerator(new Md5FileNameGenerator())
		//			.imageDownloader(new AssetsImageDownloader(getApplicationContext()))
		//			.enableLogging() // Not necessary in common
		//			.build();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
		//		.enableLogging()
		.memoryCacheSize(20 * 1024 * 1024)
		.discCacheSize(104857600)
		.threadPoolSize(10)
		.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}

	private Tracker mTracker;

	/**
	 * Gets the default {@link Tracker} for this {@link Application}.
	 * @return tracker
	 */
	synchronized public Tracker getDefaultTracker() {
		if (mTracker == null) {
			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
			// To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
			mTracker = analytics.newTracker(R.xml.global_tracker);
		}
		return mTracker;
	}


}