package com.rindang.pushnotification.notificationchannel;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

public class DefaultNotificationChannel extends BaseNotificationChannel{

    private static String CHANNEL_ID = "DEFAULT_CHANNEL_ID";
    private CharSequence CHANNEL_NAME = "DEFAULT NOTIFICATION";
    private String CHANNEL_DESC = "Default Tower Inspection notification channel";
    private int CHANNEL_IMPORTANCE_LEVEL = NotificationManager.IMPORTANCE_DEFAULT; // for Android OS 8.0 and above
    private static int CHANNEL_PRIORITY_LEVEL = NotificationCompat.PRIORITY_DEFAULT; // for Android OS 7.0 and lower
    private NotificationChannel channel;

    @Override
    public void createNotificationChannel(Context context) {

        if (channel == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, CHANNEL_IMPORTANCE_LEVEL);
            channel.setDescription(CHANNEL_DESC);
            channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                            .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build());
            channel.setVibrationPattern(new long[] { 1000, 1000, 1000, 1000, 1000 });

            // register notification channel. You cannot change after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void deleteNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.deleteNotificationChannel(CHANNEL_ID);
        }
    }

    public static String getChannelId() {
        return CHANNEL_ID;
    }
    public static int getChannelPriorityLevel() { return CHANNEL_PRIORITY_LEVEL; }
}
