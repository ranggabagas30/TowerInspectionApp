package com.sap.inspection;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

import com.sap.inspection.constant.Constants;
import com.sap.inspection.event.UploadProgressEvent;
import com.sap.inspection.fragments.BaseFragment;
import com.sap.inspection.fragments.NavigationFragment;
import com.sap.inspection.listener.FormActivityListener;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.ScheduleGeneral;
import com.sap.inspection.model.form.RowModel;
import com.sap.inspection.model.form.WorkFormGroupModel;
import com.sap.inspection.model.form.WorkFormModel;
import com.sap.inspection.tools.DebugLog;
import com.slidinglayer.SlidingLayer;

import java.util.Vector;

import de.greenrobot.event.EventBus;

public class FormActivity extends BaseActivity implements FormActivityListener{

	private SlidingLayer mSlidingLayer;
	public static final int REQUEST_CODE = 100;
	private RowModel rowModel = null;
	private WorkFormModel workFormModel;
	private Vector<WorkFormGroupModel> workFormGroupModels;
	private String dayDate;
	private ScheduleBaseModel scheduleBaseModels;
	private boolean usingCheckin;
	private ProgressDialog dialog;

	ViewPager pager;
//	FragmentsAdapter fragmentsAdapter;
	NavigationFragment navigationFragment = NavigationFragment.newInstance();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		usingCheckin = MyApplication.getInstance().isScheduleNeedCheckIn();

		/*if (!DbRepository.getInstance().getDB().isOpen() && !usingCheckin) {

			DbRepository.getInstance().open(MyApplication.getInstance());
		}*/

		DebugLog.d("");

		dialog = new ProgressDialog(activity);
		//String generatingInspectionForm
		dialog.setCancelable(false);
		dialog.setMessage(getString(R.string.generatingInspectionForm));
		dialog.show();
		//get data bundle from ScheduleFragment
		Bundle bundle = getIntent().getExtras();

		dayDate = bundle.getString("dayDate");

		DebugLog.d("scheduleId="+bundle.getString(Constants.scheduleId));
		DebugLog.d("siteId="+bundle.getInt("siteId"));
		DebugLog.d("workTypeId="+bundle.getInt("workTypeId"));
		DebugLog.d("dayDate="+dayDate);

		RowModel rModel = new RowModel();
		DebugLog.d("===================================1 row model max level : "+rModel.getMaxLevel("1"));
		DebugLog.d("===================================2 row model max level : "+rModel.getMaxLevel("2"));
		DebugLog.d("===================================3 row model max level : "+rModel.getMaxLevel("3"));
		
		scheduleBaseModels = new ScheduleGeneral();
		scheduleBaseModels = scheduleBaseModels.getScheduleById(bundle.getString(Constants.scheduleId));
		DebugLog.d("===================================4 worktype id : "+scheduleBaseModels.work_type.id);
						//penambahan debug tester untuk form_id
//		DebugLog.d("===================================4 workform id : "+scheduleBaseModels.work_form.id);

		workFormModel = new WorkFormModel();
		workFormModel = workFormModel.getItemByWorkTypeId(scheduleBaseModels.work_type.id);
		DebugLog.d("===================================4 form model max level : "+workFormModel.id);
		DebugLog.d("===================================4 form model : "+workFormModel.name);
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
			DebugLog.d("===================================4 form group model max level : "+model.id+" | "+model.name);
			RowModel groupRow = new RowModel();
			groupRow.work_form_group_id = model.id;
			groupRow.children = rowModel.getAllItemByWorkFormGroupId(model.id);
			groupRow.text = model.name;
			groupRow.level = 0;
			rowModel.children.add(groupRow);
		}

		dialog.dismiss();

		navigationFragment.setFormActivityListener(this);
		navigationFragment.setSchedule(scheduleBaseModels);
		navigationFragment.setNavigationModel(rowModel);
		navigateToFragment(navigationFragment, R.id.fragment_behind);
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
		/*if (DbRepository.getInstance().getDB().isOpen() && !usingCheckin)
			DbRepository.getInstance().close();*/
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
		{
			if (MyApplication.getInstance().isScheduleNeedCheckIn()) {
				MyApplication.getInstance().toast("Checkout success", Toast.LENGTH_SHORT);
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