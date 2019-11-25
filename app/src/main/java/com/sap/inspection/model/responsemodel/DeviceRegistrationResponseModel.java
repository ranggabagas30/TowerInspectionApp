package com.sap.inspection.model.responsemodel;

import org.parceler.Parcel;

@Parcel
public class DeviceRegistrationResponseModel extends BaseResponseModel {

    public String token;
    public String app_version;
    public boolean should_udpate;

    public DeviceRegistrationResponseModel() {}
}
