package com.rindang.pushnotification;

import android.content.Context;
import android.os.Bundle;

import com.rindang.pushnotification.notificationchannel.DefaultNotificationChannel;
import com.sap.inspection.tools.DebugLog;

public class NotificationProcessor {
	private static final int SCHEDULE_NOTIFICATION_ID = 1;
	private static final int APK_NOTIFICATION_ID = 2;

	public static BaseNotification getNotification(Bundle bundle, Context context){
		String extrasType = bundle.getString("type");
		DebugLog.d(""+extrasType);
		BaseNotification baseNotification = new BaseNotification(context, bundle, DefaultNotificationChannel.getChannelId(), DefaultNotificationChannel.getChannelPriorityLevel());
		if (extrasType == null);
		else if (extrasType.startsWith("schedule"))
			baseNotification = new ScheduleNotification(context, bundle, DefaultNotificationChannel.getChannelId(), DefaultNotificationChannel.getChannelPriorityLevel(), SCHEDULE_NOTIFICATION_ID);
		else if (extrasType.startsWith("apk"))
			baseNotification = new ApkNotification(context, bundle, DefaultNotificationChannel.getChannelId(), DefaultNotificationChannel.getChannelPriorityLevel(), APK_NOTIFICATION_ID);
		return baseNotification;
	}
}
