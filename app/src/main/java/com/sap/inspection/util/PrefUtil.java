package com.sap.inspection.util;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.sap.inspection.view.ui.MyApplication;

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



}
