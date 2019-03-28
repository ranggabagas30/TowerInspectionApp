package com.sap.inspection.model;

import android.database.sqlite.SQLiteStatement;
import android.os.Parcelable;


public abstract class BaseModel implements Parcelable {
	
	protected static void bindAndCheckNullString (SQLiteStatement statement,int index,String value){
		if (null == value)
			statement.bindNull(index);
		else
			statement.bindString(index, value);
	}
	
	protected static void bindBooleanToInteger (SQLiteStatement statement,int index,boolean value){
			statement.bindLong(index, value?1:0);
	}
}