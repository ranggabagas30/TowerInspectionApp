package com.sap.inspection;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import com.arifariyan.baseassets.fragment.BaseFragment;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.fragments.NavigationFragment;
import com.sap.inspection.listener.FormActivityListener;
import com.sap.inspection.model.DbRepository;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.ScheduleGeneral;
import com.sap.inspection.model.WorkTypeModel;
import com.sap.inspection.model.form.RowModel;
import com.sap.inspection.model.form.WorkFormGroupModel;
import com.sap.inspection.model.form.WorkFormModel;
import com.sap.inspection.tools.DebugLog;
import com.slidinglayer.SlidingLayer;

import java.util.Vector;

public class FormActivity extends BaseActivity implements FormActivityListener{

	private SlidingLayer mSlidingLayer;
	public static final int REQUEST_CODE = 100;
	private RowModel rowModel = null;
	private String workFormGroupId="1";
	private WorkTypeModel workTypeModel;
	private WorkFormModel workFormModel;
	private Vector<WorkFormGroupModel> workFormGroupModels;
	private String dayDate;
	private ScheduleBaseModel scheduleBaseModels;

	ViewPager pager;
//	FragmentsAdapter fragmentsAdapter;
	NavigationFragment navigationFragment = NavigationFragment.newInstance();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DebugLog.d("");

		ProgressDialog dialog = new ProgressDialog(activity);
		dialog.setMessage("Please wait... \n Generating Inspection Form...");
		dialog.show();
		
		//get data from bundle
		Bundle bundle = getIntent().getExtras();
		
		dayDate = bundle.getString("dayDate");

		DebugLog.d("scheduleId="+bundle.getString(Constants.scheduleId));
		DebugLog.d("siteId="+bundle.getInt("siteId"));
		DebugLog.d("workTypeId="+bundle.getInt("workTypeId"));
		DebugLog.d("dayDate="+dayDate);

		DbRepository.getInstance().open(activity);

		RowModel rModel = new RowModel();
		log("===================================1 row model max level : "+rModel.getMaxLevel("1"));
		log("===================================2 row model max level : "+rModel.getMaxLevel("2"));
		log("===================================3 row model max level : "+rModel.getMaxLevel("3"));
		
		scheduleBaseModels = new ScheduleGeneral();
		scheduleBaseModels = scheduleBaseModels.getScheduleById(bundle.getString("scheduleId"));
		log("===================================4 worktype id : "+scheduleBaseModels.work_type.id);
		workFormModel = new WorkFormModel();
		workFormModel = workFormModel.getItemByWorkTypeId(scheduleBaseModels.work_type.id);
		log("===================================4 form model max level : "+workFormModel.id);
		log("===================================4 form model : "+workFormModel.name);
		WorkFormGroupModel groupModel = new WorkFormGroupModel();
		workFormGroupModels = groupModel.getAllItemByWorkFormId(workFormModel.id);
		
		
		setContentView(R.layout.activity_main);
		mSlidingLayer = (SlidingLayer) findViewById(R.id.slidingLayer1);
		mSlidingLayer.setStickTo(SlidingLayer.STICK_TO_LEFT);
		
		//generate form
		rowModel = new RowModel();
		rowModel.isOpen = true;
		rowModel.position = 0;
		rowModel.text = "this is just a root place holder";
		rowModel.children = new Vector<RowModel>();
		for (WorkFormGroupModel model : workFormGroupModels) {
			log("===================================4 form group model max level : "+model.id+" | "+model.name);
//			rowModel.children.addAll(rowModel.getAllItemByWorkFormGroupId(model.id));
			RowModel groupRow = new RowModel();
			groupRow.children = rowModel.getAllItemByWorkFormGroupId(model.id);
			groupRow.text = model.name;
			groupRow.level = 0;
			rowModel.children.add(groupRow);
		}
		
//		rowModel.children.add(generateOthersModel());
		DbRepository.getInstance().close();
//		for (RowModel model : rowModel.getModels()) {
//			log("========= "+model.level+" | "+model.id+" | "+model.ancestry);
//		}

		dialog.dismiss();

		navigationFragment.setFormActivityListener(this);
		navigationFragment.setSchedule(scheduleBaseModels);
		navigationFragment.setNavigationModel(rowModel);
		navigationFragment.setWorkFormGroupId(workFormGroupId);
		navigateToFragment(navigationFragment, R.id.fragment_behind);
	}

	private void navigateToFragment(BaseFragment fragment, int viewContainerResId) {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(viewContainerResId, fragment);
		ft.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.commit();
	}
	
	@Override
	public void onBackPressed() {
		if (mSlidingLayer.isOpened())
			mSlidingLayer.closeLayer(true);
		else
			finish();
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
	
	private RowModel generateOthersModel(){
		RowModel groupRow = new RowModel();
		groupRow.children = new Vector<RowModel>();
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
	
}