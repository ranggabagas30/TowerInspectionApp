package com.sap.inspection.connection.rest;

import com.sap.inspection.model.responsemodel.FormVersionResponseModel;

import retrofit2.http.GET;
import rx.Observable;

public interface TowerAPI {

    @GET("form_version")
    Observable<FormVersionResponseModel> rxGetFormVersion();
    

}
