//package com.sap.inspection;
//
//import android.content.Intent;
//
//import android.os.Bundle;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentTransaction;
//import android.support.v4.view.ViewPager;
//
//import com.sap.inspection.constant.Constants;
//import BaseFragment;
//import GroupFragment;
//import com.sap.inspection.listener.GroupActivityListener;
//import com.sap.inspection.model.ScheduleBaseModel;
//import com.sap.inspection.model.ScheduleGeneral;
//import com.sap.inspection.model.form.WorkFormRowModel;
//import com.sap.inspection.view.adapter.FragmentsAdapter;
//import com.slidinglayer.SlidingLayer;
//
//public class FormActivityByOperator extends BaseActivity implements GroupActivityListener{
//
//	private SlidingLayer mSlidingLayer;
//	public static final int REQUEST_CODE = 100;
//	private ScheduleBaseModel scheduleModel;
////	private FormModel formModel = generateSampleForm();
//	private WorkFormRowModel rowModel = null;
//	private String scheduleId;
//	private String workFormGroupId="1";
//
//	ViewPager pager;
//	FragmentsAdapter fragmentsAdapter;
//	GroupFragment navigationFragment = GroupFragment.newInstance();
////	DrillFragment drillFragment = DrillFragment.newInstance();
//
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		//get schedule from bundle
//		Bundle bundle = getIntent().getExtras();
//		scheduleId = bundle.getString(Constants.KEY_SCHEDULEID);
//		scheduleModel = new ScheduleGeneral();
//		scheduleModel = scheduleModel.getScheduleById(activity, scheduleId);
//		
//		setContentView(R.layout.activity_main);
//		mSlidingLayer = (SlidingLayer) findViewById(R.id.slidingLayer1);
//		mSlidingLayer.setStickTo(SlidingLayer.STICK_TO_LEFT);
////		mSlidingLayer.openLayer(true);
//		
//		rowModel = new WorkFormRowModel();
//		rowModel.isOpen = true;
//		rowModel.position = "";
//		rowModel.children = rowModel.getAllItemByWorkFormGroupId(this,workFormGroupId="1");
//		
//		log("=================================== row model child : "+rowModel.getCount());
//		for (WorkFormRowModel model : rowModel.getModels()) {
//			log("========= "+model.level+" | "+model.id+" | "+model.ancestry);
//		}
//
//		navigationFragment.setGroupActivityListener(this);
//		navigationFragment.setGroupItems(rowModel);
//		navigationFragment.setScheduleModel(scheduleModel);
//		navigationFragment.setWorkFormGroupId(workFormGroupId);
////		drillFragment.setGroupActivityListener(this);
////		replaceFragmentWith(drillFragment, R.id.fragment_behind);
////		replaceFragmentWith(navigationFragment, R.id.fragment_front);
//		replaceFragmentWith(navigationFragment, R.id.fragment_behind);
//	}
//
//	private void replaceFragmentWith(BaseFragment fragment,int viewContainerResId) {
//		FragmentManager fm = getSupportFragmentManager();
//		FragmentTransaction ft = fm.beginTransaction();
//		ft.replace(viewContainerResId, fragment);
//		ft.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
//		ft.commit();
//	}
//	
//	@Override
//	public void onBackPressed() {
//		if (mSlidingLayer.isOpened())
//			mSlidingLayer.closeLayer(true);
//		else
//			finish();
//	}
//
//	protected void onActivityResult(int requestCode, int resultCode, Intent data){
//		super.onActivityResult(requestCode, resultCode, data);
//	}
// 
//	@Override
//	public void myOnBackPressed() {
//		onBackPressed();
//	}
//
//	@Override
//	public void onShowNavigation() {
//		mSlidingLayer.openLayer(true);
//	}
//
////	public FormModel generateSample(){
////		FormModel root = new FormModel();
////		root.children = new ArrayList<FormModel>();
////		root.isOpen = true;
////		root.position = "";
////		for (int x = 1;x <= 3; x++){
////			FormModel root2 = new FormModel();
////			root2.text = " x "+x;
////			root2.position = ""+x;
////			root2.level = 1;
////			if (x % 2 == 0)
////				root2.isOpen = true;
////			root.children.add(root2);
////			root2.children = new ArrayList<FormModel>();
////			for (int y = 1;y <= 3; y++){
////				FormModel root3 = new FormModel();
////				root3.text = " x "+x + " y "+y;
////				root3.position = ""+x+y;
////				root3.level = 2;
////				root2.children.add(root3);
////				root3.children = new ArrayList<FormModel>();
////				for (int z = 1;z <= 3; z++){
////					FormModel root4 = new FormModel();
////					root4.text = " x "+x + " y "+y+ " z "+z;
////					root4.position = ""+x+y+z;
////					root4.level = 3;
////					root3.children.add(root4);
////				}
////			}
////		}
////		return root;
////	}
////
////	public FormModel generateSampleForm(){
////		FormModel root = new FormModel();
////		root.children = new ArrayList<FormModel>();
////		root.isOpen = true;
////		root.position = "";
////
////		//		1.Site Environment
////		FormModel root2 = new FormModel();
////		root2.text = "Site Environment";
////		root2.position = "1";
////		root2.level = 1;
////		root2.isOpen = false;
////		root.children.add(root2);
////		root2.children = new ArrayList<FormModel>();
////		
//////		1.1.Tower ( SST / Minitower / Monopole / Pole )
////		FormModel root3 = new FormModel();
////		root3.text = "Tower ( SST / Minitower / Monopole / Pole )";
////		root3.position = "1.1";
////		root3.level = 2;
////		root2.children.add(root3);
////		root3.children = new ArrayList<FormModel>();
////
//////		1.1.1.Nuts and bolts
////		FormModel root4 = new FormModel();
////		root4.text = "Nuts and bolts";
////		root4.position = "1.1.1";
////		root4.level = 3;
////		root3.children.add(root4);
////		
//////		1.1.2.Paint
////		root4 = new FormModel();
////		root4.text = "Paint";
////		root4.position = "1.1.2";
////		root4.level = 3;
////		root3.children.add(root4);
////		
//////		1.1.3.Verticality Test
////		root4 = new FormModel();
////		root4.text = "Verticality Test";
////		root4.position = "1.1.3";
////		root4.level = 3;
////		root3.children.add(root4);
////		
//////		1.1.4.Verticality Test
////		root4 = new FormModel();
////		root4.text = "Verticality Test";
////		root4.position = "1.1.4";
////		root4.level = 3;
////		root3.children.add(root4);
////		
//////		1.1.5.Anchor bolt installed
////		root4 = new FormModel();
////		root4.text = "Anchor bolt installed";
////		root4.position = "1.1.5";
////		root4.level = 3;
////		root3.children.add(root4);
////
//////		1.1.6.Ladder and Platform installed 1
////		root4 = new FormModel();
////		root4.text = "Ladder and Platform installed 1";
////		root4.position = "1.1.6";
////		root4.level = 3;
////		root3.children.add(root4);
////		
//////		1.1.7.Ladder and Platform installed 2
////		root4 = new FormModel();
////		root4.text = "Ladder and Platform installed 2";
////		root4.position = "1.1.7";
////		root4.level = 3;
////		root3.children.add(root4);
////		
//////		1.1.8.Ladder and Platform installed 3
////		root4 = new FormModel();
////		root4.text = "Ladder and Platform installed 3";
////		root4.position = "1.1.8";
////		root4.level = 3;
////		root3.children.add(root4);
////		
//////		1.1.9.Name Plate
////		root4 = new FormModel();
////		root4.text = "Name Plate";
////		root4.position = "1.1.9";
////		root4.level = 3;
////		root3.children.add(root4);
////		
//////		1.2.Roof Top Tower/Pole Installation
////		root3 = new FormModel();
////		root3.text = "Roof Top Tower/Pole Installation";
////		root3.position = "1.2";
////		root3.level = 2;
////		root2.children.add(root3);
////		root3.children = new ArrayList<FormModel>();
////		
//////		1.2.1.Foundation
////		root4 = new FormModel();
////		root4.text = "Foundation";
////		root4.position = "1.2.1";
////		root4.level = 3;
////		root3.children.add(root4);
////		
//////		1.2.2.Base frame
////		root4 = new FormModel();
////		root4.text = "Base frame";
////		root4.position = "1.2.2";
////		root4.level = 3;
////		root3.children.add(root4);
////		
//////		1.2.3.Anchor bolt installed
////		root4 = new FormModel();
////		root4.text = "Anchor bolt installed";
////		root4.position = "1.2.3";
////		root4.level = 3;
////		root3.children.add(root4);
////		
//////		1.2.4.Nuts and bolts
////		root4 = new FormModel();
////		root4.text = "Nuts and bolts";
////		root4.position = "1.2.4";
////		root4.level = 3;
////		root3.children.add(root4);
////		
//////		1.2.5.Paint
////		root4 = new FormModel();
////		root4.text = "Paint";
////		root4.position = "1.2.5";
////		root4.level = 3;
////		root3.children.add(root4);
////		
//////		1.3.Shelter ( CKD ) for Indoor or RBS/BBS/Nobi for Outdoor
////		root3 = new FormModel();
////		root3.text = "Shelter ( CKD ) for Indoor or RBS/BBS/Nobi for Outdoor";
////		root3.position = "1.3";
////		root3.level = 2;
////		root2.children.add(root3);
////		root3.children = new ArrayList<FormModel>();
////		
//////		1.3.1.Wall panel
////		root4 = new FormModel();
////		root4.text = "Wall panel";
////		root4.position = "1.3.1";
////		root4.level = 3;
////		root3.children.add(root4);
////		
//////		1.3.2.Painting
////		root4 = new FormModel();
////		root4.text = "Painting";
////		root4.position = "1.3.2";
////		root4.level = 3;
////		root3.children.add(root4);
////		
//////		1.3.3.Floor
////		root4 = new FormModel();
////		root4.text = "Floor";
////		root4.position = "1.3.3";
////		root4.level = 3;
////		root3.children.add(root4);
////		
//////		1.3.4.Doors, handles, hinges & keys
////		root4 = new FormModel();
////		root4.text = "Doors, handles, hinges & keys";
////		root4.position = "1.3.4";
////		root4.level = 3;
////		root3.children.add(root4);
////		
//////		1.3.5.Doors, handles, hinges & keys
////		root4 = new FormModel();
////		root4.text = "Doors, handles, hinges & keys";
////		root4.position = "1.3.5";
////		root4.level = 3;
////		root3.children.add(root4);
////		
//////		1.3.6.Roof water proofing
////		root4 = new FormModel();
////		root4.text = "Roof water proofing";
////		root4.position = "1.3.6";
////		root4.level = 3;
////		root3.children.add(root4);
////		
//////		1.3.7.Ceiling
////		root4 = new FormModel();
////		root4.text = "Ceiling";
////		root4.position = "1.3.7";
////		root4.level = 3;
////		root3.children.add(root4);
////		
//////		1.3.8.Concrete Foundation
////		root4 = new FormModel();
////		root4.text = "Concrete Foundation";
////		root4.position = "1.3.8";
////		root4.level = 3;
////		root3.children.add(root4);
////		
//////		1.4.Fence and Gate
////		root3 = new FormModel();
////		root3.text = "Fence and Gate";
////		root3.position = "1.4";
////		root3.level = 2;
////		root2.children.add(root3);
////		root3.children = new ArrayList<FormModel>();
////		
//////		1.4.1.Wall surface
////		root4 = new FormModel();
////		root4.text = "Wall surface";
////		root4.position = "1.4.1";
////		root4.level = 3;
////		root3.children.add(root4);
////
//////		1.4.2.BRC Panel & Fence Post
////		root4 = new FormModel();
////		root4.text = "BRC Panel & Fence Post";
////		root4.position = "1.4.2";
////		root4.level = 3;
////		root3.children.add(root4);
////
//////		1.4.3.Painting
////		root4 = new FormModel();
////		root4.text = "Painting";
////		root4.position = "1.4.1";
////		root4.level = 3;
////		root3.children.add(root4);
////
//////		1.4.4.Nuts and bolts
////		root4 = new FormModel();
////		root4.text = "Nuts and bolts";
////		root4.position = "1.4.4";
////		root4.level = 3;
////		root3.children.add(root4);
////
//////		1.4.5.Door Gate and hinges/sliding railway
////		root4 = new FormModel();
////		root4.text = "Door Gate and hinges/sliding railway";
////		root4.position = "1.4.5";
////		root4.level = 3;
////		root3.children.add(root4);
////
//////		1.4.6.Barbed Wire
////		root4 = new FormModel();
////		root4.text = "Barbed Wire";
////		root4.position = "1.4.6";
////		root4.level = 3;
////		root3.children.add(root4);
////
//////		1.4.7.Padlock and Keys
////		root4 = new FormModel();
////		root4.text = "Padlock and Keys";
////		root4.position = "1.4.7";
////		root4.level = 3;
////		root3.children.add(root4);
////		
//////		1.5.Yard
////		root3 = new FormModel();
////		root3.text = "Yard";
////		root3.position = "1.5";
////		root3.level = 2;
////		root2.children.add(root3);
////		root3.children = new ArrayList<FormModel>();
////		
//////		1.5.1.Concrete Block
////		root4 = new FormModel();
////		root4.text = "Concrete Block";
////		root4.position = "1.5.1";
////		root4.level = 3;
////		root3.children.add(root4);
////
//////		1.5.2.Garden Light
////		root4 = new FormModel();
////		root4.text = "Garden Light";
////		root4.position = "1.5.2";
////		root4.level = 3;
////		root3.children.add(root4);
////
//////		1.6.Access Road
////		root3 = new FormModel();
////		root3.text = "Access Road";
////		root3.position = "1.6";
////		root3.level = 2;
////		root2.children.add(root3);
////		root3.children = new ArrayList<FormModel>();
////		
//////		1.6.1.Concrete Road/Concrete Block/Asphalt Mix
////		root4 = new FormModel();
////		root4.text = "Concrete Road/Concrete Block/Asphalt Mix";
////		root4.position = "1.6.1";
////		root4.level = 3;
////		root3.children.add(root4);
////
//////		1.6.2.Concrete bridge for acces ( if any )
////		root4 = new FormModel();
////		root4.text = "Concrete bridge for acces ( if any )";
////		root4.position = "1.6.2";
////		root4.level = 3;
////		root3.children.add(root4);
////
//////==============================================================================
////		
//////		2.Mechanical/Electrical Works
////		root2 = new FormModel();
////		root2.text = "Mechanical/Electrical Works";
////		root2.position = "2";
////		root2.level = 1;
////		root2.isOpen = false;
////		root.children.add(root2);
////		root2.children = new ArrayList<FormModel>();
////		
//////		2.1.Pole KWH Panel
////		root3 = new FormModel();
////		root3.text = "Pole KWH Panel";
////		root3.position = "2.1";
////		root3.level = 2;
////		root2.children.add(root3);
////		root3.children = new ArrayList<FormModel>();
////
//////		2.1.1.Pole
////		root4 = new FormModel();
////		root4.text = "Pole";
////		root4.position = "2.1.1";
////		root4.level = 3;
////		root3.children.add(root4);
////		
//////		2.1.2.Cable Condition
////		root4 = new FormModel();
////		root4.text = "Cable Condition";
////		root4.position = "2.1.2";
////		root4.level = 3;
////		root3.children.add(root4);
////		
//////		2.1.3.Hanger Strut and Clamps
////		root4 = new FormModel();
////		root4.text = "Hanger Strut and Clamps";
////		root4.position = "2.1.3";
////		root4.level = 3;
////		root3.children.add(root4);
////		
//////		2.2.Phase Voltage
////		root3 = new FormModel();
////		root3.text = "Phase Voltage";
////		root3.position = "2.2";
////		root3.level = 2;
////		root2.children.add(root3);
////		root3.children = new ArrayList<FormModel>();
////		
//////		2.2.1.Phase to Phase Voltage (3 Phase)
////		root4 = new FormModel();
////		root4.text = "Phase to Phase Voltage (3 Phase)";
////		root4.position = "2.2.1";
////		root4.level = 3;
////		root3.children.add(root4);
////
//////		2.2.2.Phase to Phase Voltage (3 Phase)
////		root4 = new FormModel();
////		root4.text = "Phase to Phase Voltage (3 Phase)";
////		root4.position = "2.2.2";
////		root4.level = 3;
////		root3.children.add(root4);
////		
//////		2.2.3.Ground to Neutral
////		root4 = new FormModel();
////		root4.text = "Ground to Neutral";
////		root4.position = "2.2.3";
////		root4.level = 3;
////		root3.children.add(root4);
////		
//////		2.3.KWH Panel
////		root3 = new FormModel();
////		root3.text = "KWH Panel";
////		root3.position = "2.3";
////		root3.level = 2;
////		root2.children.add(root3);
////		root3.children = new ArrayList<FormModel>();
////		
//////		2.3.1.Measurement Reading Result on KWH meter
////		root4 = new FormModel();
////		root4.text = "Measurement Reading Result on KWH meter";
////		root4.position = "2.3.1";
////		root4.level = 3;
////		root3.children.add(root4);
////		
//////		2.3.2.Functionality on KWH meter
////		root4 = new FormModel();
////		root4.text = "Functionality on KWH meter";
////		root4.position = "2.3.2";
////		root4.level = 3;
////		root3.children.add(root4);
////		
////		
////		
////		return root;
////	}
//
//
//}