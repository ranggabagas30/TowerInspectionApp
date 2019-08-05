package com.sap.inspection.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.sap.inspection.TowerApplication;
import com.sap.inspection.model.value.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class PrefUtil {

	public static String getStringPref(int resId,String defValue){
		SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(TowerApplication.getInstance());
		return mPref.getString(TowerApplication.getInstance().getString(resId), defValue);
	}

	public static Long getLongPref(int resId,Long defValue){
		SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(TowerApplication.getInstance());
		return mPref.getLong(TowerApplication.getInstance().getString(resId), defValue);
	}

	public static boolean getBoolPref(int resId,boolean defValue){
		SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(TowerApplication.getInstance());
		return mPref.getBoolean(TowerApplication.getInstance().getString(resId), defValue);
	}

	public static int getIntPref(int resId,int defValue){
		SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(TowerApplication.getInstance());
		return mPref.getInt(TowerApplication.getInstance().getString(resId), defValue);
	}
	
	public static float getFloatPref(int resId,float defValue){
		SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(TowerApplication.getInstance());
		return mPref.getFloat(TowerApplication.getInstance().getString(resId), defValue);
	}
	
	public static void putFloatPref(int resId, float value){
		SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(TowerApplication.getInstance());
		mPref.edit().putFloat(TowerApplication.getInstance().getString(resId), value).commit();
	}

	public static void putBoolPref(int resId, boolean value){
		SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(TowerApplication.getInstance());
		mPref.edit().putBoolean(TowerApplication.getInstance().getString(resId), value).commit();
	}

	public static void putStringPref(int resId, String value){
		SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(TowerApplication.getInstance());
		mPref.edit().putString(TowerApplication.getInstance().getString(resId), value).commit();
	}

	public static void putLongPref(int resId, Long value){
		SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(TowerApplication.getInstance());
		mPref.edit().putLong(TowerApplication.getInstance().getString(resId), value).commit();
	}

	public static void putIntPref(int resId, int value){
		SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(TowerApplication.getInstance());
		mPref.edit().putInt(TowerApplication.getInstance().getString(resId), value).commit();
	}

	public static void removeStringPref(int resId){
		SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(TowerApplication.getInstance());
		mPref.edit().remove(TowerApplication.getInstance().getString(resId)).commit();
	}



}
