package com.sap.inspection.manager;


import android.os.AsyncTask;

import com.sap.inspection.MyApplication;
import com.sap.inspection.event.DeleteAllScheduleEvent;
import com.sap.inspection.model.DbManager;
import com.sap.inspection.model.DbRepository;

import de.greenrobot.event.EventBus;

public class DeleteAllSchedules extends AsyncTask<Void, Integer, Void>{

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		DbRepository.getInstance().open(MyApplication.getInstance());
		DbRepository.getInstance().clearData(DbManager.mSchedule);
		DbRepository.getInstance().close();
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		EventBus.getDefault().post(new DeleteAllScheduleEvent());
	}
}
