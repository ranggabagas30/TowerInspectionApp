package com.sap.inspection.model.responsemodel;

import org.parceler.Parcel;

@Parcel
public class DeviceRegisterResponseModel extends BaseResponseModel {
    public String token;
    public String app_version;
    public boolean should_update;

    public DeviceRegisterResponseModel() {}

    @Override
    public String toString() {

        StringBuilder response = new StringBuilder("\n== Device Register Response==\n");
        response.append("{\n");
        response.append("\tstatus\t:\t" + status + ",\n");
        response.append("\ttoken\t:\t" + token + ",\n");
        response.append("\tapp_version\t:\t" + app_version + ",\n");
        response.append("\tmessage\t:\t" + messages + ",\n");
        response.append("\tshould_update\t:\t" + should_update + "\n");
        response.append("}");
        return new String(response);
    }
}
