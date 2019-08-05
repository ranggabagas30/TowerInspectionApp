package com.sap.inspection.model.responsemodel;

import android.os.Parcel;

import com.sap.inspection.model.BaseModel;

public class PageModel extends BaseModel {
    public int current;
    public int limit;
    public int total;
    public int records;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
	}
}