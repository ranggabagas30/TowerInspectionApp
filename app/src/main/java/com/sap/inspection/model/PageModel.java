package com.sap.inspection.model;

import org.parceler.Parcel;

@Parcel
public class PageModel extends BaseModel {
    public int current;
    public int limit;
    public int total;
    public int records;

	public PageModel() {}
}