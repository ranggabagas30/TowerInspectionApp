package com.sap.inspection.model.responsemodel;

import com.sap.inspection.model.BaseModel;
import com.sap.inspection.model.PageModel;

import android.os.Parcel;


public class BaseResponseModel extends BaseModel {
	
	/**
	 * 
	 */
	public int status;
	public String status_code;
	public String messages;
	public PageModel page;
	
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
	}
}