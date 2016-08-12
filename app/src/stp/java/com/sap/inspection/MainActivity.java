package com.sap.inspection;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.arifariyan.baseassets.fragment.BaseFragment;
import com.google.gson.Gson;
import com.sap.inspection.connection.APIHelper;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.constant.GlobalVar;
import com.sap.inspection.event.DeleteAllScheduleEvent;
import com.sap.inspection.event.ScheduleTempProgressEvent;
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
import com.sap.inspection.task.ScheduleSaver;
import com.sap.inspection.task.ScheduleTempSaver;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.tools.PrefUtil;
import com.slidinglayer.SlidingLayer;

import java.io.IOException;
import java.io.InputStream;

import de.greenrobot.event.EventBus;

public class MainActivity extends BaseActivity{

	private SlidingLayer mSlidingLayer;
	public static final int REQUEST_CODE = 100;

	private ViewPager pager;
	private int trying = 0;
	private static String formVersion;

	AlertDialogManager alert = new AlertDialogManager();

	private MainMenuFragment mainMenuFragment = MainMenuFragment.newInstance();
	private ScheduleFragment scheduleFragment = ScheduleFragment.newInstance();
	private BaseFragment currentFragment;

	private ProgressDialog progressDialog;
	private boolean flagScheduleSaved = false;
	private boolean flagFormSaved = false;
	private int lastClicked = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DebugLog.d("");
		setContentView(R.layout.activity_main);

		progressDialog = new ProgressDialog(this);
		progressDialog.setCancelable(false);

