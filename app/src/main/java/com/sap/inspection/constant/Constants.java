package com.sap.inspection.constant;

import android.os.Environment;

import com.sap.inspection.BuildConfig;
import com.sap.inspection.R;
import com.sap.inspection.util.PrefUtil;

import java.io.File;

public class Constants {

	/**
	 * ============================= GENERAL CONSTANTS =================================================
	 * */
	public static final String APPLICATION_FILE_PROVIDER = BuildConfig.APPLICATION_ID + ".fileProvider";
	public static final String APPLICATION_VERSION = BuildConfig.VERSION_NAME;
	public static final String APPLICATION_SAP = "sap";
	public static final String APPLICATION_STP = "stp";
	public static final String EMPTY = "EMPTY";
	public static final String[] DAYS 		= {"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
	public static final String[] MONTHS 	= {"Januari","Februari","Maret","April","Mei","Juni","Juli","Agustus","September","Oktober","November","Desember"};
	public static final String DATETIME_PATTERN1 = "dd:MM:yyyy HH:mm:ss";
	public static final String DATETIME_PATTERN2 = "yyyy-MM-dd HH:mm:ss";
	public static final String DATETIME_PATTERN3 = "yyyy-MM-dd";

	/**
	 * ============================ ACTIVITY MAPPING ===================================================== 
	 */

	public static final int CALLENDAR_ACTIVITY = 101;
	public static final int DEFAULT_REQUEST_CODE 		= 311;
	public static final String JSON_CONTENT_TYPE = "application/json";
	
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
	public static String KEY_CHECKINDURATION = "KEY_CHECKINDURATION";

	public static final String LOADAFTERLOGIN = "load";
	public static final String LOADSCHEDULE = "load_schedule";

	/**
	 * ============================ PREF KEYS =====================================================
	 */

	public static final String KEY_LATEST_APK_VERSION = "latest_apk_version";
	public static final String KEY_LATEST_FORM_VERSION = "latest_form_version";
	public static final String KEY_APK_UPDATE_URL = "apk_update_url";

	/**
	 * ============================== WATERMARK CONFIG ===============================================
	 * */

	public static final int TEXT_SIZE_POTRAIT = 12;
	public static final int TEXT_SIZE_LANDSCAPE = 18;
	public static final int TEXT_LINE_SPACE_POTRAIT = 30;
	public static final int TEXT_LINE_SPACE_LANDSCAPE = 36;
	public static final int HEIGHT_BACKGROUND_WATERMARK_PORTRAIT = 170;
	public static final int HEIGHT_BACKGROUND_WATERMARK_LANDSCAPE = 300;

	/**
	 * ============================= CHECKIN ACTIVITY =================================================
	 * */

	// regex
	public static final String regexCHECKLIST = "(.*)CHECKLIST(.*)";
	public static final String regexSITEINFORMATION = "(.*)SITE INFORMATION(.*)";
	public static final String regexPREVENTIVE = "(.*)PREVENTIVE(.*)";
	public static final String regexFOCUT = "(.*)FO CUT(.*)";

	/**
	 * ============================= REQUEST CODE =================================================
	 * */
	public static final int RC_ALL_PERMISSION = 0;
	public static final int RC_STORAGE_PERMISSION = 1;
	public static final int RC_READ_PHONE_STATE = 2;
	public static final int RC_LOCATION_PERMISSION = 3;
	public static final int RC_CAMERA_PERMISSION = 4;
	public static final int RC_INSTALL_APK = 101;
	public static final int RC_TAKE_PHOTO = 102;
	public static final int RC_CHANGE_DATE_TIME_SETTING = 103;
	public static final int RC_PLAY_SERVICES_RESOLUTION_REQUEST = 104;

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


	/**
	 * ============================= GLOBAL URL/URI =================================================
	 * */
	public static final String TOWER_PHOTOS_DIR = BuildConfig.FOLDER_TOWER_INSPECTION;
	public static final String TOWER_PHOTOS_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator + BuildConfig.FOLDER_CAMERA + File.separator + TOWER_PHOTOS_DIR;
	public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory() + File.separator + "Download";
	public static final String APK_FULL_PATH = DOWNLOAD_PATH + File.separator + "sapInspection" + PrefUtil.getStringPref(R.string.latest_apk_version, "")+".apk";
	public static final String APK_URL = PrefUtil.getStringPref(R.string.apk_update_url, "");

} 