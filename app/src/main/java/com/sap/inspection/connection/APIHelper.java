package com.sap.inspection.connection;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.rindang.zconfig.APIList;
import com.sap.inspection.R;
import com.sap.inspection.tools.AndroidUID;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.LinkedList;


public class APIHelper {

	public static void getJsonFromUrl(Context context, Handler handler, String url){
		JSONConnection getJson = new JSONConnection(context,url, handler);
		getJson.execute();
	}

	public static void getJsonFromUrl(Context context, Handler handler, String url,String token){
		JSONConnection getJson = new JSONConnection(context,url+"&"+token, handler);
		getJson.execute();
	}

	public static void postParams(Context context,String url,Handler handler,LinkedList<NameValuePair> params){
		JSONConnectionPOST postJson = new JSONConnectionPOST(context, url, handler, params);
		postJson.execute();
	}

	//Account
	public static void login(Context context,Handler handler, String userName, String password){
		LinkedList<NameValuePair> params = new LinkedList<NameValuePair>();
		NameValuePair nvp = new BasicNameValuePair("username", userName);
		params.add(nvp);
		nvp = new BasicNameValuePair("password", password);
		params.add(nvp);
		postParams(context, APIList.loginUrl(), handler, params);
	}

	public static void registerGCMToken(Context context, Handler handler, String token){
		LinkedList<NameValuePair> params = new LinkedList<NameValuePair>();
		NameValuePair nvp = new BasicNameValuePair("token", token);
		params.add(nvp);
		nvp = new BasicNameValuePair("device_id", AndroidUID.getUID(context));
		params.add(nvp);
		postParams(context, APIList.gcmTokenRegeisterUrl()+"?access_token="+getAccessToken(context), handler, params);
	}

	public static void logout(Context context,Handler handler, String userName, String password){
		getJsonFromUrl(context, handler, APIList.logoutUrl());
	}

	//Users
	public static void getUser(Context context,Handler handler, String userName){
		getJsonFromUrl(context, handler, APIList.userUrl()+"/"+userName+"?access_token="+getAccessToken(context));
	}

	public static void getUserPictures(Context context,Handler handler, String userName){
		getJsonFromUrl(context, handler, APIList.userUrl()+"/"+userName+"/pictures"+"?access_token="+getAccessToken(context));
	}

	//Schedules
	public static void getSchedules(Context context,Handler handler, String userId){
		getJsonFromUrl(context, handler, APIList.userUrl()+"/"+userId+"/schedules?template=default&per_page=-1&"+"access_token="+getAccessToken(context));
	}

	//Forms
	public static void getFormVersion(Context context,Handler handler, String userId){
		getJsonFromUrl(context, handler, APIList.formVersionUrl()+"?access_token="+getAccessToken(context));
	}
	
	public static void getForms(Context context,Handler handler, String userId){
		getJsonFromUrl(context, handler, APIList.formsUrl()+"?template=full&user_id="+userId+"&access_token="+getAccessToken(context));
//		getJsonFromUrl(context, handler, APIList.formGroupUrl()+"?template=full&user_id="+userId+"&access_token="+getAccessToken(context));
	}

	public static String getAccessToken(Context context){
		SharedPreferences mpref =  PreferenceManager.getDefaultSharedPreferences(context);
		return mpref.getString(context.getString(R.string.user_authToken), "");
	}

	//APK
	public static void getAPKVersion(Context context,Handler handler, String userId){
		getJsonFromUrl(context, handler, APIList.apkUrl()+"?access_token="+getAccessToken(context));
	}

}
