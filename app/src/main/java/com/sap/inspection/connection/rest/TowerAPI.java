package com.sap.inspection.connection.rest;

import com.sap.inspection.model.responsemodel.BaseResponseModel;
import com.sap.inspection.model.responsemodel.CheckinResponseModel;
import com.sap.inspection.model.responsemodel.CorrectiveScheduleResponseModel;
import com.sap.inspection.model.responsemodel.CreateScheduleFOCUTResponseModel;
import com.sap.inspection.model.responsemodel.DeviceRegistrationResponseModel;
import com.sap.inspection.model.responsemodel.FormResponseModel;
import com.sap.inspection.model.responsemodel.ScheduleResponseModel;
import com.sap.inspection.model.responsemodel.UserResponseModel;
import com.sap.inspection.model.responsemodel.VersionResponseModel;

import io.reactivex.Completable;
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
    @POST("schedules/{scheduleId}/check_in")
    Single<CheckinResponseModel> rxPostCheckin(
            @Path("scheduleId") String scheduleId,
            @Field("schedule_id") String schedule_id,
            @Field("site_id_customer") String siteIdCustomer,
            @Field("site_name") String siteName,
            @Field("period") String period,
            @Field("site_lat") String siteLat,
            @Field("site_long") String siteLong,
            @Field("current_lat") String currentLat,
            @Field("current_long") String currentLong,
            @Field("distance") String distance,
            @Field("time_checkin") String timeCheckin,
            @Field("status") String status,
            @Field("accuracy") String accuracy
    );


    @FormUrlEncoded
    @POST("register/fake_gps")
    Completable rxPostReportFakeGPS(
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

    @GET("form_imbas_petir")
    Single<FormResponseModel> rxGetWorkFormImbasPetir();
}
