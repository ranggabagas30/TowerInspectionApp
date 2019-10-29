package com.sap.inspection.view.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
<<<<<<< HEAD
import android.text.TextUtils;
=======
import android.support.annotation.Nullable;
>>>>>>> currentwork-sap
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sap.inspection.BuildConfig;
import com.sap.inspection.R;
import com.sap.inspection.TowerApplication;
import com.sap.inspection.connection.APIHelper;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.event.UploadProgressEvent;
import com.sap.inspection.model.DefaultValueScheduleModel;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.ScheduleGeneral;
import com.sap.inspection.model.config.formimbaspetir.CorrectiveScheduleConfig;
import com.sap.inspection.model.config.formimbaspetir.FormImbasPetirConfig;
import com.sap.inspection.model.form.WorkFormItemModel;
import com.sap.inspection.model.responsemodel.CorrectiveScheduleResponseModel;
import com.sap.inspection.model.responsemodel.ScheduleResponseModel;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.PrefUtil;
import com.sap.inspection.view.adapter.ScheduleAdapter;
import com.sap.inspection.view.ui.BaseActivity;
import com.sap.inspection.view.ui.CallendarActivity;
import com.sap.inspection.view.ui.MainActivity;

import java.util.Vector;

import de.greenrobot.event.EventBus;

public class ScheduleFragment extends BaseListTitleFragment implements OnItemClickListener{
	private ScheduleAdapter adapter;
	private Vector<ScheduleBaseModel> models;
	private String userId;
	private BaseActivity baseActivity;
	private int filterBy = 0;

	public static ScheduleFragment newInstance() {
		ScheduleFragment fragment = new ScheduleFragment();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		baseActivity = (BaseActivity) getActivity();
		adapter = new ScheduleAdapter(getActivity());
	}

	@Override
	public void onCreateView(LayoutInflater inflater) {
		super.onCreateView(inflater);
		userId = PrefUtil.getStringPref(R.string.user_id, "");
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
		actionRight.setVisibility(View.VISIBLE);
		actionRight.setOnClickListener(v -> {
			Intent intent = new Intent(getActivity(), CallendarActivity.class);
			intent.putExtra("filterBy", filterBy);
			startActivityForResult(intent,MainActivity.REQUEST_CODE);
		});
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
    public void onResume() {
        super.onResume();
		TowerApplication.getInstance().setIsScheduleNeedCheckIn(false);
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
		log("==== getActivity() result ==== "+requestCode+" "+resultCode);
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
			if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {
				Vector<ScheduleBaseModel> modelsImbasPetir = scheduleGeneral.getListScheduleForScheduleAdapter(scheduleGeneral.getScheduleByWorktype(getActivity(), getString(R.string.foto_imbas_petir)));
				checkImbasPetirConfig(modelsImbasPetir);
			}
			models = scheduleGeneral.getListScheduleForScheduleAdapter(scheduleGeneral.getAllSchedule(getActivity()));
		}else if (resId == R.string.preventive){
			ScheduleGeneral schedulePrecise = new ScheduleGeneral();
			models = schedulePrecise.getListScheduleForScheduleAdapter(schedulePrecise.getScheduleByWorktype(getActivity(),getString(R.string.preventive)));
		}else if (resId == R.string.corrective){
			ScheduleGeneral schedulePrecise = new ScheduleGeneral();
			models = schedulePrecise.getListScheduleForScheduleAdapter(schedulePrecise.getScheduleByWorktype(getActivity(),getString(R.string.corrective)));
		}else if (resId == R.string.newlocation){
			ScheduleGeneral schedulePrecise = new ScheduleGeneral();
			models = schedulePrecise.getListScheduleForScheduleAdapter(schedulePrecise.getScheduleByWorktype(getActivity(),getString(R.string.newlocation)));
		}else if (resId == R.string.colocation){
			ScheduleGeneral schedulePrecise = new ScheduleGeneral();
			models = schedulePrecise.getListScheduleForScheduleAdapter(schedulePrecise.getScheduleByWorktype(getActivity(),getString(R.string.colocation)));
		}else if (resId == R.string.site_audit){
			ScheduleGeneral schedulePrecise = new ScheduleGeneral();
			models = schedulePrecise.getListScheduleForScheduleAdapter(schedulePrecise.getScheduleByWorktype(getActivity(),getString(R.string.site_audit)));
		} else if (resId == R.string.fiber_optic){
			ScheduleGeneral schedulePrecise = new ScheduleGeneral();
			models = schedulePrecise.getListScheduleForScheduleAdapter(schedulePrecise.getScheduleByWorktype(getActivity(),getString(R.string.fiber_optic)));
		} else if (resId == R.string.foto_imbas_petir) {
			ScheduleGeneral schedulePrecise = new ScheduleGeneral();
			models = schedulePrecise.getListScheduleForScheduleAdapter(schedulePrecise.getScheduleByWorktype(getActivity(),getString(R.string.foto_imbas_petir)));
			checkImbasPetirConfig(models);
		} else if (resId == R.string.hasil_PM){
			TowerApplication.getInstance().setIS_CHECKING_HASIL_PM(true);
			ScheduleGeneral schedulePrecise = new ScheduleGeneral();
			models = schedulePrecise.getListScheduleForScheduleAdapter(schedulePrecise.getScheduleByWorktype(getActivity(),getString(R.string.preventive)));
		} else if (resId == R.string.routing_segment) {
			ScheduleGeneral schedulePrecise = new ScheduleGeneral();
			models = schedulePrecise.getListScheduleForScheduleAdapter(schedulePrecise.getScheduleByWorktype(getActivity(), getString(R.string.routing_segment)));
		} else if (resId == R.string.handhole) {
			ScheduleGeneral schedulePrecise = new ScheduleGeneral();
			models = schedulePrecise.getListScheduleForScheduleAdapter(schedulePrecise.getScheduleByWorktype(getActivity(), getString(R.string.handhole)));
		} else if (resId == R.string.hdpe) {
			ScheduleGeneral schedulePrecise = new ScheduleGeneral();
			models = schedulePrecise.getListScheduleForScheduleAdapter(schedulePrecise.getScheduleByWorktype(getActivity(), getString(R.string.hdpe)));
		} else if (resId == R.string.focut) {
			ScheduleGeneral schedulePrecise = new ScheduleGeneral();
			models = schedulePrecise.getListScheduleForScheduleAdapter(schedulePrecise.getScheduleByWorktype(getActivity(), getString(R.string.focut)));
		}
		adapter.setItems(models);
	}

