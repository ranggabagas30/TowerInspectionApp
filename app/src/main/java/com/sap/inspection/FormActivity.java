package com.sap.inspection;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.InputType;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.event.UploadProgressEvent;
import com.sap.inspection.fragments.BaseFragment;
import com.sap.inspection.fragments.NavigationFragment;
import com.sap.inspection.listener.FormActivityListener;
import com.sap.inspection.model.ConfigModel;
import com.sap.inspection.model.DbManager;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.ScheduleGeneral;
import com.sap.inspection.model.config.formimbaspetir.Barang;
import com.sap.inspection.model.config.formimbaspetir.FormImbasPetirConfig;
import com.sap.inspection.model.config.formimbaspetir.ImbasPetirData;
import com.sap.inspection.model.config.formimbaspetir.Warga;
import com.sap.inspection.model.form.RowModel;
import com.sap.inspection.model.form.WorkFormGroupModel;
import com.sap.inspection.model.form.WorkFormModel;
import com.sap.inspection.tools.DebugLog;
import com.slidinglayer.SlidingLayer;
import com.yarolegovich.lovelydialog.LovelyTextInputDialog;

import java.util.ArrayList;
import java.util.Vector;

import de.greenrobot.event.EventBus;

public class FormActivity extends BaseActivity implements FormActivityListener{

	private SlidingLayer mSlidingLayer;
	private RowModel rowModel = null;
	private WorkFormModel workFormModel;
	private Vector<WorkFormGroupModel> workFormGroupModels;
	private ScheduleBaseModel scheduleBaseModels;
	private ProgressDialog dialog;

	private String dayDate;
	private String scheduleId;
	private String workTypeName;
	private int siteId;
	private int workTypeId;

	NavigationFragment navigationFragment = NavigationFragment.newInstance();

	private LovelyTextInputDialog inputJumlahWargaDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// set dialog
		dialog = new ProgressDialog(activity);
		dialog.setCancelable(false);
		dialog.setMessage(getString(R.string.generatingInspectionForm));
		dialog.show();

