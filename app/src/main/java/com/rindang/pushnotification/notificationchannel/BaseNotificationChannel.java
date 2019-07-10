package com.rindang.pushnotification.notificationchannel;

import android.content.Context;

public abstract class BaseNotificationChannel {

    public abstract void createNotificationChannel(Context context);
    public abstract void deleteNotificationChannel(Context context);
}
