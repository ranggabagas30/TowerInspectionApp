package com.rindang.pushnotification;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

	private static final String TAG = GcmBroadcastReceiver.class.getSimpleName();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "receive wakeful broadcast");
		ComponentName comp = new ComponentName(context.getPackageName(), GcmIntentService.class.getName());
		startWakefulService(context, (intent.setComponent(comp)));
		setResultCode(Activity.RESULT_OK);
	}

}