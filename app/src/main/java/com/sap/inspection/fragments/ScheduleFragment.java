package com.sap.inspection.fragments;

import android.app.ProgressDialog;
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
import com.sap.inspection.event.UploadProgressEvent;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.ScheduleGeneral;
import com.sap.inspection.views.adapter.ScheduleAdapter;

import java.util.Vector;

import de.greenrobot.event.EventBus;

public class ScheduleFragment extends BaseListTitleFragment implements OnItemClickListener{
	private ScheduleAdapter adapter;
	private Vector<ScheduleBaseModel> models;
	private int filterBy = 0;
    private ProgressDialog dialog;
	

	public static ScheduleFragment newInstance() {
		ScheduleFragment fragment = new ScheduleFragment();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new ScheduleAdapter(activity);
        dialog = new ProgressDialog(getActivity());
        dialog.setCancelable(false);
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
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
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
			ScheduleGeneral schedulePrecise = new ScheduleGeneral();
			models = schedulePrecise.getListScheduleForScheduleAdapter(schedulePrecise.getScheduleByWorktype(activity,getString(R.string.preventive)));
		}else if (resId == R.string.corrective){
			ScheduleGeneral schedulePrecise = new ScheduleGeneral();
			models = schedulePrecise.getListScheduleForScheduleAdapter(schedulePrecise.getScheduleByWorktype(activity,getString(R.string.corrective)));
		}else if (resId == R.string.newlocation){
			ScheduleGeneral schedulePrecise = new ScheduleGeneral();
			models = schedulePrecise.getListScheduleForScheduleAdapter(schedulePrecise.getScheduleByWorktype(activity,getString(R.string.newlocation)));
		}else if (resId == R.string.colocation){
			ScheduleGeneral schedulePrecise = new ScheduleGeneral();
			models = schedulePrecise.getListScheduleForScheduleAdapter(schedulePrecise.getScheduleByWorktype(activity,getString(R.string.colocation)));
		}else if (resId == R.string.site_audit){
			ScheduleGeneral schedulePrecise = new ScheduleGeneral();
			models = schedulePrecise.getListScheduleForScheduleAdapter(schedulePrecise.getScheduleByWorktype(activity,getString(R.string.site_audit)));
		}
		
//		ScheduleBySiteModel siteModel = new ScheduleBySiteModel();
//		models = siteModel.getListScheduleForScheduleAdapter(siteModel.getAllSchedule(activity));
		adapter.setItems(models);
	}
	
	public void scrollTo(String date){
		int i = 0;
		for(; i < models.size(); i++){
			if ( 0 == models.get(i).day_date.indexOf(date)){
				models.get(i).isAnimated = true;
				break;
			}
		}
		adapter.notifyDataSetChanged();
		list.smoothScrollToPositionFromTop(i, 0, 500);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
		Intent intent = null;
		log("-=-="+models.get(position).work_type.name+"-=-=-=");
		/*
		if (models.get(position).work_type.name.equalsIgnoreCase("corrective"))
			intent = new Intent(activity, FormCorrectiveActivity.class);
		else
			intent = new Intent(activity, FormActivity.class);
			*/
		intent = new Intent(activity, FormActivity.class);
		intent.putExtra(Constants.scheduleId, models.get(position).id);
		intent.putExtra("siteId", models.get(position).site.id);
		intent.putExtra("dayDate", models.get(position).day_date);
		intent.putExtra("workTypeId", models.get(position).work_type.id);
		startActivity(intent);
	}

    public void onEvent(UploadProgressEvent event){
        if (dialog.isShowing()){
            dialog.setMessage(event.progressString);
            if (event.done) dialog.dismiss();
        }else if (!event.done) {
            dialog.show();
            dialog.setMessage(event.progressString);
        }
    }
}