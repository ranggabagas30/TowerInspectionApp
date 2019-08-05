package com.sap.inspection;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.sap.inspection.connection.APIHelper;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.fragments.ScheduleFragment;
import com.sap.inspection.mainmenu.MainMenuFragment;
import com.sap.inspection.tools.DebugLog;
import com.slidinglayer.SlidingLayer;
import com.yarolegovich.lovelydialog.LovelyChoiceDialog;

import java.util.ArrayList;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks {

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
			if (!TowerApplication.getInstance().getDEVICE_REGISTER_STATE()) {

				// haven't yet register device, do device registration
				requestReadPhoneStatePermission();

			}
			showMessageDialog(getString(R.string.getScheduleFromServer));
			APIHelper.getSchedules(activity, scheduleHandler, getPreference(R.string.user_id, ""));

		} else if (isLoadAfterLogin) {

			DebugLog.d("load after login");
			setFlagScheduleSaved(true);
		}

		/**
		 * added by : Rangga
		 * date : 26/02/2019
		 * reason : every time application launched, should check app's latest version
		 *          in order to make sure that using only the latest version
		 * */
		checkAPKVersion();

		mSlidingLayer = findViewById(R.id.slidingLayer1);
		mSlidingLayer.setStickTo(SlidingLayer.STICK_TO_LEFT);
		LayoutParams rlp = (LayoutParams) mSlidingLayer.getLayoutParams();
		rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		rlp.width = LayoutParams.MATCH_PARENT;
		mainMenuFragment.setMainMenuClickListener(mainMenuClick);

		replaceFragmentWith(mainMenuFragment, R.id.fragment_behind);
		replaceFragmentWith(scheduleFragment, R.id.fragment_front);
		trackThisPage("Main");
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
				case R.string.schedule: // R.id.s1
					DebugLog.d("schedule");
					trackThisPage(getResources().getString(R.string.schedule));
					scheduleFragment.setScheduleBy(R.string.schedule);
					mSlidingLayer.openLayer(true);
					break;
				case R.string.site_audit: // R.id.s2
					DebugLog.d("site audit");
					trackThisPage(getResources().getString(R.string.site_audit));
					scheduleFragment.setScheduleBy(R.string.site_audit);
					mSlidingLayer.openLayer(true);
					break;
				case R.string.preventive: // R.id.s3
					DebugLog.d("preventive");
					trackThisPage(getResources().getString(R.string.preventive));
					scheduleFragment.setScheduleBy(R.string.preventive);
					mSlidingLayer.openLayer(true);
					break;
				case R.string.corrective: // R.id.s4
					DebugLog.d("corrective");
					trackThisPage(getResources().getString(R.string.corrective));
					scheduleFragment.setScheduleBy(R.string.corrective);
					mSlidingLayer.openLayer(true);
					break;
				case R.string.foto_imbas_petir: //R.id.s5
					DebugLog.d("foto imbas petir");
					trackEvent(getString(R.string.foto_imbas_petir));
					scheduleFragment.setScheduleBy(R.string.foto_imbas_petir);
					mSlidingLayer.openLayer(true);
					break;
				case R.string.settings: // R.id.s6
					DebugLog.d("settings");
					trackThisPage(getResources().getString(R.string.settings));
					Intent intent = new Intent(activity, SettingActivity.class);
					startActivity(intent);
					return;
				case R.string.hasil_PM: // R.id.s7
					DebugLog.d("hasil PM");
					trackEvent(getResources().getString(R.string.hasil_PM));
					scheduleFragment.setScheduleBy(R.string.hasil_PM);
					mSlidingLayer.openLayer(true);
					break;
				case R.string.routing: // R.id.s8
					DebugLog.d("routing");
					trackEvent("routing");

					ArrayList<String> routingSchedules = new ArrayList<>();
					routingSchedules.add(getString(R.string.routing_segment));
					routingSchedules.add(getString(R.string.handhole));
					routingSchedules.add(getString(R.string.hdpe));

					new LovelyChoiceDialog(MainActivity.this)
							.setTopColor(ContextCompat.getColor(MainActivity.this, R.color.theme_color))
							.setIcon(R.drawable.logo_app)
							.setTitle("Choose schedule")
							.setMessage("Please select one of routing schedules")
							.setItems(routingSchedules, (position, item) -> {
								String result = "(pos, item) : (" + position + ", " + item + ")";
								DebugLog.d(result);

								int resId;
								switch (position) {
									case 0 : resId = R.string.routing_segment; break;
									case 1 : resId = R.string.handhole; break;
									case 2 : resId = R.string.hdpe; break;
									default: resId = R.string.routing_segment;
								}

								scheduleFragment.setScheduleBy(resId);
								mSlidingLayer.openLayer(true);
								Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
							}).show();
					break;

				default:
					break;
			}
		}
	};
}