package com.sap.inspection.model.responsemodel;

import com.sap.inspection.model.BaseModel;

import org.parceler.Parcel;

@Parcel
public class VersionResponseModel extends BaseModel {
    public String version;
    public String download;

    public VersionResponseModel(){}
}