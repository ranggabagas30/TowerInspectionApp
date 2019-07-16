package com.sap.inspection;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.sap.inspection.connection.APIHelper;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.constant.GlobalVar;
import com.sap.inspection.event.ScheduleProgressEvent;
import com.sap.inspection.fragments.BaseFragment;
import com.sap.inspection.fragments.ScheduleFragment;
import com.sap.inspection.mainmenu.MainMenuFragment;
import com.sap.inspection.manager.AlertDialogManager;
import com.sap.inspection.model.DbManager;
import com.sap.inspection.model.DbRepository;
import com.sap.inspection.model.form.ColumnModel;
import com.sap.inspection.model.form.RowModel;
import com.sap.inspection.model.form.WorkFormGroupModel;
import com.sap.inspection.model.form.WorkFormModel;
import com.sap.inspection.model.responsemodel.FormResponseModel;
import com.sap.inspection.model.responsemodel.ScheduleResponseModel;
import com.sap.inspection.model.responsemodel.VersionModel;
import com.sap.inspection.task.DownloadFileFromURL;
import com.sap.inspection.task.ScheduleSaver;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.PrefUtil;
import com.sap.inspection.util.PermissionUtil;
import com.sap.inspection.util.CommonUtil;
import com.sap.inspection.view.dialog.DialogUtil;
import com.slidinglayer.SlidingLayer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
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
			if (!MyApplication.getInstance().getDEVICE_REGISTER_STATE()) {

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

		mSlidingLayer = (SlidingLayer) findViewById(R.id.slidingLayer1);
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
			MyApplication.getInstance().setIS_CHECKING_HASIL_PM(false);
			int i = (Integer) v.getTag();
			switch (i) {
				case R.string.schedule: // R.id.s1
					DebugLog.d("schedule");
					trackThisPage(getResources().getString(R.string.schedule));
					scheduleFragment.setScheduleBy(R.string.schedule);
					break;
				case R.string.site_audit: // R.id.s2
					DebugLog.d("site audit");
					trackThisPage(getResources().getString(R.string.site_audit));
					scheduleFragment.setScheduleBy(R.string.site_audit);
					break;
				case R.string.preventive: // R.id.s3
					DebugLog.d("preventive");
					trackThisPage(getResources().getString(R.string.preventive));
					scheduleFragment.setScheduleBy(R.string.preventive);
					break;
				case R.string.corrective: // R.id.s4
					DebugLog.d("corrective");
					trackThisPage(getResources().getString(R.string.corrective));
					scheduleFragment.setScheduleBy(R.string.corrective);
					break;
				case R.string.foto_imbas_petir: //R.id.s5
					DebugLog.d("foto imbas petir");
					trackEvent(getString(R.string.foto_imbas_petir));
					scheduleFragment.setScheduleBy(R.string.foto_imbas_petir);
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
					break;
				case R.string.routing: // R.id.s8
					DebugLog.d("routing");
					trackEvent("routing");
					DialogUtil.singleChoiceScheduleRoutingDialog(MainActivity.this);
					break;

				default:
					break;
			}
			mSlidingLayer.openLayer(true);
		}
	};
}