package com.rindang.pushnotification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.rindang.zconfig.AppConfig;
import com.sap.inspection.LoginActivity;
import com.sap.inspection.R;
import com.sap.inspection.util.BundleToJson;

public class BaseNotification {
	
	private NotificationCompat.Builder builder;
	private Notification notification;
	
	protected Context context;
	protected Bundle bundle;
	protected String TAG = getClass().getName();
	protected final int NOTIFICATION_ID = 2;
	protected long[] vibratePattern = {
			0, 500
	};
	
	public BaseNotification(Context context, Bundle bundle) {
		this.context = context;
		this.bundle = bundle;
	}

	public void sendNotification(){
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(NOTIFICATION_ID, getNotification());		
	}
	
	protected Notification getNotification(){
		if (notification != null)
			return notification;
		Log.d(TAG, "notification: " + getMessage());
		getNotifBuilder().setContentIntent(getPendingIntent());
		notification = getNotifBuilder().build(); 
		return notification;
	}
	
	protected NotificationCompat.Builder getNotifBuilder(){
		if (builder != null )
			return builder;
		Bitmap bitmap = null;
		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_app);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
		.setSmallIcon(R.drawable.logo_app)
		.setLargeIcon(bitmap)
		.setContentTitle(getTitle())
		.setVibrate(vibratePattern)
		.setStyle(new NotificationCompat.BigTextStyle().bigText(getMessage()))
		.setAutoCancel(true)
		.setContentText(getMessage());
		this.builder = builder;
		return builder;
	}
	
	protected String getTitle(){
		return bundle.getString("title");
	}
	
	protected String getMessage(){
		if (AppConfig.getInstance().config.isProduction()) return bundle.getString("message");
		else if (bundle.getString("message") == null) return BundleToJson.convert(bundle);
		else return "";
	}
	
	protected PendingIntent getPendingIntent(){
		Intent resultIntent = new Intent(context, LoginActivity.class);
		return PendingIntent.getActivity(context.getApplicationContext(),0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);
	}
	
}
