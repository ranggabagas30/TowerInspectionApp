package com.sap.inspection.constant;

import android.os.Environment;

import com.sap.inspection.MyApplication;
import com.sap.inspection.R;

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

	public static String KEY_USERID = "KEY_USERID";
	public static String KEY_SCHEDULEID = "KEY_SCHEDULEID";
	public static String KEY_SITEID = "KEY_SITEID";
	public static String KEY_ROWID = "KEY_ROWID";
	public static String KEY_WORKFORMGROUPID = "KEY_WORKFORMGROUPID";
	public static String KEY_DATAINDEX = "KEY_DATAINDEX";
	public static String KEY_WARGAID = "KEY_WARGAID";
	public static String KEY_BARANGID = "KEY_BARANGID";
	public static String KEY_PARENTID = "KEY_PARENTID";
	public static String KEY_WORKTYPEID = "KEY_WORKTYPEID";
	public static String KEY_WORKTYPENAME = "KEY_WORKTYPENAME";
	public static String KEY_DAYDATE = "KEY_DAYDATE";
	public static String KEY_WORKFORMGROUPNAME = "KEY_WORKFORMGROUPNAME";
	public static String KEY_SCHEDULEBASEMODEL = "KEY_SCHEDULEBASEMODEL";

	public static final String LOADAFTERLOGIN = "load";
	public static final String LOADSCHEDULE = "load_schedule";

	/**
	 * ============================== WATERMARK CONFIG ===============================================
	 * */

	public static final int TEXT_SIZE_POTRAIT = 12;
	public static final int TEXT_SIZE_LANDSCAPE = 18;
	public static final int TEXT_LINE_SPACE_POTRAIT = 30;
	public static final int TEXT_LINE_SPACE_LANDSCAPE = 36;

	/**
	 * ============================= CHECKIN ACTIVITY =================================================
	 * */

	// regex
	public static final String regexCHECKLIST = "(.*)CHECKLIST(.*)";
	public static final String regexSITEINFORMATION = "(.*)SITE INFORMATION(.*)";
	public static final String regexPREVENTIVE = "(.*)PREVENTIVE(.*)";

	/**
	 * ============================= REQUEST CODE =================================================
	 * */

	public static final int RC_ALL_PERMISSION = 0;
	public static final int RC_STORAGE_PERMISSION = 1;
	public static final int RC_READ_PHONE_STATE = 2;
	public static final int RC_LOCATION_PERMISSION = 3;
	public static final int RC_CAMERA = 4;

	public static final int RC_INSTALL_APK = 101;

	/**
	 * ============================= GENERAL CONSTANTS =================================================
	 * */

	public static final String APPLICATION_SAP = "sap";
	public static final String APPLICATION_STP = "stp";

	public static final String EMPTY = "EMPTY";

	public static final String FOLDER_CAMERA = "Camera";
	public static final String FOLDER_TOWER_INSPECTION = ".TowerInspection";
	public static final String DIR_PHOTOS = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + Constants.FOLDER_CAMERA + "/" + Constants.FOLDER_TOWER_INSPECTION;
	/**
	 * ============================= PHOTO STATUS =================================================
	 * */

	public static final String OK = "OK";
	public static final String NOK = "NOK";
	public static final String NA = "NA";

	/**
	 * ============================= FORM IMBAS PETIR =================================================
	 * */

	public static final String regexIMBASPETIR = "(.*)IMBAS PETIR(.*)";
	public static final String regexWarga = "Warga";
	public static final String regexTambah = "Tambah";
	public static final String regexId = "Id-";
	public static final String regexWargaId	= "Warga Id-";
	public static final String regexBarangId = "Barang Id-";
	public static final String regexBeritaAcaraClosing = "Berita Acara Closing";
	public static final String regexBeritaAcaraPenghancuran = "Berita Acara Penghancuran";
	public static final String regexKwitansi = "Kwitansi";
	public static final String regexPhotoPenghancuran = "Photo Penghancuran";
} 