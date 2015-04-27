package com.sap.inspection.model.form;

import android.os.Parcel;
import android.view.View;
import android.widget.TextView;

import com.sap.inspection.model.BaseModel;

public class ItemUpdateResultViewModel extends BaseModel {
	
	public TextView colored;
	public TextView plain;
	public View backGround;
	public int sumTask = 0;
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

	}
	
	public int getPercentage(int taskDone){
		if (sumTask != 0)
			return 100 * taskDone / sumTask;
		return 0;
	}

}
