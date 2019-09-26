package com.sap.inspection.model.responsemodel;

import android.os.Parcel;

import com.sap.inspection.model.BaseModel;

public class FormVersionResponseModel extends BaseModel {
    public String version;
    public String download;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
	}
}