package com.rindang.pushnotification;

import android.content.Context;
import android.os.Bundle;

import com.sap.inspection.tools.DebugLog;

public class NotificationProcessor {
	public static BaseNotification getNotification(Bundle bundle, Context context){
		String extrasType = bundle.getString("type");
		DebugLog.d(""+extrasType);
		BaseNotification baseNotification = new BaseNotification(context, bundle);
		if (extrasType == null);
		else if (extrasType.startsWith("schedule"))
			baseNotification = new ScheduleNotification(context, bundle);
		return baseNotification;
	}
}
