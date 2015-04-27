package com.sap.inspection.model;

import android.os.Parcel;

public class ErrorSatutempatModel extends BaseModel {

	public String error_type;
	public int error_code;
	public String message;
	public String errors;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
	}

}
