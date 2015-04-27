package com.sap.inspection.dblisting;

import java.util.Vector;

import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.ScheduleGeneral;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.AsyncTaskLoader;

public class ScheduleListing extends AsyncTaskLoader<Vector<ScheduleBaseModel>>{

	//type of the published values
	public static int MSGCODE_PROGRESS = 1;
	public static int MSGCODE_MESSAGE = 2;
	
	
	private Handler handler;

	public ScheduleListing(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Vector<ScheduleBaseModel> loadInBackground() {
		ScheduleGeneral scheduleGeneral = new ScheduleGeneral();
		return scheduleGeneral.getListScheduleForScheduleAdapter(scheduleGeneral.getAllSchedule(getContext()));
		
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	/**
	 * Helper to publish string value
	 * @param value
	 */
	protected void publishMessage(String value){

		if(handler!=null){

			Bundle data = new Bundle();
			data.putString("message", value);

			/* Creating a message */
			Message msg = new Message();
			msg.setData(data);
			msg.what = MSGCODE_MESSAGE; 

			/* Sending the message */
			handler.sendMessage(msg);

		}

	}

	/**
	 * Helper to publish string value
	 * @param value
	 */
	protected void publishProgress(int value){

		if(handler!=null){

			/* Creating a message */
			Message msg = new Message();
			msg.what = MSGCODE_PROGRESS; 
			msg.arg1 = value;

			/* Sending the message */
			handler.sendMessage(msg);

		}

	}

}
