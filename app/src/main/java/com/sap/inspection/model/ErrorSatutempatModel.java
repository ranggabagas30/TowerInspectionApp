package com.sap.inspection.model;

import org.parceler.Parcel;

@Parcel
public class ErrorSatutempatModel extends BaseModel {
	public String error_type;
	public int error_code;
	public String message;
	public String errors;

	public ErrorSatutempatModel() {}
}
