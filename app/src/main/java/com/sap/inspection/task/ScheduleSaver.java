package com.sap.inspection.task;

import android.app.Activity;
import android.os.AsyncTask;

import com.sap.inspection.BaseActivity;
import com.sap.inspection.MainActivity;
import com.sap.inspection.MyApplication;
import com.sap.inspection.SettingActivity;
import com.sap.inspection.event.ScheduleProgressEvent;
import com.sap.inspection.model.DbRepository;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.value.DbRepositoryValue;
import com.sap.inspection.tools.DebugLog;

import de.greenrobot.event.EventBus;

public class ScheduleSaver extends AsyncTask<Object,Integer,Void> {

	private MainActivity mainActivity;
	private Activity activity;
	
	public void setMainActivity(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}
	
	public void setActivity(Activity activity){
		this.activity = activity;
	}

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

		if (activity != null) {

			if (activity instanceof BaseActivity) ((BaseActivity) activity).showMessageDialog("saving schedule "+values[0]+" %...");
		}

		/*if (mainActivity != null)
		    mainActivity.showMessageDialog("saving schedule "+values[0]+" %...");*/
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		DebugLog.d( "on post db...");

		EventBus.getDefault().post(new ScheduleProgressEvent(100,true));

		if (activity != null) {
			if (activity instanceof MainActivity) {
				((MainActivity) activity).hideDialog();
				((MainActivity) activity).setFlagScheduleSaved(true);

			} else if (activity instanceof SettingActivity) {
				((SettingActivity) activity).hideDialog();
			}
		}
		/*if (mainActivity != null) {
			mainActivity.setFlagScheduleSaved(true);
		if (activity != null)
			try {
				if (activity instanceof SettingActivity)
					((SettingActivity)activity).hideDialog();
				else
					((MainActivity)activity).hideDialog();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}*/
	}
}
