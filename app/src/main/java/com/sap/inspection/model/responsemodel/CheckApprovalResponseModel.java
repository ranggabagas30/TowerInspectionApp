package com.sap.inspection.model.responsemodel;

import com.sap.inspection.tools.DebugLog;

public class CheckApprovalResponseModel extends BaseResponseModel {

    public String respond_in;

    public void printLogResponse() {
        DebugLog.d("--- Check Approval Response ---\n");
        DebugLog.d("{");
        DebugLog.d("\t status : " + status);
        DebugLog.d("\t status_code : " + status_code);
        DebugLog.d("\t messages : " + messages);
        DebugLog.d("\t respond_in : " + respond_in);
        DebugLog.d("},");
    }
}
