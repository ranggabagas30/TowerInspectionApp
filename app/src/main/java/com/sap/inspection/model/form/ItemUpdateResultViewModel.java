package com.sap.inspection.model.form;

import android.widget.TextView;

import com.sap.inspection.model.BaseModel;

import org.parceler.Parcel;
import org.parceler.Transient;

@Parcel
public class ItemUpdateResultViewModel extends BaseModel {

	@Transient
	public TextView colored;

	@Transient
	public TextView plain;

	public int sumTask = 0;

	public ItemUpdateResultViewModel() {}

	public int getPercent(int taskDone){
		if (sumTask != 0)
			return 100 * taskDone / sumTask;
		return 0;
	}

}
