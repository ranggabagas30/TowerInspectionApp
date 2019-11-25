package com.sap.inspection.model;

import org.apache.http.NameValuePair;
import org.parceler.Parcel;

import java.util.ArrayList;

@Parcel
public class NotificationModel extends BaseModel {
    public String category;
    public String action;
    public String message;
    public String data;
    public String version;
    public String json;

    public NotificationModel() {}

	public ArrayList<NameValuePair> getData(){
		ArrayList<NameValuePair> nvps = new ArrayList<NameValuePair>();
		return nvps;
	}
	
}
