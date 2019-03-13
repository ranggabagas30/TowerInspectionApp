package com.rindang.pushnotification;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sap.inspection.MyApplication;
import com.sap.inspection.R;
import com.sap.inspection.connection.APIHelper;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.PrefUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class FCMListenerService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        DebugLog.d("==== FIREBASE MESSAGE RECEIVED ====\n");

        // [START_EXCLUDE]
        // There are two types of messages : data messages and notification messages. Data messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data
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

            DebugLog.d("Message data payload : " + remoteMessage.getData());

            // data can be processed in long-running mode (using Firebase Job Dispatcher)
            // or just handle it directly which is short-running mode (under 10 secs)

            try {
                JSONObject jsonObject = new JSONObject(remoteMessage.getData().toString());
                handleDataMessage(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
                DebugLog.e("JSON ERROR : " + e.getMessage());
            }
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

    private void handleDataMessage(JSONObject json) {
        DebugLog.d("push json : " + json.toString());

        try {
            String type = json.getString("type");
            String message = json.getString("message");

            //JSONObject jsonCollapseKey = json.getJSONObject("collapse_ke");

            DebugLog.d("data : {");
            DebugLog.d("--type : " + type);
            DebugLog.d("--message : " + message);
            DebugLog.d("}");
           // DebugLog.d("collapse_key : " + jsonCollapseKey.toString());

            Bundle extras = new Bundle();
            extras.putString("type", type);
            extras.putString("message", message);

            BaseNotification notif = NotificationProcessor.getNotification(extras, this);
            notif.sendNotification();

        } catch (JSONException e) {
            e.printStackTrace();
            DebugLog.e("JSON error : " + e.getMessage());
        }
    }
}
