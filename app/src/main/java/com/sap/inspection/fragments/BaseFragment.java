package com.sap.inspection.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.sap.inspection.MyApplication;
import com.sap.inspection.model.DbRepository;
import com.sap.inspection.model.value.DbRepositoryValue;

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
		DbRepository.getInstance().open(MyApplication.getInstance());
		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
		DbRepository.getInstance().close();
		DbRepositoryValue.getInstance().close();
	}

	protected void log(String logString){
		log(getClass().getName(), logString);
	}
	
	protected void log(String tag, String logString){
		Log.d(tag, logString);
	}

}
