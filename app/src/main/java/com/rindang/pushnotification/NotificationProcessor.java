package com.rindang.pushnotification;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

public class NotificationProcessor {
	public static BaseNotification getNotification(Bundle bundle, Context context){
		String extrasType = bundle.getString("type");
		Log.d("notification processor", ""+extrasType);
		BaseNotification baseNotification = new BaseNotification(context, bundle);
		if (extrasType == null);
		else if (extrasType.startsWith("schedule"))
			baseNotification = new ScheduleNotification(context, bundle);
		return baseNotification;
	}
}
