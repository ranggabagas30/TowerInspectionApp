package com.sap.inspection.connection.rest;

import com.sap.inspection.model.responsemodel.BaseResponseModel;
import com.sap.inspection.model.responsemodel.CreateScheduleFOCUTResponseModel;
import com.sap.inspection.model.responsemodel.DeviceRegistrationResponseModel;
import com.sap.inspection.model.responsemodel.FormVersionResponseModel;
import com.sap.inspection.model.responsemodel.ScheduleResponseModel;
import com.sap.inspection.model.responsemodel.UserResponseModel;

import io.reactivex.Observable;
import io.reactivex.Single;

public class TowerAPIHelper {

    public static Observable<FormVersionResponseModel> getFormVersion() {
        return TowerAPIClient.createService(TowerAPI.class).rxGetFormVersion();
    }

    public static Observable<DeviceRegistrationResponseModel> sendRegistrationFCMToken(String fcmToken, String deviceid, String appversion) {
        return TowerAPIClient.createService(TowerAPI.class).rxPostRegisterFCMToken(fcmToken, deviceid, appversion);
    }

    public static Observable<UserResponseModel> login(String username, String password) {
        return TowerAPIClient.createService(TowerAPI.class).rxPostLogin(username, password);
    }

    public static Single<CreateScheduleFOCUTResponseModel> createScheduleFOCUT(String ttNumber, String workDate, String userId) {
        return TowerAPIClient.createService(TowerAPI.class).rxPostCreateScheduleFOCUT(ttNumber, workDate, userId);
    }

    public static Single<BaseResponseModel> deleteSchedule(String scheduleId) {
        return TowerAPIClient.createService(TowerAPI.class).rxDeleteSchedule(scheduleId);
    }

    public static Single<BaseResponseModel> editSchedule(String oldTTNumber, String newTTNumber) {
        return TowerAPIClient.createService(TowerAPI.class).rxEditSchedule(oldTTNumber, newTTNumber);
    }
}
