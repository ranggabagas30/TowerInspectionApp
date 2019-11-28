package com.sap.inspection.connection.rest;

import com.sap.inspection.R;
import com.sap.inspection.model.responsemodel.BaseResponseModel;
import com.sap.inspection.model.responsemodel.CorrectiveScheduleResponseModel;
import com.sap.inspection.model.responsemodel.CreateScheduleFOCUTResponseModel;
import com.sap.inspection.model.responsemodel.DeviceRegistrationResponseModel;
import com.sap.inspection.model.responsemodel.FakeGPSResponseModel;
import com.sap.inspection.model.responsemodel.FormResponseModel;
import com.sap.inspection.model.responsemodel.ScheduleResponseModel;
import com.sap.inspection.model.responsemodel.UserResponseModel;
import com.sap.inspection.model.responsemodel.VersionResponseModel;
import com.sap.inspection.util.PrefUtil;

import io.reactivex.Single;

public class TowerAPIHelper {

    public static Single<VersionResponseModel> getAPKVersion() {
        return TowerAPIClient.createService(TowerAPI.class).rxGetAPKVersion();
    }

    public static Single<VersionResponseModel> getFormVersion() {
        return TowerAPIClient.createService(TowerAPI.class).rxGetFormVersion();
    }

    public static Single<DeviceRegistrationResponseModel> sendRegistrationFCMToken(String fcmToken, String deviceid, String appversion) {
        return TowerAPIClient.createService(TowerAPI.class).rxPostRegisterFCMToken(fcmToken, deviceid, appversion);
    }

    public static Single<UserResponseModel> login(String username, String password) {
        return TowerAPIClient.createService(TowerAPI.class).rxPostLogin(username, password);
    }

    public static Single<CreateScheduleFOCUTResponseModel> createScheduleFOCUT(String ttNumber, String workDate, String userId) {
        return TowerAPIClient.createService(TowerAPI.class).rxPostCreateScheduleFOCUT(ttNumber, workDate, userId);
    }

    public static Single<CorrectiveScheduleResponseModel> getCorrectiveSchedules() {
        String userId = PrefUtil.getStringPref(R.string.user_id, null);
        String template = "corrective";
        return TowerAPIClient.createService(TowerAPI.class).rxGetCorrectiveSchedules(userId, template);
    }

    public static Single<ScheduleResponseModel> downloadSchedules() {
        String userId = PrefUtil.getStringPref(R.string.user_id, null);
        String template = "full";
        return TowerAPIClient.createService(TowerAPI.class).rxGetSchedules(userId, template);
    }

    public static Single<BaseResponseModel> deleteSchedule(String scheduleId) {
        return TowerAPIClient.createService(TowerAPI.class).rxDeleteSchedule(scheduleId);
    }

    public static Single<BaseResponseModel> editSchedule(String oldTTNumber, String newTTNumber) {
        return TowerAPIClient.createService(TowerAPI.class).rxEditSchedule(oldTTNumber, newTTNumber);
    }

    public static Single<FakeGPSResponseModel> reportFakeGPS(String timeDetected, String appVersion, String message, String siteId) {
        return TowerAPIClient.createService(TowerAPI.class).rxPostReportFakeGPS(timeDetected, appVersion, message, siteId);
    }

    public static Single<FormResponseModel> downloadWorkForms() {
        String userId = PrefUtil.getStringPref(R.string.user_id, null);
        String template = "full";
        return TowerAPIClient.createService(TowerAPI.class).rxGetWorkForm(userId, template);
    }
}
