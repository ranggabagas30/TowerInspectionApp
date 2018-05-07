package com.sap.inspection.constant;

public class Constants {
	
	public static final String[] DAYS 		= {"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
	public static final String[] MONTHS 	= {"Januari","Februari","Maret","April","Mei","Juni","Juli","Agustus","September","Oktober","November","Desember"};
	
	/**
	 * ============================ PUSH NOTIFICATION ===================================================== 
	 */
	
	public static final String SENDER_ID = "494949404342";
	
	/**
	 * ============================ ACTIVITY MAPPING ===================================================== 
	 */

	
	public static final int CALLENDAR_ACTIVITY = 101;
	
	
	public static final int DEFAULT_REQUEST_CODE 		= 311;
	
	public static final String JSON_CONTENT_TYPE = "application/json";
	
	/**
	 * ============================ CONSUMER ===================================================== 
	 */

	public static final String CONSUMER_KEY_MINE 		= "8D1IVo2wMNKasGy8mojnzfPf1Vd0K1Qo1tiMs9Or";
	public static final String CONSUMER_SECRET_MINE 	= "1Kk6jOYUg0hoB9XUOiNCMgFwW0FwS6JFEFnpDgcp";

	public static String CONSUMER_KEY = CONSUMER_KEY_MINE;
	public static String CONSUMER_SECRET = CONSUMER_SECRET_MINE;

	/**
	 * ============================ URL ===================================================== 
	 */
	
	//Main - URL
//	public static String MAIN_URL			= "http://tower-api.demo.domikado.com";
//	public static String MAIN_URL			= "http://mobile-api.sekap.net";
//	public static String MAIN_URL			= "http://192.168.120.182:9292";
//	public static String MAIN_URL			= "http://192.168.120.140:9292";
//	public static String MAIN_URL			= "http://192.168.88.48";
//	public static String MAIN_URL			= "http://192.168.120.244:9292";
//	public static String VERSION_URL		= AppConfig.getInstance().config.getHost() + "/v1";
	
	//Account
//	public static String LOGIN					= VERSION_URL + "/login";
//	public static String GCM_TOKEN_REGISTER		= VERSION_URL + "/device/register";
//	public static String UPDATE_TOKEN			= VERSION_URL + "/update_token";
//	public static String LOGOUT					= VERSION_URL + "/logout";
//	
//	//User
//	public static String USER			= VERSION_URL + "/users";
//	
//	//Schedules
//	public static String SCHEDULES		= VERSION_URL + "/schedules";
//	
//	//Forms
//	public static String FORMS			= VERSION_URL + "/work_forms";
//	public static String FORM_VERSION	= VERSION_URL + "/form_version";
//	public static String UPLOAD			= VERSION_URL + "/schedules/upload_item";
//	
//	//APK
//	public static String APK		= VERSION_URL + "/apk";
	
	/**
	 * ============================ Passing param ===================================================== 
	 */
	
	public static String scheduleId = "scheduleId";

	public static final String LOADAFTERLOGIN = "load";
	public static final String LOADSCHEDULE = "load_schedule";

	/**
	 * ============================== WATERMARK CONFIG ===============================================
	 * */

	public static final int TEXT_SIZE_POTRAIT = 22;
	public static final int TEXT_SIZE_LANDSCAPE = 12;

	/**
	 * ============================= CHECKIN ACTIVITY =================================================
	 * */

	public static int STP_PREVENTIVE_WORKTYPE_ID = 1;
	public static int SAP_PREVENTIVE_WORKTYPE_ID = 26;
} 