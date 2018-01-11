package com.rindang.zconfig;

public class APIList {

	//Account
	public static String loginUrl(){
		return AppConfig.getInstance().getV1()+"/login";
	}
	
	public static String gcmTokenRegeisterUrl(){
		return AppConfig.getInstance().getV1()+"/device/register";
	}
	
	public static String updateTokenUrl(){
		return AppConfig.getInstance().getV1()+"/update_token";
	}
	
	public static String logoutUrl(){
		return AppConfig.getInstance().getV1()+"/logout";
	}

	//User
	public static String userUrl(){
		return AppConfig.getInstance().getV1()+"/users";
	}

	//Schedules
	public static String schedulesUrl(){
		return AppConfig.getInstance().getV1()+"/schedules";
	}

	//Forms
	public static String formsUrl(){
		return AppConfig.getInstance().getV1()+"/work_forms";
	}

	//FormGroup
	public static String formGroupUrl(){
		return AppConfig.getInstance().getV1()+"/work_form_groups";
	}

	public static String formVersionUrl(){
		return AppConfig.getInstance().getV1()+"/form_version";
	}
	
	public static String uploadUrl(){
		return AppConfig.getInstance().getV1()+"/schedules/upload_item";
	}

	public static String uploadStatusUrl() {
		return AppConfig.getInstance().getV1()+"/schedules/upload_status";
	}

	//APK
	public static String apkUrl(){
		return AppConfig.getInstance().getV1()+"/apk";
	}

	//Confirm
	public static String uploadConfirmUrl(){
		return AppConfig.getInstance().getV1()+"/corrective/";
	}

}