	private void checkImbasPetirConfig(Vector<ScheduleBaseModel> listSchedules) {

		FormImbasPetirConfig formImbasPetirConfig = FormImbasPetirConfig.getImbasPetirConfig();

		if (formImbasPetirConfig == null) {

			DebugLog.d("Form imbas petir config not found, create config");
			FormImbasPetirConfig.createImbasPetirConfig();
		}

		DebugLog.d("== checking config data over schedules === ");
		for (ScheduleBaseModel schedule : listSchedules) {

			if (schedule.id != null) {

				// if schedule data config not found, then add new data to the config

				if (!FormImbasPetirConfig.isDataExist(schedule.id)) {

					DebugLog.d("schedule data for scheduleid " + schedule.id + " not found, add new data !");
					FormImbasPetirConfig.insertNewData(schedule.id);

				}

			}
		}
	}

	private void checkCorrectiveScheduleConfig(String userId) {

		CorrectiveScheduleResponseModel correctiveData = CorrectiveScheduleConfig.getCorrectiveScheduleConfig();
		if (correctiveData == null) {

			DebugLog.d("Corrective schedule config not found, create config");
			baseActivity.showMessageDialog("Loading corrective schedules data");
			APIHelper.getCorrectiveSchedule(getContext(), correctiveScheduleHandler, userId);

		} else {

			Vector<ScheduleBaseModel> correctiveScheduleModels = new Vector<>();
			for (CorrectiveScheduleResponseModel.CorrectiveSchedule correctiveSchedule : correctiveData.getData()) {

				String scheduleId = String.valueOf(correctiveSchedule.getId());
				ScheduleBaseModel correctiveScheduleModel = new ScheduleGeneral();
				correctiveScheduleModel = correctiveScheduleModel.getScheduleById(scheduleId);
				correctiveScheduleModels.add(correctiveScheduleModel);
			}
			ScheduleGeneral schedulePrecise = new ScheduleGeneral();
			models.addAll(schedulePrecise.getListScheduleForScheduleAdapter(correctiveScheduleModels));
            adapter.setItems(models);
		}
	}

	public void setItemScheduleModelBy(String scheduleId, String userId) {
		DebugLog.d("set item schedule ");
		DebugLog.d("schedule id : " + scheduleId);
		DebugLog.d("user id : " + userId);

<<<<<<< HEAD
		/*dialog.setMessage("Loading data please wait");
		dialog.show();*/
=======
		//baseActivity.showMessageDialog("Loading data please wait");
>>>>>>> currentwork-sap
	    APIHelper.getItemSchedules(getContext(), itemScheduleHandler, scheduleId, userId);
	}

