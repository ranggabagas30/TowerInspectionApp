package com.sap.inspection.connection.rest;

import com.sap.inspection.model.responsemodel.DeviceRegistrationResponseModel;
import com.sap.inspection.model.responsemodel.FormVersionResponseModel;
import com.sap.inspection.model.responsemodel.ScheduleResponseModel;
import com.sap.inspection.model.responsemodel.UserResponseModel;

import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface TowerAPI {

    @GET("form_version")
    Observable<FormVersionResponseModel> rxGetFormVersion();

    // using UTF-8 encode
    @FormUrlEncoded
    @POST("device/register")
    Observable<DeviceRegistrationResponseModel> rxPostRegisterFCMToken(
            @Field("token") String fcmtoken,
            @Field("device_id") String deviceid,
            @Field("version_app") String appversion
    );

    @FormUrlEncoded
    @POST("login")
    Observable<UserResponseModel> rxPostLogin(
            @Field("username") String username,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("schedule/create/fo_cut")
    Single<ScheduleResponseModel> rxPostCreateScheduleFOCUT(
            @Field("tt_number") String ttNumber, // text
            @Field("work_date") String workDate, // yyyy-MM-dd
            @Field("user_id") String userId
    );
}
