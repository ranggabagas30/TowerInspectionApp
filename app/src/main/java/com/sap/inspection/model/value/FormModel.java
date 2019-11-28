package com.sap.inspection.model.value;

import com.sap.inspection.model.BaseModel;
import com.sap.inspection.model.form.WorkFormRowModel;

import org.parceler.Parcel;

import java.util.ArrayList;

@Parcel
public class FormModel extends BaseModel {

	public boolean isOpen;
	public boolean hasForm = false;
	public int level;
	public String text;
	public String position;
	public ArrayList<WorkFormRowModel> children;

	public FormModel() {}

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

	public ArrayList<WorkFormRowModel> getModels(){
		ArrayList<WorkFormRowModel> models = new ArrayList<WorkFormRowModel>();
		if (isOpen && children != null){
			for (WorkFormRowModel child : children) {
				models.add(child);
				models.addAll(child.getModels());
			}
		}
		return models;
	}

}
