package com.sap.inspection.view.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.sap.inspection.R;
import com.sap.inspection.TowerApplication;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.mainmenu.MainMenuFragment;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.view.ui.fragments.ScheduleFragment;
import com.slidinglayer.SlidingLayer;

public class MainActivity extends BaseActivity {

	private SlidingLayer mSlidingLayer;
	public static final int REQUEST_CODE = 100;

	private MainMenuFragment mainMenuFragment = MainMenuFragment.newInstance();
	private ScheduleFragment scheduleFragment = ScheduleFragment.newInstance();
	private int lastClicked = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		boolean isLoadSchedule   = getIntent().getBooleanExtra(Constants.LOADSCHEDULE,false);
		boolean isLoadAfterLogin = getIntent().getBooleanExtra(Constants.LOADAFTERLOGIN,false);

		DebugLog.d("Constants.LOADSCHEDULE : " + isLoadSchedule);
		DebugLog.d("Constants.LOADAFTERLOGIN : " + isLoadAfterLogin);

		if (isLoadSchedule) {

			DebugLog.d("load schedule");
			if (!TowerApplication.getInstance().getDEVICE_REGISTRATION_STATE()) {

				// haven't yet register device, do device registration
				DebugLog.d("start device registration....");
				requestReadPhoneStatePermission();

			}
			downloadSchedules();

		} else if (isLoadAfterLogin) {

			DebugLog.d("load after login");
			setFlagScheduleSaved(true);
		}

		mSlidingLayer = findViewById(R.id.slidingLayer1);
		mSlidingLayer.setStickTo(SlidingLayer.STICK_TO_LEFT);

		LayoutParams rlp = (LayoutParams) mSlidingLayer.getLayoutParams();
		rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		rlp.width = LayoutParams.MATCH_PARENT;

		mainMenuFragment.setMainMenuClickListener(mainMenuClick);

		replaceFragmentWith(mainMenuFragment, R.id.fragment_behind);
		replaceFragmentWith(scheduleFragment, R.id.fragment_front);

		checkLatestAPKVersion();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (lastClicked != -1){
			scheduleFragment.setScheduleBy(lastClicked);
		}
	}

	@Override
	public void onBackPressed() {
		if (mSlidingLayer.isOpened())
			mSlidingLayer.closeLayer(true);
		else
			finish();
	}

	OnClickListener mainMenuClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			TowerApplication.getInstance().setIS_CHECKING_HASIL_PM(false);
			int i = (Integer) v.getTag();
			switch (i) {
				case R.string.schedule:
					DebugLog.d("schedule");
					scheduleFragment.setScheduleBy(R.string.schedule);
					break;
				case R.string.site_audit:
					DebugLog.d("site audit");
					scheduleFragment.setScheduleBy(R.string.site_audit);
					break;
				case R.string.preventive:
					DebugLog.d("preventive");
					scheduleFragment.setScheduleBy(R.string.preventive);
					break;
				case R.string.corrective:
					DebugLog.d("corrective");
					scheduleFragment.setScheduleBy(R.string.corrective);
					break;
				case R.string.newlocation:
					DebugLog.d("new location");
					scheduleFragment.setScheduleBy(R.string.newlocation);
					break;
				case R.string.fiber_optic:
					DebugLog.d("fiber optik");
					scheduleFragment.setScheduleBy(R.string.fiber_optic);
					break;
				case R.string.colocation:
					DebugLog.d("colocation");
					scheduleFragment.setScheduleBy(R.string.colocation);
					break;
				case R.string.hasil_PM:
					DebugLog.d("hasil PM");
					scheduleFragment.setScheduleBy(R.string.hasil_PM);
					break;
				case R.string.settings:
					DebugLog.d("settings");
					Intent intent = new Intent(activity, SettingActivity.class);
					startActivity(intent);
					return;
				default:
					break;
			}
			mSlidingLayer.openLayer(true);
		}
	};
}