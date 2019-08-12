package com.sap.inspection.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

public abstract class BaseFragment extends Fragment{

	protected Activity activity;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		if (context instanceof Activity) {
			this.activity = (Activity) context;
		}
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	protected void log(String logString){
		log(getClass().getName(), logString);
	}
	
	protected void log(String tag, String logString){
		Log.d(tag, logString);
	}

}
