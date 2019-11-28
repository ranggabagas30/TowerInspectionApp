package com.sap.inspection.task;

import android.app.Activity;
import android.os.AsyncTask;

import com.sap.inspection.view.ui.MainActivity;
import com.sap.inspection.view.ui.SettingActivity;
import com.sap.inspection.event.ScheduleTempProgressEvent;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.tools.DebugLog;

import de.greenrobot.event.EventBus;

public class ScheduleTempSaver extends AsyncTask<Object,Integer,Void> {

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
		//DbRepositoryValue.getInstance().open(activity);
		DebugLog.d("open db...");
	}

	@Override
	protected Void doInBackground(Object... params) {
		for (int i = 0; i < params.length; i++) {
			DebugLog.d("saving schedule : "+i+":"+(params.length - 1));
			publishProgress((i+1)*100/params.length);
			((ScheduleBaseModel)params[i]).save();
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		DebugLog.d("saving schedule "+values[0]+" %...");
		EventBus.getDefault().post(new ScheduleTempProgressEvent(values[0]));
		if (mainActivity != null)
			mainActivity.showMessageDialog("saving schedule "+values[0]+" %...");
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		DebugLog.d("on post db...");
		EventBus.getDefault().post(new ScheduleTempProgressEvent(100,true));
		if (activity != null) {
			try {
				if (activity instanceof SettingActivity)
				    ((SettingActivity)activity).hideDialog();
				else
					((MainActivity)activity).hideDialog();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
