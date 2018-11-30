package com.sap.inspection.model.form;

import java.util.Vector;

import android.content.Context;
import android.os.Parcel;

import com.sap.inspection.MyApplication;
import com.sap.inspection.model.BaseModel;
import com.sap.inspection.model.DbRepository;

public class TableModel extends BaseModel {

	public Vector<ColumnModel> headers;
	public Vector<RowModel> rows;

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	public void save(Context context){
		if (!DbRepository.getInstance().getDB().isOpen())
			DbRepository.getInstance().open(MyApplication.getInstance());
		save();
		DbRepository.getInstance().close();
	}

	public void save(){
		if (headers != null)
			for (ColumnModel header : headers) {
				header.save();
			}

		if (rows != null)
			for (RowModel row : rows) {
				row.save();
			}
	}
	
	public int getInputCount(){
		int count = 0;
		if (rows != null)
			for (RowModel row : rows) {
				count += row.getInputCount();
			}
		return count;
	}

}
