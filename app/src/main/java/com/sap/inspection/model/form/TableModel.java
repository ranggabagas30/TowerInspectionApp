package com.sap.inspection.model.form;

import android.content.Context;

import com.sap.inspection.model.BaseModel;

import org.parceler.Parcel;

import java.util.ArrayList;

@Parcel
public class TableModel extends BaseModel {

	public ArrayList<WorkFormColumnModel> headers;
	public ArrayList<WorkFormRowModel> rows;

	public void save(Context context){
		save();
	}

	public void save(){
		if (headers != null)
			for (WorkFormColumnModel header : headers) {
				header.save();
			}

		if (rows != null)
			for (WorkFormRowModel row : rows) {
				row.save();
			}
	}
	
	public int getInputCount(){
		int count = 0;
		if (rows != null)
			for (WorkFormRowModel row : rows) {
				count += row.getInputCount();
			}
		return count;
	}

}