	@SuppressLint("HandlerLeak")
	private Handler itemScheduleHandler = new Handler(){

		public void handleMessage(android.os.Message msg) {

<<<<<<< HEAD
			//dialog.dismiss();

=======
			baseActivity.hideDialog();
>>>>>>> currentwork-sap
			Bundle bundle = msg.getData();
			Gson gson = new Gson();

			boolean isResponseOK = bundle.getBoolean("isresponseok");

			if (isResponseOK) {

				if (bundle.getString("json") != null) {
					String jsonItemSchedule = bundle.getString("json");

					/* obtain the response */
					ScheduleResponseModel itemScheduleResponse = gson.fromJson(jsonItemSchedule, ScheduleResponseModel.class);

					if (!itemScheduleResponse.data.isEmpty()) {

						if (itemScheduleResponse.status == 200) {

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

								if (!TextUtils.isEmpty(new_default_value)) {
									DebugLog.d("json default value not null, do update");
									WorkFormItemModel.setDefaultValueFromItemSchedule(workFormItemId, workFormGroupId, new_default_value);
								}
							}

						} else {

							//Toast.makeText(getContext(), "Gagal mendapatkan item schedules\n" + itemScheduleResponse.status + " : " + itemScheduleResponse.messages, Toast.LENGTH_LONG).show();

							DebugLog.d("response status code : " + itemScheduleResponse.status);
							DebugLog.d("response message : " + itemScheduleResponse.messages);
						}
					} else {

						//Toast.makeText(getContext(), "Item schedules data kosong atau tidak ada", Toast.LENGTH_LONG).show();
						DebugLog.d("item schedules kosong");
					}

				} else {

					//Toast.makeText(getContext(), "Schedule ini tidak memiliki data item schedules", Toast.LENGTH_LONG).show();
					DebugLog.e("repsonse json for ITEM SCHEDULES is null");
				}
			}

		}
	};

	@SuppressLint("HandlerLeak")
	private Handler correctiveScheduleHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			baseActivity.hideDialog();
			Bundle bundle = msg.getData();
			Gson gson = new Gson();

			boolean isResponseOK = bundle.getBoolean("isresponseok");

			if (isResponseOK) {

				if (bundle.getString("json") != null) {
					String jsonCorrectiveSchedule = bundle.getString("json");
					CorrectiveScheduleResponseModel correctiveData = gson.fromJson(jsonCorrectiveSchedule, CorrectiveScheduleResponseModel.class);
					if (correctiveData != null) {
						CorrectiveScheduleConfig.setCorrectiveScheduleConfig(correctiveData);
						Vector<ScheduleBaseModel> correctiveScheduleModels = new Vector<>();
						for (CorrectiveScheduleResponseModel.CorrectiveSchedule correctiveSchedule : correctiveData.getData()) {
							String scheduleId = String.valueOf(correctiveSchedule.getId());
							ScheduleBaseModel correctiveScheduleModel = new ScheduleGeneral();
							correctiveScheduleModel = correctiveScheduleModel.getScheduleById(scheduleId);
							correctiveScheduleModels.add(correctiveScheduleModel);
						}
						ScheduleGeneral schedulePrecise = new ScheduleGeneral();
						models.addAll(schedulePrecise.getListScheduleForScheduleAdapter(correctiveScheduleModels));
						adapter.setItems(models);
					}
				} else {

					Toast.makeText(getContext(), "JSON == null. Gagal mengunduh data schedule Corrective", Toast.LENGTH_LONG).show();
				}
			} else {

				Toast.makeText(getContext(), "Response not OK. Gagal mengunduh data schedule Corrective", Toast.LENGTH_LONG).show();
			}
		}
	};

	public void scrollTo(String date){
		int i = 0;
		for(; i < models.size(); i++){
			if ( 0 == models.get(i).day_date.indexOf(date)) {
				models.get(i).isAnimated = true;
				break;
			}
		}
		adapter.notifyDataSetChanged();
		list.smoothScrollToPositionFromTop(i, 0, 500);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view,int position, long id) {

		int workTypeId = models.get(position).work_type.id;
		int siteId = models.get(position).site.id;
		String workTypeName = models.get(position).work_type.name;
		String dayDate = models.get(position).day_date;
		String scheduleId = models.get(position).id;

		log("-=-="+ workTypeName +"-=-=-=");
		log("-=-="+ workTypeId +"-=-=-=");
		log("-=-="+ scheduleId +"-=-=-=");
		log("-=-="+ userId +"-=-=-=");

		if (userId != null && !userId.equalsIgnoreCase("") && !TowerApplication.getInstance().IS_CHECKING_HASIL_PM()) {

			setItemScheduleModelBy(scheduleId, userId);
		}

		if (workTypeName.matches(Constants.regexPREVENTIVE) && !TowerApplication.getInstance().IS_CHECKING_HASIL_PM()) {
		    TowerApplication.getInstance().setIsScheduleNeedCheckIn(true);

			BaseActivity.navigateToCheckinActivity(
					getActivity(),
					userId,
					scheduleId,
					siteId,
					dayDate,
					workTypeId,
					workTypeName
			);

		} else {

			BaseActivity.navigateToGroupActivity(
					getActivity(),
					scheduleId,
					siteId,
					workTypeId,
					workTypeName,
					dayDate
			);
		}

	}

    public void onEvent(UploadProgressEvent event){
        if (event.done) baseActivity.hideDialog();
        else baseActivity.showMessageDialog(event.progressString);
    }
}