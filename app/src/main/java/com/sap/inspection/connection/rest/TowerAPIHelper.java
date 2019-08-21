package com.sap.inspection.connection.rest;

import com.sap.inspection.model.responsemodel.DeviceRegistrationResponseModel;
import com.sap.inspection.model.responsemodel.FormVersionResponseModel;

import io.reactivex.Observable;

public class TowerAPIHelper {

    public static Observable<FormVersionResponseModel> getFormVersion() {
        return TowerAPIClient.createService(TowerAPI.class).rxGetFormVersion();
    }

    public static Observable<DeviceRegistrationResponseModel> postRegisterFCMToken(String fcmToken, String deviceid, String appversion) {
        return TowerAPIClient.createService(TowerAPI.class).rxPostRegisterFCMToken(fcmToken, deviceid, appversion);
    }
}
