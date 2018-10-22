package com.sap.inspection.fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rindang.zconfig.APIList;
import com.sap.inspection.CallendarActivity;
import com.sap.inspection.CheckInActivity;
import com.sap.inspection.FormActivity;
import com.sap.inspection.MainActivity;
import com.sap.inspection.MyApplication;
import com.sap.inspection.R;
import com.sap.inspection.connection.APIHelper;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.event.UploadProgressEvent;
import com.sap.inspection.model.DefaultValueScheduleModel;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.ScheduleGeneral;
import com.sap.inspection.model.form.WorkFormItemModel;
import com.sap.inspection.model.responsemodel.ScheduleResponseModel;
import com.sap.inspection.task.ScheduleSaver;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.tools.PrefUtil;
import com.sap.inspection.views.adapter.ScheduleAdapter;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Vector;

import de.greenrobot.event.EventBus;

public class ScheduleFragment extends BaseListTitleFragment implements OnItemClickListener{
	private ScheduleAdapter adapter;
	private Vector<ScheduleBaseModel> models;
	private ArrayList<ScheduleBaseModel> itemScheduleModel;

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
		MyApplication.getInstance().setIsScheduleNeedCheckIn(false);
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
		//Schedule
		return "Jadwal";
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
		} else if (resId == R.string.fiber_optic){
			ScheduleGeneral schedulePrecise = new ScheduleGeneral();
			models = schedulePrecise.getListScheduleForScheduleAdapter(schedulePrecise.getScheduleByWorktype(activity,getString(R.string.fiber_optic)));
		} else if (resId == R.string.hasil_PM){
			MyApplication.getInstance().setIsInCheckHasilPm(true);
			ScheduleGeneral schedulePrecise = new ScheduleGeneral();
			models = schedulePrecise.getListScheduleForScheduleAdapter(schedulePrecise.getScheduleByWorktype(activity,getString(R.string.preventive)));
		}
		
//		ScheduleBySiteModel siteModel = new ScheduleBySiteModel();
//		models = siteModel.getListScheduleForScheduleAdapter(siteModel.getAllSchedule(activity));
		adapter.setItems(models);
	}

	public void setItemScheduleModelBy(String scheduleId, String userId) {

		DebugLog.d("set item schedule ");
		DebugLog.d("schedule id : " + scheduleId);
		DebugLog.d("user id : " + userId);

		dialog.setMessage("Loading data please wait");
		dialog.show();
	    APIHelper.getItemSchedules(getContext(), itemScheduleHandler, scheduleId, userId);

	}

	private Handler itemScheduleHandler = new Handler(){

		public void handleMessage(android.os.Message msg) {
			Bundle bundle = msg.getData();
			Gson gson = new Gson();
			if (bundle.getString("json") != null) {
				String jsonItemSchedule = bundle.getString("json");

				/* obtain the response */
				ScheduleResponseModel itemScheduleResponse = gson.fromJson(jsonItemSchedule, ScheduleResponseModel.class);
				if (itemScheduleResponse.status == 200) {

					//dialog.setOnDismissListener(dialog -> Toast.makeText(getContext(), "Berhasil mendapatkan item schedules", Toast.LENGTH_LONG).show());

					DebugLog.d("response OK");
					ScheduleGeneral itemScheduleGeneral = itemScheduleResponse.data.get(0);

					DebugLog.d("size of default value schedules : " + itemScheduleGeneral.default_value_schedule.size());

					for (DefaultValueScheduleModel item_default_value : itemScheduleGeneral.default_value_schedule) {

						String workFormItemId    = String.valueOf(item_default_value.getItem_id());
						String workFormGroupId   = String.valueOf(item_default_value.getGroup_id());
						String new_default_value = String.valueOf(item_default_value.getDefault_value());

						DebugLog.d("{");
						DebugLog.d("--item_id  : " + workFormItemId);
						DebugLog.d("--group_id : " + workFormGroupId);
						DebugLog.d("--default_value : " + new_default_value);
						DebugLog.d("}");

						if (!(new_default_value == null && new_default_value.isEmpty())) {

							DebugLog.d("json default value not null, do update");
							WorkFormItemModel.setDefaultValueFromItemSchedule(workFormItemId, workFormGroupId, new_default_value);
						}
					}
				} else {

					dialog.setOnDismissListener(dialog -> Toast.makeText(getContext(), "Gagal mendapatkan item schedules", Toast.LENGTH_LONG).show());

					DebugLog.d("response status code : " + itemScheduleResponse.status);
					DebugLog.d("response message : " + itemScheduleResponse.messages);
				}

			} else {

				Toast.makeText(getContext(), "repsonse json for ITEM SCHEDULES is null", Toast.LENGTH_LONG).show();
				DebugLog.e("repsonse json for ITEM SCHEDULES is null");
			}

			dialog.dismiss();
		}
	};

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
		Intent intent;

		int workTypeId = models.get(position).work_type.id;
		int siteId = models.get(position).site.id;
		String workTypeName = models.get(position).work_type.name;
		String dayDate = models.get(position).day_date;
		String scheduleId = models.get(position).id;
		String userId = PrefUtil.getStringPref(R.string.user_id, "");

		log("-=-="+ workTypeName +"-=-=-=");
		log("-=-="+ workTypeId +"-=-=-=");
		log("-=-="+ scheduleId +"-=-=-=");
		log("-=-="+ userId +"-=-=-=");

		models.get(position).user.printUserValue();

		if (userId != null && !userId.equalsIgnoreCase("")) {

			setItemScheduleModelBy(scheduleId, userId);
		}

		if (workTypeName.matches(Constants.regexPREVENTIVE) && !MyApplication.getInstance().isInCheckHasilPm()) {
			MyApplication.getInstance().setIsScheduleNeedCheckIn(true);
			intent = new Intent(activity, CheckInActivity.class);
		} else {
			intent = new Intent(activity, FormActivity.class);
		}
		intent.putExtra("userId", userId);
		intent.putExtra("scheduleId", scheduleId);
		intent.putExtra("siteId", siteId);
		intent.putExtra("dayDate", dayDate);
		intent.putExtra("workTypeId", workTypeId);
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