package com.sap.inspection;

import java.lang.Thread.UncaughtExceptionHandler;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class MyApplication extends Application {

	private UncaughtExceptionHandler defaultUEH;
	private static MyApplication instance;

	public MyApplication() {
		instance = this;
	}

	public static MyApplication getInstance() {
		return instance;
	}

	public static Context getContext() {
		return instance;
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

        // configure Flurry
        FlurryAgent.setLogEnabled(true);

        // init Flurry
        FlurryAgent.init(this, "B7GT4CR8JGPNQB94PM86");

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
}