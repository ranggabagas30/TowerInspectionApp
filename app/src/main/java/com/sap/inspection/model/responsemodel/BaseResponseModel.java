<<<<<<< HEAD
package com.sap.inspection.model.responsemodel;

import android.os.Parcel;

import com.sap.inspection.model.BaseModel;
import com.sap.inspection.model.PageModel;


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

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("{");
		stringBuilder.append("\tstatus: " + status);
		stringBuilder.append("\tstatus_code: " + status_code);
		stringBuilder.append("\tmessage: " + messages);
		stringBuilder.append("}");
		return new String(stringBuilder);
	}
=======
package com.sap.inspection.model.responsemodel;

import android.os.Parcel;

import com.sap.inspection.model.BaseModel;
import com.sap.inspection.model.PageModel;


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

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("-> RESPONSE").append("\n");
		s.append("status: " + status).append("\n");
		s.append("status_code: " + status_code).append("\n");
		s.append("messages: " + messages).append("\n");
		return new String(s);
	}
>>>>>>> currentwork-sap
}