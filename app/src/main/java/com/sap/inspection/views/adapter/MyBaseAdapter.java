package com.sap.inspection.views.adapter;

import android.util.Log;
import android.widget.BaseAdapter;

public abstract class MyBaseAdapter extends BaseAdapter {
	
	protected void log(String logString){
		log(getClass().getName(), logString);
	}
	
	protected void log(String tag, String logString){
		Log.d(tag, logString);
	}


}
