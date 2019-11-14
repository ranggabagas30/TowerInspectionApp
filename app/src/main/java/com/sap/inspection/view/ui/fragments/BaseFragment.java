package com.sap.inspection.view.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import io.reactivex.disposables.CompositeDisposable;

public abstract class BaseFragment extends Fragment{

	protected Activity activity;
	protected CompositeDisposable compositeDisposable;

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
		compositeDisposable = new CompositeDisposable();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		compositeDisposable.clear();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		compositeDisposable.dispose();
	}

	protected void log(String logString){
		log(getClass().getName(), logString);
	}
	
	protected void log(String tag, String logString){
		Log.d(tag, logString);
	}

}