		if (getIntent().getBooleanExtra(Constants.LOADSCHEDULE,false)) {
			progressDialog.setMessage("Get schedule from server");
			APIHelper.getSchedules(activity, scheduleHandlerTemp, getPreference(R.string.user_id, ""));
			try {
				progressDialog.show();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else if (getIntent().getBooleanExtra(Constants.LOADAFTERLOGIN,false)) {
			if (GlobalVar.getInstance().anyNetwork(activity)) {
				DbRepository.getInstance().open(activity);
				try {
					progressDialog.show();
				} catch (Exception e) {
					e.printStackTrace();
				}
				checkAPKVersion();
			} else
				setFlagScheduleSaved(true);
		}

		mSlidingLayer = (SlidingLayer) findViewById(R.id.slidingLayer1);
		mSlidingLayer.setStickTo(SlidingLayer.STICK_TO_LEFT);
		LayoutParams rlp = (LayoutParams) mSlidingLayer.getLayoutParams();
		rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		rlp.width = LayoutParams.MATCH_PARENT;
		mainMenuFragment.setMainMenuClickListener(mainMenuClick);

		navigateToFragment(mainMenuFragment, R.id.fragment_behind);
		navigateToFragment(scheduleFragment, R.id.fragment_front);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (lastClicked != -1){
			scheduleFragment.setScheduleBy(lastClicked);
		}
		EventBus.getDefault().register(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		EventBus.getDefault().unregister(this);
	}

	private void offlineSchedule(){
		byte[] buffer = null;
		InputStream is;
		try {
			is = this.getAssets().open("schedules.txt");
			int size = is.available();
			buffer = new byte[size];
			is.read(buffer);
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String bufferString = new String(buffer);
		Gson gson = new Gson();
		if (bufferString != null){
			ScheduleResponseModel scheduleResponseModel = gson.fromJson(bufferString, ScheduleResponseModel.class);
			if (scheduleResponseModel.status == 200){
				ScheduleSaver scheduleSaver = new ScheduleSaver();
				scheduleSaver.setMainActivity(MainActivity.this);
				scheduleSaver.execute(scheduleResponseModel.data.toArray());
			}
		}
	}

	Handler scheduleHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			Bundle bundle = msg.getData();
			Gson gson = new Gson();
			if (bundle.getString("json") != null){
				ScheduleResponseModel scheduleResponseModel = gson.fromJson(bundle.getString("json"), ScheduleResponseModel.class);
				if (scheduleResponseModel.status == 200){
					ScheduleSaver scheduleSaver = new ScheduleSaver();
					scheduleSaver.setMainActivity(MainActivity.this);
					scheduleSaver.execute(scheduleResponseModel.data.toArray());
				}
			}else{
				setFlagScheduleSaved(true);
				Toast.makeText(activity, "Can't get schedule from server\nPlease do relogin and have fast internet connection",Toast.LENGTH_LONG).show();
			}
		}
	};

	Handler scheduleHandlerTemp = new Handler() {
		public void handleMessage(android.os.Message msg) {
			Bundle bundle = msg.getData();
			Gson gson = new Gson();
			if (bundle.getString("json") != null) {
				ScheduleResponseModel scheduleResponseModel = gson.fromJson(bundle.getString("json"), ScheduleResponseModel.class);
				if (scheduleResponseModel.status == 200) {
					DeleteAllScheduleEvent deleteAllScheduleEvent = new DeleteAllScheduleEvent();
					deleteAllScheduleEvent.scheduleResponseModel = scheduleResponseModel;
					EventBus.getDefault().post(deleteAllScheduleEvent);
				}
			} else {
				progressDialog.dismiss();
				Toast.makeText(activity, "Can't get schedule from server\nPlease get an fast internet connection", Toast.LENGTH_LONG).show();
			}
		}
	};

	public void onEvent(ScheduleTempProgressEvent event) {
		if (event.done) {
			if (DbRepository.getInstance().getDB() != null && DbRepository.getInstance().getDB().isOpen())
				DbRepository.getInstance().close();
			progressDialog.dismiss();
			Toast.makeText(activity, "Schedule Updated", Toast.LENGTH_SHORT).show();
		} else
			progressDialog.setMessage("saving schedule " + event.progress + " %...");
	}

	public void onEvent(DeleteAllScheduleEvent event) {
		if (DbRepository.getInstance().getDB()!=null) {
			if (DbRepository.getInstance().getDB().isOpen())
				DbRepository.getInstance().clearData(DbManager.mSchedule);
			else {
				DbRepository.getInstance().open(activity);
				DbRepository.getInstance().clearData(DbManager.mSchedule);
			}
		}

		ScheduleTempSaver scheduleSaver = new ScheduleTempSaver();
		scheduleSaver.setActivity(activity);
		scheduleSaver.execute(event.scheduleResponseModel.data.toArray());
	}

	private void navigateToFragment(BaseFragment fragment,int viewContainerResId) {
		if (fragment.equals(currentFragment))
			return;
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(viewContainerResId, fragment);
		//		ft.addToBackStack(null);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.commit();
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
//				Toast.makeText(activity, "Coming soon",Toast.LENGTH_LONG).show();
				scheduleFragment.setScheduleBy(R.string.corrective);
				break;
			case R.string.newlocation:
				DebugLog.d("new location");
				scheduleFragment.setScheduleBy(R.string.newlocation);
				break;
			case R.string.colocation:
				DebugLog.d("colocation");
				scheduleFragment.setScheduleBy(R.string.colocation);
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

	public void setProgressDialogMessage(String from,String message){
		//		if(from.equalsIgnoreCase("schedule"))
		progressDialog.setMessage(message);
		//		else if(isFlagScheduleSaved())
		//			progressDialog.setMessage(message);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
	}

	//	public void setFlagFormSaved(boolean flagFormSaved) {
	//		this.flagFormSaved = flagFormSaved;
	//		progressDialog.setMessage("Get schedules from server");
	//		if (this.flagScheduleSaved){
	//			progressDialog.dismiss();
	//			DbRepository.getInstance().close();
	//		}
	//	}

	public void setFlagScheduleSaved(boolean flagScheduleSaved) {
		this.flagScheduleSaved = flagScheduleSaved;
		//		progressDialog.setMessage("Generating forms");
		//		if (this.flagFormSaved){
		progressDialog.dismiss();
		if (DbRepository.getInstance().getDB() != null && DbRepository.getInstance().getDB().isOpen())
			DbRepository.getInstance().close();
		//		}
	}

	public boolean isFlagFormSaved() {
		return flagFormSaved;
	}

	public boolean isFlagScheduleSaved() {
		return flagScheduleSaved;
	}

	private class FormSaver extends AsyncTask<Object, Integer, Void>{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog.setMessage("Prepare for saving");
			DbRepository.getInstance().clearData(DbManager.mWorkFormItem);
			DbRepository.getInstance().clearData(DbManager.mWorkFormOption);
			DbRepository.getInstance().clearData(DbManager.mWorkFormColumn);
			DbRepository.getInstance().clearData(DbManager.mWorkFormRow);
			DbRepository.getInstance().clearData(DbManager.mWorkFormRowCol);
		}

		@Override
		protected Void doInBackground(Object... params) {
			int sum = 0;
			for (int i = 0; i < params.length; i++) {
				if (((WorkFormModel)params[i]).groups != null)
					for (WorkFormGroupModel group : ((WorkFormModel)params[i]).groups) {
						if (group.table == null){
							continue;
						}
						DebugLog.d("group name : "+group.name);
						DebugLog.d("group table : "+group.table.toString());
						DebugLog.d("group table header : "+group.table.headers.toString());
						sum += group.table.headers.size();
						sum += group.table.rows.size();
					}
			}

			int curr = 0;
			for (int i = 0; i < params.length; i++) {
				((WorkFormModel)params[i]).save();
				if (((WorkFormModel)params[i]).groups != null)
					for (WorkFormGroupModel group : ((WorkFormModel)params[i]).groups) {
						if (group.table == null){
							continue;
						}
						for (ColumnModel columnModel : group.table.headers) {
							curr ++;
							publishProgress(curr*100/sum);
							columnModel.save();
						}

						for (RowModel rowModel : group.table.rows) {
							curr ++;
							publishProgress(curr*100/sum);
							rowModel.save();
						}
					}
			}
			DebugLog.d("version saved : "+PrefUtil.getStringPref(R.string.user_id, "")+getString(R.string.latest_version_form));
			DebugLog.d("version saved value : "+getPreference(PrefUtil.getStringPref(R.string.user_id, "")+getString(R.string.latest_version_form), "no value"));
			DebugLog.d("version saved value from web: "+formVersion);
			writePreference(PrefUtil.getStringPref(R.string.user_id, "")+getString(R.string.latest_version_form), formVersion);
			writePreference(PrefUtil.getStringPref(R.string.user_id, "")+getString(R.string.offline_form),"not null");
			DebugLog.d("form ofline user pref: "+PrefUtil.getStringPref(R.string.user_id, "")+getString(R.string.offline_form));
			DebugLog.d("form ofline user : "+getPreference(PrefUtil.getStringPref(R.string.user_id, "")+getString(R.string.offline_form), null));
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			DebugLog.d("saving forms "+values[0]+" %...");
			progressDialog.setMessage("saving forms "+values[0]+" %...");
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			//			setFlagFormSaved(true);
			progressDialog.setMessage("Get schedule from server");
			APIHelper.getSchedules(activity, scheduleHandler, getPreference(R.string.user_id, ""));
		}
	}

	private void initFormOffline(){
		WorkFormModel form = new WorkFormModel();
		DbRepository.getInstance().open(this);
		if (form.countItem() != 0){
			//			DbRepository.getInstance().close();
			//			setFlagFormSaved(true);
			return;
		}
		byte[] buffer = null;
		InputStream is;
		try {
			is = this.getAssets().open("forms_14_10_2014.txt");
			int size = is.available();
			buffer = new byte[size];
			is.read(buffer);
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String bufferString = new String(buffer);
		//		progress_dialog.setMessage("initialize default form template");
		//		progress_dialog.setCancelable(false);
		//		progress_dialog.show();
		initForm(bufferString);
	}

	private void initForm(String json){
		Gson gson = new Gson();
		FormResponseModel formResponseModel = gson.fromJson(json,FormResponseModel.class);
		if (formResponseModel.status == 200){
			FormSaver formSaver = new FormSaver();
			formSaver.execute(formResponseModel.data.toArray());
		}
	}

	private void checkAPKVersion(){
		DebugLog.d("check apk version");
		progressDialog.setMessage("Check application version");
		APIHelper.getAPKVersion(activity, apkHandler, getPreference(R.string.user_id, ""));
	}

	private Handler apkHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if (msg.getData() != null && msg.getData().getString("json") != null){
				VersionModel model = new Gson().fromJson(msg.getData().getString("json"), VersionModel.class);
				writePreference(R.string.latest_version, model.version);
				writePreference(R.string.url_update, model.download);
				String version = null;
				try {
					version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
					DebugLog.d(version);
				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (!version.equalsIgnoreCase(getPreference(R.string.latest_version, "")) && !getPreference(R.string.url_update, "").equalsIgnoreCase("")){
					Toast.makeText(activity, "There is new update for STP Mobile Application\nPlease update the aplication from setting", Toast.LENGTH_LONG).show();
				}
			}else{
				Toast.makeText(activity, "Check application update failed\nPlease do relogin and have fast internet connection", Toast.LENGTH_LONG).show();
			}
			checkFormVersion();
			//checkFormVersionOffline();
		};
	};

	private void checkFormVersion(){
		progressDialog.setMessage("Check form version");
		APIHelper.getFormVersion(activity, formVersionHandler, getPreference(R.string.user_id, ""));
	}
	
	private void checkFormVersionOffline(){
		DebugLog.d("check form ofline user pref: "+PrefUtil.getStringPref(R.string.user_id, "")+getString(R.string.offline_form));
		DebugLog.d("check form ofline user : "+getPreference(PrefUtil.getStringPref(R.string.user_id, "")+getString(R.string.offline_form), null));
		if (getPreference(PrefUtil.getStringPref(R.string.user_id, "")+getString(R.string.offline_form), null) != null){
			progressDialog.setMessage("Get schedule from server");
			APIHelper.getSchedules(activity, scheduleHandler, getPreference(R.string.user_id, ""));
		}
		else{
			progressDialog.setMessage("Generate offline form");
			initFormOffline();
		}
	}

	private Handler formVersionHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if (msg.getData() != null && msg.getData().getString("json") != null){
				VersionModel model = new Gson().fromJson(msg.getData().getString("json"), VersionModel.class);
				formVersion = model.version;
				DebugLog.d("check version : "+PrefUtil.getStringPref(R.string.user_id, "")+getString(R.string.latest_version_form));
				DebugLog.d("check version value : "+getPreference(PrefUtil.getStringPref(R.string.user_id, "")+getString(R.string.latest_version_form), "no value"));
				DebugLog.d("check version value from web: "+formVersion);
				if (!formVersion.equals(getPreference(PrefUtil.getStringPref(R.string.user_id, "")+getString(R.string.latest_version_form), "no value"))){
					progressDialog.setMessage("Get new form from server");
					APIHelper.getForms(activity, formSaverHandler, getPreference(R.string.user_id, ""));
				}else{
					progressDialog.setMessage("Get schedule from server");
					APIHelper.getSchedules(activity, scheduleHandlerTemp, getPreference(R.string.user_id, ""));
				}
			}else{
				progressDialog.dismiss();
				Toast.makeText(activity, "Form update failed\nPlease do relogin and have fast internet connection", Toast.LENGTH_LONG).show();
			}
		};
	};

	private Handler formSaverHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if (msg.getData() != null && msg.getData().getString("json") != null){
				initForm(msg.getData().getString("json"));
			}else{
				Toast.makeText(activity, "Form update failed\nPlease do relogin and have fast internet connection", Toast.LENGTH_LONG).show();
			}
		};
	};
}