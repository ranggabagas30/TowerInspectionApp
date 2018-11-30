package com.arifariyan.baseassets.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

public abstract class BaseFragmentArchived extends Fragment{

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

	protected void log(String logString){
		log(getClass().getName(), logString);
	}
	
	protected void log(String tag, String logString){
		Log.d(tag, logString);
	}

}
