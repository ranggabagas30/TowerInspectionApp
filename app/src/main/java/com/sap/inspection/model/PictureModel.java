package com.sap.inspection.model;

import android.os.Parcel;

public class PictureModel extends BaseModel {

	public String small;
	public String medium;
	public String large;
	public String original;

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		// TODO Auto-generated method stub

	}

}
