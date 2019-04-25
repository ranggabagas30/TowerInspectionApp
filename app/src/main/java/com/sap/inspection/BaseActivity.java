package com.sap.inspection;


import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.Window;
import android.widget.Toast;

/*import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;*/
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sap.inspection.constant.GlobalVar;
import com.sap.inspection.event.UploadProgressEvent;
import com.sap.inspection.manager.ScreenManager;
import com.sap.inspection.model.DbRepository;
import com.sap.inspection.model.value.DbRepositoryValue;
import com.sap.inspection.tools.DebugLog;

import de.greenrobot.event.EventBus;

//import com.sap.inspection.gcm.GCMService;

public abstract class BaseActivity extends FragmentActivity{
	protected FragmentActivity activity;
	public static final int ACTIVITY_REQUEST_CODE = 311; 
	protected SharedPreferences mPref;

	public static ImageLoader imageLoader = ImageLoader.getInstance();
	public static DisplayImageOptions  avatarOptions = new DisplayImageOptions.Builder()
	.showStubImage(R.drawable.logo_app)
	.cacheInMemory()
	.cacheOnDisc()
//	.resetViewBeforeLoading()
//	.delayBeforeLoading(300)
//	.displayer(new RoundedBitmapDisplayer(30))
	.build();
	
	public static DisplayImageOptions  itemOptions = new DisplayImageOptions.Builder()
//	.showStubImage(R.drawable.ic_launcher)
	.cacheInMemory()
	.cacheOnDisc()
//	.resetViewBeforeLoading()
//	.delayBeforeLoading(300)
//	.displayer(new RoundedBitmapDisplayer(30))
	.build();

	private boolean instanceStateSaved;

	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		progressDialog = new ProgressDialog(this);
		progressDialog.setCancelable(false);

		activity = this;
//        int x = 0;
//        int y = 1/x;
		mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		 getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		ScreenManager.getInstance().setHeight(metrics.heightPixels);
		ScreenManager.getInstance().setWidth(metrics.widthPixels);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		EventBus.getDefault().register(this);
//		GCMService.baseActivity = this;
	}

	@Override
	protected void onPause() {
		super.onPause();
		EventBus.getDefault().unregister(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
	@Override
	protected void onDestroy() {
		if (!instanceStateSaved) {
//			imageLoader.stop();
		}
		super.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		instanceStateSaved = true;
	}

	public void onEvent(UploadProgressEvent event) {
		DebugLog.d("event="+new Gson().toJson(event));
		if (!event.done)
			showMessageDialog(event.progressString);
		else
			hideDialog();
	}

	public void writePreference(int key, String value) {
		mPref.edit().putString(getString(key), value).commit();
	}
	
	public void writePreference(String key, String value) {
		mPref.edit().putString(key, value).commit();
	}
	
	public void writePreference(int key, int value) {
		mPref.edit().putInt(getString(key), value).commit();
	}
	
	public void writePreference(int key, boolean value) {
		mPref.edit().putBoolean(getString(key), value).commit();
	}
	
	public String getPreference(int key, String defaultValue) {
		return mPref.getString(getString(key), defaultValue);
	}
	
	public String getPreference(String key, String defaultValue) {
		return mPref.getString(key, defaultValue);
	}
	
	public int getPreference(int key, int defaultValue) {
		return mPref.getInt(getString(key), defaultValue);
	}
	
	public boolean getPreference(int key, boolean defaultValue) {
		return mPref.getBoolean(getString(key), defaultValue);
	}

	protected int color(int colorRes) {
		return ContextCompat.getColor(this, colorRes);
	}

	protected void trackThisPage(String name) {
// Obtain the shared Tracker instance.
		/*MyApplication application = (MyApplication) getApplication();
		Tracker mTracker = application.getDefaultTracker();
		DebugLog.d("Track screen name: " + name);
		mTracker.setScreenName(name);
		mTracker.send(new HitBuilders.ScreenViewBuilder().build());*/

		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
		MyApplication myApplication = (MyApplication) getApplication();
		FirebaseAnalytics mFirebaseAnalytics = myApplication.getDefaultAnalytics();
		mFirebaseAnalytics.logEvent("track_page", bundle);
	}

	protected void trackEvent(String name) {
// Obtain the shared Tracker instance.
		/*MyApplication application = (MyApplication) getApplication();
		Tracker mTracker = application.getDefaultTracker();
		DebugLog.d("Track event name: " + name);
		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory("Event")
				.setAction(name)
				.build());*/

		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
		MyApplication myApplication = (MyApplication) getApplication();
		FirebaseAnalytics mFirebaseAnalytics = myApplication.getDefaultAnalytics();
		mFirebaseAnalytics.logEvent("track_event", bundle);
	}

	public void hideDialog() {

		if (progressDialog != null && progressDialog.isShowing())
			progressDialog.dismiss();

	}

	public void showMessageDialog(String message) {

		if (progressDialog != null) {
			progressDialog.setMessage(message);

			if (!progressDialog.isShowing())
				progressDialog.show();
		}
	}
}
