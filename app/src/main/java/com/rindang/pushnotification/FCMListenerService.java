package com.rindang.pushnotification;

import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sap.inspection.R;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.PrefUtil;

public class FCMListenerService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        DebugLog.d("==== FIREBASE MESSAGE RECEIVED ====\n");

        // [START_EXCLUDE]
        // There are two types of messages : data messages and notification messages. ImbasPetirData messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. ImbasPetirData
        // messages are the type
        // traditionally used with GCM. Notification messages are only received here in
        // onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated
        // notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages
        // containing both notification
        // and data payloads are treated as notification messages. The Firebase console always
        // sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        DebugLog.d("From : " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {

            // data can be processed in long-running mode (using Firebase Job Dispatcher)
            // or just handle it directly which is short-running mode (under 10 secs)
            handleDataMessage(remoteMessage);
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {

            String messageType = remoteMessage.getMessageType();
            String message = remoteMessage.getNotification().getBody();

            DebugLog.d("Message type : " + messageType);
            DebugLog.d("Message Notification Body : " + message);

            Bundle bundleMessage = new Bundle();
            bundleMessage.putString("messageType", messageType);
            bundleMessage.putString("message", message);
            handleNotification(bundleMessage);
        }
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

        /**
         * once get new registration token
         * 1. save to shared pref
         * 2. send token to server
         *
         * */
        DebugLog.d("NEW TOKEN : " + s);
        storeRegIdInpref(s);
    }

    private void storeRegIdInpref(String token) {
        PrefUtil.putStringPref(R.string.app_fcm_reg_id, token);
    }

    private void handleNotification(Bundle bundleMessage) {

        Toast.makeText(getApplicationContext(), "message notification : " + bundleMessage.getString("message"), Toast.LENGTH_SHORT).show();
        DebugLog.d("message notification : " + bundleMessage.getString("message"));
    }

    private void handleDataMessage(RemoteMessage remoteMessage) {

        String type = remoteMessage.getData().get("type");
        String message = remoteMessage.getData().get("message");
        DebugLog.d("Message data payload : " + remoteMessage.getData());

        Bundle extras = new Bundle();
        extras.putString("type", type);
        extras.putString("message", message);

        NotificationProcessor.getNotification(extras, this).sendNotification();
    }
}
