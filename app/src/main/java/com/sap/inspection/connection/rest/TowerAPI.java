package com.sap.inspection.connection.rest;

import com.sap.inspection.model.responsemodel.BaseResponseModel;
import com.sap.inspection.model.responsemodel.CorrectiveScheduleResponseModel;
import com.sap.inspection.model.responsemodel.CreateScheduleFOCUTResponseModel;
import com.sap.inspection.model.responsemodel.DeviceRegistrationResponseModel;
import com.sap.inspection.model.responsemodel.FakeGPSResponseModel;
import com.sap.inspection.model.responsemodel.FormResponseModel;
import com.sap.inspection.model.responsemodel.ScheduleResponseModel;
import com.sap.inspection.model.responsemodel.UserResponseModel;
import com.sap.inspection.model.responsemodel.VersionResponseModel;

import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TowerAPI {

    @GET("apk")
    Single<VersionResponseModel> rxGetAPKVersion();

    @GET("form_version")
    Single<VersionResponseModel> rxGetFormVersion();

    // using UTF-8 encode
    @FormUrlEncoded
    @POST("device/register")
    Single<DeviceRegistrationResponseModel> rxPostRegisterFCMToken(
            @Field("token") String fcmtoken,
            @Field("device_id") String deviceid,
            @Field("version_app") String appversion
    );

    @FormUrlEncoded
    @POST("login")
    Single<UserResponseModel> rxPostLogin(
            @Field("username") String username,
            @Field("password") String password
    );


    @FormUrlEncoded
    @POST("schedule/create/fo_cut")
    Single<CreateScheduleFOCUTResponseModel> rxPostCreateScheduleFOCUT(
            @Field("tt_number") String ttNumber, // text
            @Field("work_date") String workDate, // yyyy-MM-dd
            @Field("user_id") String userId
    );

    @GET("users/{userId}/corrective_schedules")
    Single<CorrectiveScheduleResponseModel> rxGetCorrectiveSchedules(
            @Path("userId") String userId,
            @Query("template") String template
    );

    @GET("users/{userId}/schedules")
    Single<ScheduleResponseModel> rxGetSchedules(
            @Path("userId") String userId,
            @Query("template") String template
    );

    @FormUrlEncoded
    @POST("delete/schedule")
    Single<BaseResponseModel> rxDeleteSchedule(
            @Field("schedule_id") String scheduleId
    );

    @FormUrlEncoded
    @POST("edit/ticket")
    Single<BaseResponseModel> rxEditSchedule(
            @Field("tt_number") String oldTTNumber,
            @Field("tt_number_new") String newTTNumber
    );

    @FormUrlEncoded
    @POST("register/fake_gps")
    Single<FakeGPSResponseModel> rxPostReportFakeGPS(
            @Field("time_detected") String timeDetected,
            @Field("app_version") String appVersion,
            @Field("message") String message,
            @Field("site_id") String siteId
    );

    @GET("work_forms")
    Single<FormResponseModel> rxGetWorkForm(
            @Query("user_id") String userId,
            @Query("template") String template
    );
}
