package com.sap.inspection.model.responsemodel;

import com.sap.inspection.model.BaseModel;
import com.sap.inspection.model.PageModel;

import org.parceler.Parcel;

@Parcel
public class BaseResponseModel extends BaseModel {

	public int status;
	public String status_code;
	public String messages;
	public PageModel page;

	public BaseResponseModel(){}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("-> RESPONSE").append("\n");
		s.append("status: " + status).append("\n");
		s.append("status_code: " + status_code).append("\n");
		s.append("messages: " + messages).append("\n");
		return new String(s);
	}
}