package com.sap.inspection.task;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.sap.inspection.MainActivity;
import com.sap.inspection.SettingActivity;
import com.sap.inspection.event.ScheduleProgressEvent;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.value.DbRepositoryValue;

import de.greenrobot.event.EventBus;

public class ScheduleSaver extends AsyncTask<Object,Integer,Void> {

	private MainActivity mainActivity;
	private Activity activity;
	
	public void setMainActivity(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
		this.activity = mainActivity;
	}
	
	public void setActivity(Activity activity){
		this.activity = activity;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		DbRepositoryValue.getInstance().open(activity);
		Log.d(getClass().getName(), "open db...");
	}

	@Override
	protected Void doInBackground(Object... params) {
		for (int i = 0; i < params.length; i++) {
			Log.d(getClass().getName(), "saving schedule : "+i+":"+(params.length - 1));
			publishProgress((i+1)*100/params.length);
			((ScheduleBaseModel)params[i]).save();
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		Log.d(getClass().getName(), "saving schedule "+values[0]+" %...");
		EventBus.getDefault().post(new ScheduleProgressEvent(values[0]));
		if (mainActivity != null)
			mainActivity.setProgressDialogMessage("schedule","saving schedule "+values[0]+" %...");
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		Log.d(getClass().getName(), "on post db...");
		DbRepositoryValue.getInstance().close();
		EventBus.getDefault().post(new ScheduleProgressEvent(100,true));
		if (mainActivity != null)
			mainActivity.setFlagScheduleSaved(true);
		if (activity != null)
			try {
				((SettingActivity)activity).hideDialog();
			} catch (Exception e) {
				e.printStackTrace();
		}
	}
}
