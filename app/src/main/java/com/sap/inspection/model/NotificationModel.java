package com.sap.inspection.model;

import android.os.Parcel;

import org.apache.http.NameValuePair;

import java.util.ArrayList;

public class NotificationModel extends BaseModel {
	
    public String category;
    public String action;
    public String message;
    public String data;
    public String version;
    public String json;

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		// TODO Auto-generated method stub
	}
	
	public ArrayList<NameValuePair> getData(){
		ArrayList<NameValuePair> nvps = new ArrayList<NameValuePair>();
		return nvps;
	}
	
}
