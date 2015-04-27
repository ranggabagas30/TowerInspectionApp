package com.sap.inspection.model;

import android.database.sqlite.SQLiteStatement;
import android.os.Parcelable;
import android.util.Log;


public abstract class BaseModel implements Parcelable {
	
	protected void bindAndCheckNullString (SQLiteStatement statement,int index,String value){
		if (null == value)
			statement.bindNull(index);
		else
			statement.bindString(index, value);
	}
	
	protected void bindBooleanToInteger (SQLiteStatement statement,int index,boolean value){
			statement.bindLong(index, value?1:0);
	}
	
	protected void log(String tag,String text) {
		Log.e(tag, text);
	}
	
	protected void log(String text) {
		log(getClass().getName(), text);
	}
}