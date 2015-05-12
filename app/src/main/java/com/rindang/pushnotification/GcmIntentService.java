package com.rindang.pushnotification;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService extends IntentService {

	private static final String TAG = GcmIntentService.class.getSimpleName();
	private static final int NOTIFICATION_ID = 2;

	private long[] vibratePattern = {
			0, 500
	};

	private NotificationManager notificationManager;

	public GcmIntentService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		logPush(intent);
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);
		Log.d(TAG, "==== GCM IN ====");
		Log.d(TAG, "messageType: " + messageType);
//		Log.d(TAG, "all data : " + BundleToJson.convert(extras));
		if(!extras.isEmpty()){
			Log.d(TAG, "2.extras: " + extras);
			if(GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)){
			}

			if(GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)){
			}

			if(GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)){
//				String extrasMessage = extras.getString("message");
//				String extrasTitle = extras.getString("title");
//				String extrasType = extras.getString("notification_type");
//				Log.d(TAG, "message----: " + extrasMessage);
//				if (extrasMessage.contains("rindang")) Log.d(TAG, "hey it's rindang");
				Log.d(TAG, "on message type message");
				BaseNotification notif = NotificationProcessor.getNotification(extras, this);
				notif.sendNotification();
//				DebugNotification notif = new DebugNotification(this, extras);
//				notif.sendNotification();

			}
		}
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	private void logPush(Intent intent){
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);
		Log.d(TAG, "messageType: " + messageType);
		if(!extras.isEmpty()){
			Log.d(TAG, "2.extras: " + extras);
			String extrasMessage = extras.getString("message");
			Log.d(TAG, "message---- : " + extrasMessage);
		}
	}

}