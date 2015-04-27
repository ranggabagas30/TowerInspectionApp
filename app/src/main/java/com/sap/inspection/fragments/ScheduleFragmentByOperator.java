package com.sap.inspection.fragments;

import java.util.Vector;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.sap.inspection.CallendarActivity;
import com.sap.inspection.FormActivity;
import com.sap.inspection.MainActivity;
import com.sap.inspection.R;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.ScheduleGeneral;
import com.sap.inspection.views.adapter.ScheduleAdapter;

public class ScheduleFragmentByOperator extends BaseListTitleFragment implements OnItemClickListener{
	private ScheduleAdapter adapter;
	private Vector<ScheduleBaseModel> models;
	private int filterBy = 0;
	

	public static ScheduleFragmentByOperator newInstance() {
		ScheduleFragmentByOperator fragment = new ScheduleFragmentByOperator();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new ScheduleAdapter(activity);
	}

	@Override
	public void onCreateView(LayoutInflater inflater) {
		super.onCreateView(inflater);
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
		actionRight.setVisibility(View.VISIBLE);
		actionRight.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(activity, CallendarActivity.class);
				intent.putExtra("filterBy", filterBy);
				startActivityForResult(intent,MainActivity.REQUEST_CODE);
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		log("==== activity result ==== "+requestCode+" "+resultCode);
		if (requestCode == MainActivity.REQUEST_CODE){
			switch (resultCode) {
			case Constants.CALLENDAR_ACTIVITY:
				scrollTo(data.getExtras().getString("date"));
				break;

			default:
				break;
			}
		}
	}
	
	@Override
	public String getTitle() {
		return "Schedule";
	}

	public void setScheduleBy(int resId){
		filterBy = resId;
		if (resId == R.string.schedule){
			ScheduleGeneral scheduleGeneral = new ScheduleGeneral();
			models = scheduleGeneral.getListScheduleForScheduleAdapter(scheduleGeneral.getAllSchedule(activity));
		}else if (resId == R.string.preventive){
			ScheduleGeneral scheduleGeneral = new ScheduleGeneral();
			models = scheduleGeneral.getListScheduleForScheduleAdapter(scheduleGeneral.getScheduleByWorktype(activity,getString(R.string.preventive)));
		}else if (resId == R.string.corrective){
			ScheduleGeneral scheduleGeneral = new ScheduleGeneral();
			models = scheduleGeneral.getListScheduleForScheduleAdapter(scheduleGeneral.getScheduleByWorktype(activity,getString(R.string.corrective)));
		}else if (resId == R.string.newlocation){
			ScheduleGeneral scheduleGeneral = new ScheduleGeneral();
			models = scheduleGeneral.getListScheduleForScheduleAdapter(scheduleGeneral.getScheduleByWorktype(activity,getString(R.string.newlocation)));
		}else if (resId == R.string.colocation){
			ScheduleGeneral scheduleGeneral = new ScheduleGeneral();
			models = scheduleGeneral.getListScheduleForScheduleAdapter(scheduleGeneral.getScheduleByWorktype(activity,getString(R.string.colocation)));
		}
		adapter.setItems(models);
	}
	
	public void scrollTo(String date){
		int i = 0;
		for(; i < models.size(); i++){
			if ( 0 == models.get(i).work_date.indexOf(date)){
				models.get(i).isAnimated = true;
				break;
			}
		}
		adapter.notifyDataSetChanged();
		list.smoothScrollToPositionFromTop(i, 0, 500);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
		Intent intent = new Intent(activity, FormActivity.class);
		intent.putExtra(Constants.scheduleId, models.get(position).id);
		startActivity(intent);
	}
	
}