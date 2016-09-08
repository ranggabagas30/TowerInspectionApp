package com.rindang.pushnotification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.sap.inspection.R;
import com.sap.inspection.SettingActivity;

public class ApkNotification extends BaseNotification {


	public ApkNotification(Context context, Bundle bundle) {
		super(context, bundle);
	}
	
	@Override
	protected PendingIntent getPendingIntent() {
		Intent resultIntent = new Intent(context, SettingActivity.class);
		return PendingIntent.getActivity(context.getApplicationContext(),0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);
	}
	
	@Override
	protected String getMessage() {
		return bundle.getString("message");
	}
	
	@Override
	protected String getTitle() {
		return context.getString(R.string.app_name);
	}
	
	private String getOrderCode(){
		return bundle.getString("order_code");
	}
}