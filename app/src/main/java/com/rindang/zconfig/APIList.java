package com.rindang.zconfig;

public class APIList {

	//Account
	public static String loginUrl(){
		return AppConfig.getInstance().getV1()+"/login";
	}
	
	public static String fcmTokenRegeisterUrl(){
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

	//Checkin schedules
	public static String checkinScheduleUrl(String scheduleId) { return AppConfig.getInstance().getV1()+ "/schedules/" + scheduleId + "/check_in"; }

	//Item Schedules -- to obtain default value schedules
	public static String itemSchedulesUrl(String scheduleId, String userId) {
		return AppConfig.getInstance().getV1() + "/users/" + userId + "/item_schedules?schedule_id=" + scheduleId;
	}

	//Forms
	public static String formsUrl(){
		return AppConfig.getInstance().getV1()+"/work_forms";
	}

	//Form Imbas Petir
	public static String formImbasPetirUrl(){
	    return AppConfig.getInstance().getV1() + "/form_imbas_petir";
    }

    // Check Approve
    public static String checkApprove(String scheduleId) {
		return AppConfig.getInstance().getV1() + "/check_approve/" + scheduleId;
	}

	// Delete Warga by wargaId
	public static String deleteWarga(String wargaId) {
		return AppConfig.getInstance().getV1() + "/delete_warga/" + wargaId;
	}

	// Delete Barang by barangId
	public static String deleteBarang(String barangId) {
		return AppConfig.getInstance().getV1() + "/delete_barang/" + barangId;
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
