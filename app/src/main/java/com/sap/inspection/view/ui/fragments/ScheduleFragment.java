package com.sap.inspection.view.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
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
import com.sap.inspection.connection.rest.TowerAPIHelper;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.constant.GlobalVar;
import com.sap.inspection.model.DefaultValueScheduleModel;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.ScheduleGeneral;
import com.sap.inspection.model.config.formimbaspetir.CorrectiveScheduleConfig;
import com.sap.inspection.model.config.formimbaspetir.FormImbasPetirConfig;
import com.sap.inspection.model.form.WorkFormItemModel;
import com.sap.inspection.model.responsemodel.CorrectiveScheduleResponseModel;
import com.sap.inspection.model.responsemodel.CreateScheduleFOCUTResponseModel;
import com.sap.inspection.model.responsemodel.ScheduleResponseModel;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.DateUtil;
import com.sap.inspection.util.DialogUtil;
import com.sap.inspection.util.PrefUtil;
import com.sap.inspection.view.adapter.ScheduleAdapter;
import com.sap.inspection.view.ui.BaseActivity;
import com.sap.inspection.view.ui.CallendarActivity;
import com.sap.inspection.view.ui.MainActivity;

import org.apache.http.HttpStatus;

import java.net.HttpURLConnection;
import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ScheduleFragment extends BaseListTitleFragment implements OnItemClickListener{

	private ScheduleAdapter adapter;
	private BaseActivity baseActivity;
	private ArrayList<ScheduleGeneral> schedules;
	private String userId;
	private String ttNumber; // SAP for creating FO CUT schedule

	private int filterBy = 0;

	public static ScheduleFragment newInstance() {
		return new ScheduleFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		baseActivity = (BaseActivity) getActivity();
		adapter = new ScheduleAdapter(getActivity());
		schedules = new ArrayList<>();
	}

	@Override
	public void onCreateView(LayoutInflater inflater, Bundle savedInstanceState) {
		userId = PrefUtil.getStringPref(R.string.user_id, "");
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
		actionRight.setVisibility(View.VISIBLE);
		actionRight.setOnClickListener(v -> {
			Intent intent = new Intent(getActivity(), CallendarActivity.class);
			intent.putExtra("filterBy", filterBy);
			startActivityForResult(intent,MainActivity.REQUEST_CODE);
		});

		if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {
			FormImbasPetirConfig formImbasPetirConfig = FormImbasPetirConfig.getImbasPetirConfig();
			if (formImbasPetirConfig == null) {
				DebugLog.d("Form imbas petir config not found, create config");
				FormImbasPetirConfig.createImbasPetirConfig();
			}
		}
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
    public void onResume() {
        super.onResume();
        DebugLog.d("resume");
		TowerApplication.getInstance().setIsScheduleNeedCheckIn(false);
		if (filterBy != 0) {
			DebugLog.d("key filter by: " + filterBy);
			setScheduleBy(filterBy);
		}
    }

    @Override
    public void onPause() {
		DebugLog.d("pause");
        super.onPause();
    }

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
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
		return "Jadwal";
	}

	@SuppressLint("CheckResult")
	public void setScheduleBy(int resId){
		actionAdd.setVisibility(View.INVISIBLE);
		filterBy = resId;

		if (resId == R.string.hasil_PM){
			TowerApplication.getInstance().setIS_CHECKING_HASIL_PM(true);
			resId = R.string.preventive;
		} else if (resId == R.string.focut) {
			actionAdd.setVisibility(View.VISIBLE);
			actionAdd.setOnClickListener(view -> openCreateScheduleFOCUT());
		}

		String workType = resId == R.string.schedule ? null : getString(resId);
		showMessageDialog("Memuat jadwal");
		schedules.clear();
		compositeDisposable.add(
				ScheduleBaseModel.loadSchedules(workType)
						.flatMapIterable(schedules -> schedules)
						.flatMap(this::checkImbasPetirSchedule)
						.subscribe(
								schedule -> {
									if (schedule != null) schedules.add(schedule);
								}, error -> {
									hideDialog();
									DebugLog.e(error.getMessage(), error);
									Toast.makeText(getActivity(), "Gagal memuat jadwal", Toast.LENGTH_LONG).show();
								}, () -> {
									schedules = ScheduleBaseModel.getListScheduleForScheduleAdapter(schedules);
									adapter.setItems(schedules);
									hideDialog();
									if (schedules.isEmpty()) {
										Toast.makeText(getActivity(), "Tidak ada jadwal", Toast.LENGTH_SHORT).show();
									}
								})
		);
	}

	private Observable<ScheduleGeneral> checkImbasPetirSchedule(ScheduleGeneral schedule) {
		if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP) && schedule.id != null && schedule.work_type.name.matches(Constants.regexIMBASPETIR)) {
			// if schedule data config not found, then add new data to the config
			if (!FormImbasPetirConfig.isDataExist(schedule.id)) {
				DebugLog.d("schedule data for scheduleid " + schedule.id + " not found, add new data !");
				FormImbasPetirConfig.insertNewData(schedule.id);
			}
		}
		return Observable.just(schedule);
	}

	private void checkCorrectiveScheduleConfig(String userId) {
		CorrectiveScheduleResponseModel correctiveData = CorrectiveScheduleConfig.getCorrectiveScheduleConfig();
		if (correctiveData == null) {
			DebugLog.d("Corrective schedule config not found, create config");
			showMessageDialog("Loading corrective schedules data");
			APIHelper.getCorrectiveSchedule(getContext(), correctiveScheduleHandler, userId);
		} else {

			ArrayList<ScheduleGeneral> correctiveScheduleModels = new ArrayList<>();
			for (CorrectiveScheduleResponseModel.CorrectiveSchedule correctiveSchedule : correctiveData.getData()) {
				String scheduleId = String.valueOf(correctiveSchedule.getId());
				ScheduleGeneral correctiveScheduleModel = ScheduleBaseModel.getScheduleById(scheduleId);
				correctiveScheduleModels.add(correctiveScheduleModel);
			}
			schedules.addAll(ScheduleGeneral.getListScheduleForScheduleAdapter(correctiveScheduleModels));
            adapter.setItems(schedules);
		}
	}

	public void setItemScheduleModelBy(String scheduleId, String userId) {
		DebugLog.d("set item schedule ");
		DebugLog.d("schedule id : " + scheduleId);
		DebugLog.d("user id : " + userId);
	    APIHelper.getItemSchedules(getContext(), itemScheduleHandler, scheduleId, userId);
	}

	@SuppressLint("HandlerLeak")
	private Handler itemScheduleHandler = new Handler(){

		public void handleMessage(android.os.Message msg) {
			hideDialog();
			Bundle bundle = msg.getData();
			Gson gson = new Gson();
			boolean isResponseOK = bundle.getBoolean("isresponseok");
			if (isResponseOK) {
				if (bundle.getString("json") != null) {
					String jsonItemSchedule = bundle.getString("json");

					/* obtain the response */
					ScheduleResponseModel itemScheduleResponse = gson.fromJson(jsonItemSchedule, ScheduleResponseModel.class);

					// TODO: test itemScheduleResponse.data =
					if (itemScheduleResponse != null && itemScheduleResponse.data != null && !itemScheduleResponse.data.isEmpty()) {
						if (itemScheduleResponse.status == HttpURLConnection.HTTP_OK) {
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
							DebugLog.d("response status code : " + itemScheduleResponse.status);
							DebugLog.d("response message : " + itemScheduleResponse.messages);
						}
					} else {
						DebugLog.d("item schedules kosong");
					}

				} else {
					DebugLog.e("repsonse json for ITEM SCHEDULES is null");
				}
			}

		}
	};

	@SuppressLint("HandlerLeak")
	private Handler correctiveScheduleHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			hideDialog();
			Bundle bundle = msg.getData();
			Gson gson = new Gson();
			boolean isResponseOK = bundle.getBoolean("isresponseok");
			if (isResponseOK) {

				if (bundle.getString("json") != null) {
					String jsonCorrectiveSchedule = bundle.getString("json");
					CorrectiveScheduleResponseModel correctiveData = gson.fromJson(jsonCorrectiveSchedule, CorrectiveScheduleResponseModel.class);
					if (correctiveData != null) {
						CorrectiveScheduleConfig.setCorrectiveScheduleConfig(correctiveData);
						ArrayList<ScheduleGeneral> correctiveScheduleModels = new ArrayList<>();
						for (CorrectiveScheduleResponseModel.CorrectiveSchedule correctiveSchedule : correctiveData.getData()) {
							String scheduleId = String.valueOf(correctiveSchedule.getId());
							ScheduleGeneral correctiveScheduleModel = ScheduleGeneral.getScheduleById(scheduleId);
							correctiveScheduleModels.add(correctiveScheduleModel);
						}
						schedules.addAll(ScheduleBaseModel.getListScheduleForScheduleAdapter(correctiveScheduleModels));
						adapter.setItems(schedules);
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
		for(; i < schedules.size(); i++){
			if ( 0 == schedules.get(i).day_date.indexOf(date)) {
				schedules.get(i).isAnimated = true;
				break;
			}
		}
		adapter.notifyDataSetChanged();
		list.smoothScrollToPositionFromTop(i, 0, 500);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view,int position, long id) {

		ScheduleGeneral schedule = schedules.get(position);
		int workTypeId = schedule.work_type.id;
		int siteId = schedule.site.id;
		String workTypeName = schedule.work_type.name;
		String dayDate = schedule.day_date;
		String scheduleId = schedule.id;

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

    private void openCreateScheduleFOCUT() {
		DialogUtil.showCreateFoCutScheduleDialog(getContext(), ttNumber -> {
			if (!GlobalVar.getInstance().anyNetwork(getActivity())) {
				TowerApplication.getInstance().toast("Tidak ada koneksi internet, periksa kembali jaringan anda.", Toast.LENGTH_SHORT);
				return;
			}

			if (!TextUtils.isEmpty(ttNumber))
				createScheduleFOCUT(ttNumber, DateUtil.toDate(System.currentTimeMillis(), Constants.DATETIME_PATTERN3), this.userId);
			else Toast.makeText(baseActivity, "TT Number tidak boleh kosong", Toast.LENGTH_SHORT).show();
		});
	}

	@SuppressLint("CheckResult")
	private void createScheduleFOCUT(String ttNumber, String workDate, String userId) {
		DebugLog.d("ttnumber: " + ttNumber);
		DebugLog.d("workdate: " + workDate);
		DebugLog.d("userId: " + userId);

		showMessageDialog("Creating schedule FOCUT");
		compositeDisposable.add(TowerAPIHelper.createScheduleFOCUT(ttNumber, workDate, userId)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(response -> {
					hideDialog();
					if (response.status == HttpStatus.SC_CREATED) {
						onSuccessCreateScheduleFOCUT(response);
					} else {
						// error
                        DebugLog.e("ERROR: " + response.messages + ", status: " + response.status);
						Toast.makeText(getContext(), response.messages, Toast.LENGTH_LONG).show();
					}
				}, error -> {
					hideDialog();
					Toast.makeText(getContext(), "Failed to create schedule FO CUT (error: " + error.getMessage() + ")", Toast.LENGTH_LONG).show();
					DebugLog.e(error.getMessage(), error);
				})
		);
	}

	private void onSuccessCreateScheduleFOCUT(CreateScheduleFOCUTResponseModel response) {
		if (response != null) {
			showMessageDialog("Loading schedules");
			ScheduleGeneral schedule = response.data;
			schedule.save();
			setScheduleBy(R.string.focut);
			hideDialog();
		}
	}
}