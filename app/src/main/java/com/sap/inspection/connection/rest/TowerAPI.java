package com.sap.inspection.connection.rest;

import com.sap.inspection.model.responsemodel.FormVersionResponseModel;

import io.reactivex.Observable;
import retrofit2.http.GET;

public interface TowerAPI {

    @GET("form_version")
    Observable<FormVersionResponseModel> rxGetFormVersion();
    

}