		inputJumlahWargaDialog = new LovelyTextInputDialog(this, R.style.CheckBoxTintTheme)
				.setTopColorRes(R.color.item_drill_red)
				.setTopTitle("input Jumlah Warga")
				.setTopTitleColor(R.color.lightgray)
				.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED);

		// get data bundle from ScheduleFragment
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {

			scheduleId 	 = bundle.getString(Constants.KEY_SCHEDULEID);
			siteId		 = bundle.getInt(Constants.KEY_SITEID);
			workTypeId	 = bundle.getInt(Constants.KEY_WORKTYPEID);
			workTypeName = bundle.getString(Constants.KEY_WORKTYPENAME);
			dayDate 	 = bundle.getString(Constants.KEY_DAYDATE);
		}

		DebugLog.d("scheduleId=" + scheduleId);
		DebugLog.d("siteId=" + siteId);
		DebugLog.d("workTypeId=" + workTypeId);
		DebugLog.d("workTypeName=" + workTypeName);
		DebugLog.d("dayDate=" + dayDate);

		// get schedule base model by scheduleid
		scheduleBaseModels = new ScheduleGeneral();
		scheduleBaseModels = scheduleBaseModels.getScheduleById(scheduleId);

		// get workformid by worktypeid
		workFormModel = new WorkFormModel();
		workFormModel = workFormModel.getItemByWorkTypeId(scheduleBaseModels.work_type.id);

		DebugLog.d("== schedule worktype id : "+scheduleBaseModels.work_type.id);
		DebugLog.d("== form model id : "+workFormModel.id);
		DebugLog.d("== form model name : "+workFormModel.name);

		// get all workformgroup by workformid
		WorkFormGroupModel groupModel = new WorkFormGroupModel();
		workFormGroupModels = groupModel.getAllItemByWorkFormId(workFormModel.id);

		mSlidingLayer = (SlidingLayer) findViewById(R.id.slidingLayer1);
		mSlidingLayer.setStickTo(SlidingLayer.STICK_TO_LEFT);

		//generate form
		rowModel = new RowModel();
		rowModel.isOpen = true;
		rowModel.position = 0;
		rowModel.text = "this is just";
		rowModel.children = new Vector<>();

		if (workTypeName.equalsIgnoreCase(getString(R.string.foto_imbas_petir))) {

			checkDataWarga();

		} else {

			// get all workformgroup submenu
			DebugLog.d("get all workformgroup submenu");
			for (WorkFormGroupModel model : workFormGroupModels) {
				DebugLog.d("==== form group model id : " + model.id + " | " + model.name);
				RowModel groupRow = new RowModel();
				groupRow.work_form_group_id = model.id;
				groupRow.children = rowModel.getAllItemByWorkFormGroupId(model.id);
				groupRow.text = model.name;
				groupRow.level = 0;
				rowModel.children.add(groupRow); // children of
			}

			navigationFragment.setFormActivityListener(this);
			navigationFragment.setSchedule(scheduleBaseModels);
			navigationFragment.setNavigationModel(rowModel);
			navigationFragment.setWorkTypeName(workTypeName);
			navigateToFragment(navigationFragment, R.id.fragment_behind);
		}

		dialog.dismiss();

		trackThisPage("Form");
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		EventBus.getDefault().register(this);


	}

	@Override
	protected void onPause() {
		super.onPause();
		EventBus.getDefault().unregister(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onBackPressed() {
		if (mSlidingLayer.isOpened())
			mSlidingLayer.closeLayer(true);
		else
		{
			DebugLog.d("back button pressed");
			if (MyApplication.getInstance().isScheduleNeedCheckIn()) {
				MyApplication.getInstance().toast("Checkout success", Toast.LENGTH_SHORT);
			}

			if (inputJumlahWargaDialog != null) {
				inputJumlahWargaDialog.dismiss();
			}
			finish();
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void myOnBackPressed() {
		onBackPressed();
	}

	@Override
	public void onShowNavigation() {
		mSlidingLayer.openLayer(true);
	}

	private void navigateToFragment(BaseFragment fragment, int viewContainerResId) {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(viewContainerResId, fragment);
		ft.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.commit();
	}

	private RowModel generateOthersModel(){
		RowModel groupRow = new RowModel();
		groupRow.children = new Vector<>();
		groupRow.children.add(generateOthersChildModel());
		groupRow.text = "Others";
		groupRow.level = 0;
		return groupRow;
	}
	
	private RowModel generateOthersChildModel(){
		RowModel groupRow = new RowModel();
		groupRow.text = "Others Form";
		groupRow.hasForm = true;
		groupRow.level = 1;
		return groupRow;
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

	private void checkDataWarga() {

		int dataIndex = FormImbasPetirConfig.indexOfData(scheduleId);

		if (dataIndex != -1) {

			// found data by that scheduleid
			// check amount of warga

			ArrayList<Warga> wargas = FormImbasPetirConfig.getDataWarga(dataIndex);

			if (wargas != null) {

				// if == 0, then show dialog to input amount of warga
				if (wargas.size() == 0) {

					DebugLog.d("data warga is empty");
					showInputAmountWargaDialog(dataIndex);

				} else {

					// if != 0, then just generate the navigation child view
					DebugLog.d("data warga is not empty, size : " + wargas.size());
					generateImbasPetirChildModel(wargas.size());

				}
			}
		} else {

			// not found data by that scheduleid
			DebugLog.d("no data config for scheduleid : " + scheduleId);
		}
	}

	private void showInputAmountWargaDialog(int dataIndex) {

		inputJumlahWargaDialog.setConfirmButton("Tambah", amountOfWarga -> {

			// insert new data warga as many as amount inputted
			MyApplication.getInstance().toast("Tambahan jumlah warga : " + amountOfWarga, Toast.LENGTH_LONG);

			FormImbasPetirConfig.updateDataWarga(dataIndex, Integer.valueOf(amountOfWarga));

			generateImbasPetirChildModel(Integer.valueOf(amountOfWarga));

		}).show();
	}

	private void generateImbasPetirChildModel(int wargaSize) {

		// get all workformgroup submenu
		DebugLog.d("get all workformgroup submenu");
		for (WorkFormGroupModel model : workFormGroupModels) {

			DebugLog.d("==== form group model id : " + model.id + " | " + model.name);
			RowModel groupRow = new RowModel();
			groupRow.work_form_group_id = model.id;
			groupRow.text = model.name;
			groupRow.level = 0;

			if (model.name.equalsIgnoreCase("Warga")) {

				Vector<RowModel> childRows = new Vector<>();

				for (int i = 0; i < wargaSize; i++) {

					int wargake = i + 1;

					StringBuilder wargaLabel = new StringBuilder();

					RowModel wargaKeModel = rowModel.getAllItemByWorkFormGroupId(model.id).get(0);

					wargaKeModel.hasForm = true;

					wargaLabel.append(wargaKeModel.text).append(wargake);

					wargaKeModel.text = new String(wargaLabel);

					DebugLog.d("-- child id : " + wargaKeModel.id);
					DebugLog.d("-- child work form group id : " + wargaKeModel.work_form_group_id);
					DebugLog.d("-- child name : " + wargaKeModel.text);
					DebugLog.d("-- child level : " + wargaKeModel.level);
					DebugLog.d("-- child ancestry : " + wargaKeModel.ancestry);
					DebugLog.d("-- child parentid : " + wargaKeModel.parent_id);
					DebugLog.d("\n\n");
					childRows.add(wargaKeModel);
				}

				// row model for "tambah warga" submenu action
				RowModel addWargaKeModel = new RowModel();
				addWargaKeModel.id = -1;
				addWargaKeModel.work_form_group_id = model.id;
				addWargaKeModel.hasForm = false;
				addWargaKeModel.text = "Tambah warga";
				addWargaKeModel.level = 1;
				addWargaKeModel.ancestry = null;
				addWargaKeModel.parent_id = 0;

				childRows.add(addWargaKeModel);

				groupRow.children = childRows;

			} else {

				groupRow.children = rowModel.getAllItemByWorkFormGroupId(model.id);
			}

			rowModel.children.add(groupRow);
		}

		navigationFragment.setFormActivityListener(this);
		navigationFragment.setSchedule(scheduleBaseModels);
		navigationFragment.setNavigationModel(rowModel);
		navigateToFragment(navigationFragment, R.id.fragment_behind);
	}
}