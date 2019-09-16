package com.sap.inspection.view.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.InputType;
import android.widget.Toast;

import com.sap.inspection.R;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.listener.GroupActivityListener;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.ScheduleGeneral;
import com.sap.inspection.model.config.formimbaspetir.CorrectiveScheduleConfig;
import com.sap.inspection.model.config.formimbaspetir.FormImbasPetirConfig;
import com.sap.inspection.model.config.formimbaspetir.Warga;
import com.sap.inspection.model.form.WorkFormGroupModel;
import com.sap.inspection.model.form.WorkFormModel;
import com.sap.inspection.model.form.WorkFormRowModel;
import com.sap.inspection.model.responsemodel.CorrectiveScheduleResponseModel;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.StringUtil;
import com.sap.inspection.view.ui.fragments.GroupFragment;
import com.slidinglayer.SlidingLayer;
import com.yarolegovich.lovelydialog.LovelyTextInputDialog;

import java.util.ArrayList;
import java.util.Vector;

public class GroupActivity extends BaseActivity implements GroupActivityListener {

	private SlidingLayer mSlidingLayer;
	private WorkFormRowModel parentGroupRow = null;
	private WorkFormModel workForm;
	private Vector<WorkFormGroupModel> workFormGroups;
	private ScheduleBaseModel schedule;

	private String dayDate;
	private String scheduleId;
	private String workTypeName;
	private int siteId;
	private int workTypeId;

	private FragmentManager fm;
	private Fragment currentFragment;
	private GroupFragment groupFragment = GroupFragment.newInstance();

