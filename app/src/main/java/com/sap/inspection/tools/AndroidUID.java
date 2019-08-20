package com.sap.inspection.tools;
/**
 * Created by Rindang Septyan
 */

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import com.sap.inspection.R;

import java.util.Calendar;
import java.util.Random;

public class AndroidUID {
	Activity activity;
	public AndroidUID(Activity activity) {
		this.activity = activity;
	}
	public static String UID;

	public static  String getUID(Context context){
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

}
