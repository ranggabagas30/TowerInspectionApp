package com.sap.inspection.model;

import android.os.Parcel;

import com.sap.inspection.tools.DebugLog;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;

public class CheckinDataModel extends BaseModel {
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    /* contants for parameter field */
    public static final String FIELD_SCHEDULE_ID            = "schedule_id";
    public static final String FIELD_SITE_ID_CUSTOMER       = "site_id_customer";
    public static final String FIELD_SITE_NAME              = "site_name";
    public static final String FIELD_PERIOD                 = "period";
    public static final String FIELD_SITE_LAT               = "site_lat";
    public static final String FIELD_SITE_LONG              = "site_long";
    public static final String FIELD_CURRENT_LAT            = "current_lat";
    public static final String FIELD_CURRENT_LONG           = "current_long";
    public static final String FIELD_DISTANCE               = "distance";
    public static final String FIELD_TIME                   = "time";
    public static final String FIELD_STATUS                 = "status";
    public static final String FIELD_ACCURACY               = "accuracy";

    private int scheduleId;
    private String siteIdCustomer;
    private String siteName;
    private String period;
    private String siteLat;
    private String siteLong;
    private String currentLat;
    private String currentLong;
    private float distance;
    private String time;
    private String status;
    private float accuracy;

    private ArrayList<NameValuePair> paramNameValuePair;

    public CheckinDataModel() {
    }

    public int getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getSiteIdCustomer() {
        return siteIdCustomer;
    }

    public void setSiteIdCustomer(String siteIdCustomer) {
        this.siteIdCustomer = siteIdCustomer;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getSiteLat() {
        return siteLat;
    }

    public void setSiteLat(String siteLat) {
        this.siteLat = siteLat;
    }

    public String getSiteLong() {
        return siteLong;
    }

    public void setSiteLong(String siteLong) {
        this.siteLong = siteLong;
    }

    public String getCurrentLat() {
        return currentLat;
    }

    public void setCurrentLat(String currentLat) {
        this.currentLat = currentLat;
    }

    public String getCurrentLong() {
        return currentLong;
    }

    public void setCurrentLong(String currentLong) {
        this.currentLong = currentLong;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public void compileParamNameValuePair() {
        DebugLog.d("compiling param to NameValuePair");

        paramNameValuePair = new ArrayList<>();
        paramNameValuePair.add(new BasicNameValuePair(FIELD_SCHEDULE_ID, String.valueOf(this.scheduleId)));
        paramNameValuePair.add(new BasicNameValuePair(FIELD_SITE_ID_CUSTOMER, String.valueOf(this.siteIdCustomer)));
        paramNameValuePair.add(new BasicNameValuePair(FIELD_SITE_NAME, this.siteName));
        paramNameValuePair.add(new BasicNameValuePair(FIELD_PERIOD, String.valueOf(this.period)));
        paramNameValuePair.add(new BasicNameValuePair(FIELD_SITE_LAT, this.siteLat));
        paramNameValuePair.add(new BasicNameValuePair(FIELD_SITE_LONG, this.siteLong));
        paramNameValuePair.add(new BasicNameValuePair(FIELD_CURRENT_LAT, this.currentLat));
        paramNameValuePair.add(new BasicNameValuePair(FIELD_CURRENT_LONG, this.currentLong));
        paramNameValuePair.add(new BasicNameValuePair(FIELD_DISTANCE, String.valueOf(this.distance)));
        paramNameValuePair.add(new BasicNameValuePair(FIELD_TIME, this.time));
        paramNameValuePair.add(new BasicNameValuePair(FIELD_STATUS, this.status));
        paramNameValuePair.add(new BasicNameValuePair(FIELD_ACCURACY, String.valueOf(this.accuracy)));
    }

    public ArrayList<NameValuePair> getParamNameValuePair() {
        return paramNameValuePair;
    }
}
