package com.sap.inspection.model.value;

import android.os.Parcel;

import com.sap.inspection.model.BaseModel;
import com.sap.inspection.model.DbManager;

public class QueueModel extends BaseModel {
	
	public String scheduleId;
	public String itemId;
	public String createdAt;
	public String value;
	public boolean typePhoto;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
	}

	public static String createDB(){
		return "create table if not exists " + DbManager.mWorkFormGroup
				+ " (" + DbManager.colID + " varchar, "
				+ DbManager.colName + " varchar, "
				+ DbManager.colPosition + " varchar, "
				+ DbManager.colWorkFormId + " varchar, "
				+ DbManager.colDescription + " varchar, "
				+ DbManager.colIsTable + " integer, "
				+ DbManager.colAncestry + " varchar, "
				+ DbManager.colCreatedAt + " varchar, "
				+ DbManager.colUpdatedAt + " varchar, "
				+ "PRIMARY KEY (" + DbManager.colID + "))";
	}
}
