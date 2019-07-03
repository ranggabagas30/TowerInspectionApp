package com.sap.inspection.task;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.sap.inspection.BaseActivity;
import com.sap.inspection.MainActivity;
import com.sap.inspection.MyApplication;
import com.sap.inspection.R;
import com.sap.inspection.SettingActivity;
import com.sap.inspection.connection.APIHelper;
import com.sap.inspection.event.ScheduleProgressEvent;
import com.sap.inspection.model.DbRepository;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.value.DbRepositoryValue;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.PrefUtil;

import de.greenrobot.event.EventBus;

public class ScheduleSaver extends AsyncTask<Object,Integer,Void> {

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		DebugLog.d( "open db...");
	}

	@Override
	protected Void doInBackground(Object... params) {
		for (int i = 0; i < params.length; i++) {
			DebugLog.d( "saving schedule : "+i+":"+(params.length - 1));
			publishProgress((i+1)*100/params.length);
			((ScheduleBaseModel)params[i]).save();
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		DebugLog.d( "saving schedule "+values[0]+" %...");
		EventBus.getDefault().post(new ScheduleProgressEvent(values[0]));
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		DebugLog.d( "on post db...");
		EventBus.getDefault().post(new ScheduleProgressEvent(100,true));
	}
}
