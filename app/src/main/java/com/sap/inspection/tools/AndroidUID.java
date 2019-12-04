package com.sap.inspection.tools;
/**
 * Created by Rindang Septyan
 */

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.RequiresPermission;
import android.telephony.TelephonyManager;

import com.google.firebase.iid.FirebaseInstanceId;
import com.pixplicity.easyprefs.library.Prefs;
import com.sap.inspection.BuildConfig;
import com.sap.inspection.R;

import java.util.Calendar;
import java.util.Random;

public class AndroidUID {
	private static String UID;

	/**
	 * @deprecated
	 * */
	public static String getUID(Context context){
		if (UID != null)
			return UID;
		TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		UID = telephonyManager.getDeviceId();
		if (UID != null)
			return UID;

		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wInfo = wifiManager.getConnectionInfo();
		UID = wInfo.getMacAddress();

		if (UID != null)
			return UID;

		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		if (pref.getString(context.getString(R.string.uid),null) != null)
			return pref.getString(context.getString(R.string.uid),null);

		Random random = new Random(Calendar.getInstance().getTimeInMillis());
		UID = Calendar.getInstance().getTimeInMillis()+""+Calendar.getInstance().getTimeZone().getID()+""+random.nextInt();
		pref.edit().putString(context.getString(R.string.uid), UID).commit();
		return UID;
	}

	@RequiresPermission(Manifest.permission.READ_PHONE_STATE)
	public static String getDeviceID(Context context) {

		// UID has been assigned, return it
		if (UID != null) return UID;

		TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if (telephony.getDeviceId() != null){ // use IMEI
			UID = telephony.getDeviceId();
		}else{
			// else use Secure.AndroidID
			UID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
		}

		if (UID == null) {
			WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
			if (wifiManager != null) {
				WifiInfo wInfo = wifiManager.getConnectionInfo();
				if (wInfo != null) // use mac address
					UID = wInfo.getMacAddress();
			}
		}

		if (UID == null) { // use Firebase Instance ID
			UID = Prefs.getString(context.getString(R.string.app_fcm_reg_id), null);
		}
		return UID;
	}
}
