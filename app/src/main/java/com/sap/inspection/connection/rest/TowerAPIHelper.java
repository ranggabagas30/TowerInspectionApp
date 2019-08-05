package com.sap.inspection.connection.rest;

import com.sap.inspection.model.responsemodel.FormVersionResponseModel;

import rx.Observable;

public class TowerAPIHelper {

    public static Observable<FormVersionResponseModel> getFormVersion() {
        return TowerAPIClient.createService(TowerAPI.class).rxGetFormVersion();
    }
}
