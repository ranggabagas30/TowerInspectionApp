package com.sap.inspection.model.responsemodel;

import android.os.Parcel;

public class DeviceRegistrationResponseModel extends BaseResponseModel {

    public String token;
    public String app_version;
    public boolean should_udpate;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }
}
