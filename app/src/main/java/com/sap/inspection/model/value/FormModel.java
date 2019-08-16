package com.sap.inspection.model.value;

import android.os.Parcel;

import com.sap.inspection.model.BaseModel;
import com.sap.inspection.model.form.WorkFormRowModel;

import java.util.Vector;

public class FormModel extends BaseModel {

	public boolean isOpen;
	public boolean hasForm = false;
	public int level;
	public String text;
	public String position;
	public Vector<WorkFormRowModel> children;

	public int getCount(){
		int count = 0;
		if (isOpen && children != null){
			for (WorkFormRowModel child : children) {
				count += child.getCount();
			}
			count += children.size();
		}
		return count;
	}

	public Vector<WorkFormRowModel> getModels(){
		Vector<WorkFormRowModel> models = new Vector<WorkFormRowModel>();
		if (isOpen && children != null){
			for (WorkFormRowModel child : children) {
				models.add(child);
				models.addAll(child.getModels());
			}
		}
		return models;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
	}

}
