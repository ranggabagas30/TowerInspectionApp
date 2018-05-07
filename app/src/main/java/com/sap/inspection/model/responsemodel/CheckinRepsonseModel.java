package com.sap.inspection.model.responsemodel;

import android.os.Debug;

import com.sap.inspection.tools.DebugLog;

public class CheckinRepsonseModel extends BaseResponseModel {
    public Data data;
    public class Data{
        public int id;
        public int schedule_id;
        public int site_id_customer;
        public float distance;
        public String site_name;
        public String period;
        public String site_lat;
        public String site_long;
        public String current_lat;
        public String current_long;
        public String time_checkin;
        public String status;
    }
    public String respond_in;

    public void printLogResponse() {
        DebugLog.d("--- checkin response data from server ---\n");
        DebugLog.d("data : {");
        DebugLog.d("\t id : " + data.id);
        DebugLog.d("\t schedule_id : " + data.schedule_id);
        DebugLog.d("\t site_id_customer : " + data.site_id_customer);
        DebugLog.d("\t distance : " + data.distance);
        DebugLog.d("\t site_name : " + data.site_name);
        DebugLog.d("\t period : " + data.period);
        DebugLog.d("\t site_lat : " + data.site_lat);
        DebugLog.d("\t site_long : " + data.site_long);
        DebugLog.d("\t current_lat : " + data.current_lat);
        DebugLog.d("\t current_long : " + data.current_long);
        DebugLog.d("\t time_checkin : " + data.time_checkin);
        DebugLog.d("\t status : " + data.status);
        DebugLog.d("},");
        DebugLog.d("respond_in : " + respond_in);
    }
}
