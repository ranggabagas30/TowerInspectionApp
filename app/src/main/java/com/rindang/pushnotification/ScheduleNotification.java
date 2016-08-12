package com.rindang.pushnotification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.sap.inspection.LoginActivity;
import com.sap.inspection.constant.Constants;

public class ScheduleNotification extends BaseNotification {
	

	public ScheduleNotification(Context context, Bundle bundle) {
		super(context, bundle);
	}
	
	@Override
	protected PendingIntent getPendingIntent() {
		Intent resultIntent = new Intent(context, LoginActivity.class);
//		resultIntent.putExtra("order_code", bundle.getString("order_code"));
		resultIntent.putExtra(Constants.LOADSCHEDULE,true);
		return PendingIntent.getActivity(context.getApplicationContext(),0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);
	}
	
	@Override
	protected String getMessage() {
		return bundle.getString("message");
	}
	
	@Override
	protected String getTitle() {
		return "SAP Mobile Application";
	}
	
	private String getOrderCode(){
		return bundle.getString("order_code");
	}
}