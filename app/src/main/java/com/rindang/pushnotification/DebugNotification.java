package com.rindang.pushnotification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.sap.inspection.LoginActivity;
import com.sap.inspection.util.BundleToJson;

public class DebugNotification extends BaseNotification {
	

	public DebugNotification(Context context, Bundle bundle) {
		super(context, bundle);
	}
	
	@Override
	protected PendingIntent getPendingIntent() {
		Intent resultIntent = new Intent(context, LoginActivity.class);
//		resultIntent.putExtra("order_code", bundle.getString("order_code"));
		return PendingIntent.getActivity(context.getApplicationContext(),0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);
	}
	
	@Override
	protected String getMessage() {
		return "This is general notification : "+BundleToJson.convert(bundle);
	}
	
	@Override
	protected String getTitle() {
		return "SAP Mobile Application";
	}
	
	private String getOrderCode(){
		return bundle.getString("order_code");
	}
}