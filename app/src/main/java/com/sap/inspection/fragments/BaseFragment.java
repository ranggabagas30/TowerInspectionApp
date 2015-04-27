package com.sap.inspection.fragments;

import android.app.Activity;

import android.support.v4.app.Fragment;
import android.util.Log;

public abstract class BaseFragment extends Fragment{

	protected Activity activity;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = activity;
	}
	
	protected void log(String logString){
		log(getClass().getName(), logString);
	}
	
	protected void log(String tag, String logString){
		Log.d(tag, logString);
	}

}