	public LovelyTextInputDialog inputJumlahWargaDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        // get data bundle from ScheduleFragment
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            scheduleId 	 = bundle.getString(Constants.KEY_SCHEDULEID);
            siteId		 = bundle.getInt(Constants.KEY_SITEID);
            workTypeId	 = bundle.getInt(Constants.KEY_WORKTYPEID);
            workTypeName = bundle.getString(Constants.KEY_WORKTYPENAME);
            dayDate 	 = bundle.getString(Constants.KEY_DAYDATE);
        }

        DebugLog.d("received bundles : ");
        DebugLog.d("scheduleId=" + scheduleId);
        DebugLog.d("siteId=" + siteId);
        DebugLog.d("workTypeId=" + workTypeId);
        DebugLog.d("workTypeName=" + workTypeName);
        DebugLog.d("dayDate=" + dayDate);

		fm = getSupportFragmentManager();

		showMessageDialog(getString(R.string.generatingInspectionForm));

		inputJumlahWargaDialog = new LovelyTextInputDialog(this, R.style.CheckBoxTintTheme)
				.setTopTitle("input jumlah warga")
				.setTopTitleColor(R.color.item_drill_red)
				.setTopColorRes(android.R.color.white)
				.setMessage(getString(R.string.warning_input_amount_warga_barang))
				.setErrorMessageColor(R.color.item_drill_red)
				.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED)
				.setInputFilter(getString(R.string.error_input_amount_warga), input -> {
					int numericInputAmount = Integer.parseInt(input);
					return numericInputAmount >= 0 && numericInputAmount <= 10 && input.charAt(0) != '0';
				});

		mSlidingLayer = findViewById(R.id.slidingLayer1);
		mSlidingLayer.setStickTo(SlidingLayer.STICK_TO_LEFT);

        // get schedule base model by scheduleid
        schedule = new ScheduleGeneral();
        schedule = schedule.getScheduleById(scheduleId);

		//generate form
		parentGroupRow = new WorkFormRowModel();
		parentGroupRow.isOpen = true;
		parentGroupRow.position = 0;
		parentGroupRow.text = "this is just";
		parentGroupRow.children = new Vector<>();

		if (workTypeName.equalsIgnoreCase(getString(R.string.corrective))) {

			int correctiveScheduleId = Integer.valueOf(scheduleId);
			CorrectiveScheduleResponseModel.CorrectiveSchedule correctiveSchedule = CorrectiveScheduleConfig.getCorrectiveSchedule(correctiveScheduleId);
			workFormGroups = new Vector<>();
			if (correctiveSchedule != null) {
				for (CorrectiveScheduleResponseModel.CorrectiveGroup correctiveGroup : correctiveSchedule.getGroup()) {

					WorkFormGroupModel groupModel = WorkFormGroupModel.getWorkFormGroupById(String.valueOf(correctiveGroup.getId()));
					workFormGroups.add(groupModel);
				}

				// get all workformgroup submenu
				DebugLog.d("get all workformgroup submenu");
				for (WorkFormGroupModel group : workFormGroups) {
					DebugLog.d("==== form group group id : " + group.id + " | " + group.name);
					WorkFormRowModel groupRow = new WorkFormRowModel();
					groupRow.work_form_group_id = group.id;
					groupRow.children = parentGroupRow.getAllItemByWorkFormGroupId(group.id);
					groupRow.text = group.name;
					groupRow.level = 0;
					parentGroupRow.children.add(groupRow); // children of
				}
			}

		} else if (workTypeName.equalsIgnoreCase(getString(R.string.foto_imbas_petir))) {

			// get workformid by worktypeid
			workForm = new WorkFormModel();
			workForm = workForm.getItemByWorkTypeId(schedule.work_type.id);

			DebugLog.d("== schedule worktype id : "+schedule.work_type.id);
			DebugLog.d("== form model id : "+workForm.id);
			DebugLog.d("== form model name : "+workForm.name);

			// get all workformgroup by workformid
			workFormGroups = WorkFormGroupModel.getAllItemByWorkFormId(workForm.id);

			checkDataWarga();

		} else{

			// get workformid by worktypeid
			workForm = new WorkFormModel();
			workForm = workForm.getItemByWorkTypeId(schedule.work_type.id);

			DebugLog.d("== schedule worktype id : "+schedule.work_type.id);
			DebugLog.d("== form model id : "+workForm.id);
			DebugLog.d("== form model name : "+workForm.name);

			// get all workformgroup by workformid
			workFormGroups = WorkFormGroupModel.getAllItemByWorkFormId(workForm.id);

			// get all workformgroup submenu
			DebugLog.d("get all workformgroup submenu");
			for (WorkFormGroupModel group : workFormGroups) {
				DebugLog.d("==== form group group id : " + group.id + " | " + group.name);
				WorkFormRowModel groupRow = new WorkFormRowModel();
				groupRow.work_form_group_id = group.id;
				groupRow.children = parentGroupRow.getAllItemByWorkFormGroupId(group.id);
				groupRow.text = group.name;
				groupRow.level = 0;
				parentGroupRow.children.add(groupRow); // children of
			}
		}

		groupFragment.setGroupActivityListener(this);
		groupFragment.setSchedule(schedule);
		groupFragment.setGroupItems(parentGroupRow);
		groupFragment.setWorkTypeName(workTypeName);
		addFragment(fm, groupFragment, R.id.fragment_behind);

		hideDialog();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		//EventBus.getDefault().register(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		//EventBus.getDefault().unregister(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onBackPressed() {
		if (mSlidingLayer.isOpened())
			mSlidingLayer.closeLayer(true);
		else {
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

	private Fragment getCurrentFragment(String tag) {

		if (fm != null) {
			return fm.findFragmentByTag(tag);
		}
		return null;
	}

	private WorkFormRowModel generateOthersModel(){
		WorkFormRowModel groupRow = new WorkFormRowModel();
		groupRow.children = new Vector<>();
		groupRow.children.add(generateOthersChildModel());
		groupRow.text = "Others";
		groupRow.level = 0;
		return groupRow;
	}
	
	private WorkFormRowModel generateOthersChildModel(){
		WorkFormRowModel groupRow = new WorkFormRowModel();
		groupRow.text = "Others Form";
		groupRow.hasForm = true;
		groupRow.level = 1;
		return groupRow;
	}

	private void checkDataWarga() {

		int dataIndex = FormImbasPetirConfig.getDataIndex(scheduleId);

		if (dataIndex != -1) {

			// found data by that scheduleid
			// check amount of warga
			generateImbasPetirGroups(dataIndex);

		} else {

			// not found data by that scheduleid
			DebugLog.d("no data config for scheduleid : " + scheduleId);
		}
	}

	public void showInputAmountWargaDialog(int dataIndex) {

		inputJumlahWargaDialog.setConfirmButton("Tambah", amountOfWarga -> {

			// insert new data warga as many as amount inputted
			MyApplication.getInstance().toast("Tambahan jumlah warga : " + amountOfWarga, Toast.LENGTH_LONG);
			FormImbasPetirConfig.insertDataWarga(dataIndex, Integer.valueOf(amountOfWarga));
			generateImbasPetirGroups(dataIndex);
			updateGroupItems();

		}).show();
	}

	public void generateImbasPetirGroups(int dataIndex) {

		ArrayList<Warga> wargas = FormImbasPetirConfig.getDataWarga(dataIndex);

		if (wargas != null) {

			int wargaSize = wargas.size();

			parentGroupRow.children = new Vector<>();

			// get all workformgroup submenu
			DebugLog.d("get all workformgroup submenu");
			for (WorkFormGroupModel group : workFormGroups) {

				DebugLog.d("==== form group group id : " + group.id + " | " + group.name);
				WorkFormRowModel groupRow = new WorkFormRowModel();
				groupRow.work_form_group_id = group.id;
				groupRow.text = group.name;
				groupRow.level = 0;

				if (group.name.equalsIgnoreCase("Warga")) {

					Vector<WorkFormRowModel> childRows = new Vector<>();

					for (int i = 0; i < wargaSize; i++) {

						Vector<WorkFormRowModel> wargaIdRows = parentGroupRow.getAllItemByWorkFormGroupId(group.id);
						WorkFormRowModel wargaIdRow;
						if (!wargaIdRows.isEmpty())
							wargaIdRow = wargaIdRows.get(0);
						else {
							wargaIdRow = new WorkFormRowModel();
							wargaIdRow.work_form_group_id = groupRow.work_form_group_id;
							wargaIdRow.text = "Id-";
							wargaIdRow.level = 1;
							wargaIdRow.hasForm = true;
							wargaIdRow.ancestry = null;
							wargaIdRow.parent_id = 0;
						}

						String wargaLabel = wargaIdRow.text;
						String wargaId	  = wargas.get(i).getWargaid();
						String wargaRowLabel = wargaLabel + wargaId;

						wargaIdRow.text = StringUtil.getIdWithName(scheduleId, wargaRowLabel, group.id);

						DebugLog.d("-- child id : " + wargaIdRow.id);
						DebugLog.d("-- child work form group id : " + wargaIdRow.work_form_group_id);
						DebugLog.d("-- child name : " + wargaIdRow.text);
						DebugLog.d("-- child level : " + wargaIdRow.level);
						DebugLog.d("-- child hasForm : " + wargaIdRow.hasForm);
						DebugLog.d("-- child ancestry : " + wargaIdRow.ancestry);
						DebugLog.d("-- child parentid : " + wargaIdRow.parent_id);
						DebugLog.d("\n\n");
						childRows.add(wargaIdRow);
					}

					// row group for "tambah warga" submenu action
					WorkFormRowModel tambahWargaRow = new WorkFormRowModel();
					tambahWargaRow.id = -1;
					tambahWargaRow.work_form_group_id = group.id;
					tambahWargaRow.hasForm = false;
					tambahWargaRow.text = "Tambah warga";
					tambahWargaRow.level = 1;
					tambahWargaRow.ancestry = null;
					tambahWargaRow.parent_id = 0;

					childRows.add(tambahWargaRow);

					groupRow.children = childRows;

				} else {
					groupRow.children = parentGroupRow.getAllItemByWorkFormGroupId(group.id);
				}

				parentGroupRow.children.add(groupRow);
			}
		}
	}

	private void updateGroupItems() {

		currentFragment = getCurrentFragment(GroupFragment.class.getSimpleName());
		if (currentFragment instanceof GroupFragment) {

			DebugLog.d("current fragment = navigation fragment");
			((GroupFragment) currentFragment).setItems(parentGroupRow);

		}
	}
}