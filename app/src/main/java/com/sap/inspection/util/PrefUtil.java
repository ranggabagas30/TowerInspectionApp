package com.sap.inspection.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.sap.inspection.MyApplication;
import com.sap.inspection.model.value.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class PrefUtil {

	public static String getStringPref(int resId,String defValue){
		SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance());
		return mPref.getString(MyApplication.getInstance().getString(resId), defValue);
	}

	public static Long getLongPref(int resId,Long defValue){
		SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance());
		return mPref.getLong(MyApplication.getInstance().getString(resId), defValue);
	}

	public static boolean getBoolPref(int resId,boolean defValue){
		SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance());
		return mPref.getBoolean(MyApplication.getInstance().getString(resId), defValue);
	}

	public static int getIntPref(int resId,int defValue){
		SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance());
		return mPref.getInt(MyApplication.getInstance().getString(resId), defValue);
	}
	
	public static float getFloatPref(int resId,float defValue){
		SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance());
		return mPref.getFloat(MyApplication.getInstance().getString(resId), defValue);
	}
	
	public static void putFloatPref(int resId, float value){
		SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance());
		mPref.edit().putFloat(MyApplication.getInstance().getString(resId), value).commit();
	}

	public static void putBoolPref(int resId, boolean value){
		SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance());
		mPref.edit().putBoolean(MyApplication.getInstance().getString(resId), value).commit();
	}

	public static void putStringPref(int resId, String value){
		SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance());
		mPref.edit().putString(MyApplication.getInstance().getString(resId), value).commit();
	}

	public static void putLongPref(int resId, Long value){
		SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance());
		mPref.edit().putLong(MyApplication.getInstance().getString(resId), value).commit();
	}

	public static void putIntPref(int resId, int value){
		SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance());
		mPref.edit().putInt(MyApplication.getInstance().getString(resId), value).commit();
	}

	public static void removeStringPref(int resId){
		SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance());
		mPref.edit().remove(MyApplication.getInstance().getString(resId)).commit();
	}

	private static final String KEYCODE = "persistentdata";

	public static String getSharedPreference(Context context, String key) {
		return context.getSharedPreferences(KEYCODE, Context.MODE_PRIVATE).getString(key,"");
	}

	public static void setSharedPreference(Context context, String key, String value) {
		context.getSharedPreferences(KEYCODE, Context.MODE_PRIVATE).edit().putString(key,value).commit();
	}

	public static void setPersistentLatitude(Context context, String scheduleId, String latitude) {
		String compiledpersistentdata = scheduleId + "," + latitude;
		setSharedPreference(context, KEY.PREFPERSISTENTLATITUDE, compiledpersistentdata);
	}

	public static void setPersistentLongitude(Context context, String scheduleId, String longitude) {
		String compiledpersistentdata = scheduleId + "," + longitude;
		setSharedPreference(context, KEY.PREFPERSISTENTLONGITUDE, compiledpersistentdata);
	}

	public static void setPersistenLatLng(Context context, String scheduleId, String latitude, String longitude) {
		String compiledpersistentdata = scheduleId + "," + latitude + "," + longitude;
		setSharedPreference(context, KEY.PREFPERSISTENTLATLNG, compiledpersistentdata);
		setPersistentLatitude(context, scheduleId, latitude);
		setPersistentLongitude(context, scheduleId, longitude);
	}

	private static class KEY {
		private static final String PREFPERSISTENTLATITUDE = "PREFPERSISTENTLATITUDE";
		private static final String PREFPERSISTENTLONGITUDE = "PREFPERSISTENTLONGITUDE";
		private static final String PREFPERSISTENTLATLNG = "PREFPERSISTENTLATLNG";
	}
}
