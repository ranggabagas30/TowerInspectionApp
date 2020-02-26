package com.sap.inspection.view.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Window;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.pixplicity.easyprefs.library.Prefs;
import com.sap.inspection.R;
import com.sap.inspection.TowerApplication;
import com.sap.inspection.connection.APIHelper;
import com.sap.inspection.connection.rest.TowerAPIHelper;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.constant.GlobalVar;
import com.sap.inspection.event.DeleteAllProgressEvent;
import com.sap.inspection.event.DeleteAllScheduleEvent;
import com.sap.inspection.event.ScheduleProgressEvent;
import com.sap.inspection.event.ScheduleTempProgressEvent;
import com.sap.inspection.event.UploadProgressEvent;
import com.sap.inspection.manager.ScreenManager;
import com.sap.inspection.model.DbManager;
import com.sap.inspection.model.DbRepository;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.config.formimbaspetir.CorrectiveScheduleConfig;
import com.sap.inspection.model.form.WorkFormColumnModel;
import com.sap.inspection.model.form.WorkFormGroupModel;
import com.sap.inspection.model.form.WorkFormModel;
import com.sap.inspection.model.form.WorkFormRowModel;
import com.sap.inspection.model.responsemodel.CorrectiveScheduleResponseModel;
import com.sap.inspection.model.responsemodel.FormResponseModel;
import com.sap.inspection.model.responsemodel.ScheduleResponseModel;
import com.sap.inspection.task.ScheduleSaver;
import com.sap.inspection.task.ScheduleTempSaver;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.CommonUtil;
import com.sap.inspection.util.NetworkUtil;
import com.sap.inspection.view.ui.fragments.BaseFragment;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import de.greenrobot.event.EventBus;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public abstract class BaseActivity extends AppCompatActivity {

	protected FragmentActivity activity;
	protected SharedPreferences mPref;
	protected CompositeDisposable compositeDisposable;

	public static ImageLoader imageLoader = ImageLoader.getInstance();

	// Progress dialog type (0 - for Horizontal progress bar)
	public static final int progress_bar_type = 0;

	private ProgressDialog progressDialog, pDialog;
	private boolean instanceStateSaved;
	protected boolean isUpdateAvailable = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = this;
		progressDialog = new ProgressDialog(this);
		progressDialog.setCancelable(false);
		mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		compositeDisposable = new CompositeDisposable();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		ScreenManager.getInstance().setHeight(metrics.heightPixels);
		ScreenManager.getInstance().setWidth(metrics.widthPixels);
	}

	@Override
	protected void onStart() {
		super.onStart();
		EventBus.getDefault().register(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		EventBus.getDefault().unregister(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (compositeDisposable != null) compositeDisposable.dispose();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		instanceStateSaved = true;
	}

	public void onEvent(ScheduleTempProgressEvent event) {
		if (event.done) {
			hideDialog();
		} else
			showMessageDialog("Menyimpan schedule " + event.progress + " %");
	}

    public void onEvent(ScheduleProgressEvent event) {
        if (event.done) {
            hideDialog();
        } else
            showMessageDialog("Menyimpan schedule " + event.progress + " %");
    }

	public void onEvent(DeleteAllScheduleEvent event) {
		DbRepository.getInstance().open(TowerApplication.getInstance());
		DbRepository.getInstance().clearData(DbManager.mSchedule);
		DbRepository.getInstance().close();
		ScheduleTempSaver scheduleSaver = new ScheduleTempSaver();
		scheduleSaver.setActivity(activity);
		scheduleSaver.execute(event.scheduleResponseModel.data.toArray());
	}

	public void onEvent(DeleteAllProgressEvent event) {
		if (event.done) {
			hideDialog();
			Toast.makeText(activity, event.progressString, Toast.LENGTH_SHORT).show();
			if (event.shouldRelogin)
				navigateToLoginActivity(this);
		} else {
			showMessageDialog(event.progressString);
		}
	}

	public void onEvent(UploadProgressEvent event) {
		if (!event.done)
			showMessageDialog(event.progressString);
		else {
			hideDialog();
		}
	}

	public void writePreference(int key, String value) {
		mPref.edit().putString(getString(key), value).commit();
	}
	
	public void writePreference(String key, String value) {
		mPref.edit().putString(key, value).commit();
	}
	
	public void writePreference(int key, int value) {
		mPref.edit().putInt(getString(key), value).commit();
	}
	
	public void writePreference(int key, boolean value) {
		mPref.edit().putBoolean(getString(key), value).commit();
	}
	
	public String getPreference(int key, String defaultValue) {
		return mPref.getString(getString(key), defaultValue);
	}
	
	public String getPreference(String key, String defaultValue) {
		return mPref.getString(key, defaultValue);
	}
	
	public boolean getPreference(int key, boolean defaultValue) {
		return mPref.getBoolean(getString(key), defaultValue);
	}

	protected int color(int colorRes) {
		return ContextCompat.getColor(this, colorRes);
	}

	protected static void trackEvent(String name) {
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
		FirebaseAnalytics mFirebaseAnalytics = TowerApplication.getInstance().getDefaultAnalytics();
		mFirebaseAnalytics.logEvent("track_event", bundle);
	}

	public static void trackLog(String message) {
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.VALUE, message);
		FirebaseAnalytics mFirebaseAnalytics = TowerApplication.getInstance().getDefaultAnalytics();
		mFirebaseAnalytics.logEvent("track_event", bundle);
	}

	/**
	 * Showing Dialog
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case progress_bar_type: // we set this to 0
				pDialog = new ProgressDialog(this);
				pDialog.setMessage(getString(R.string.downloadfile));
				pDialog.setIndeterminate(false);
				pDialog.setMax(100);
				pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				pDialog.setCancelable(false);
				pDialog.show();
				return pDialog;
			default:
				return null;
		}
	}

	public void showMessageDialog(String message) {
		if (progressDialog != null) {
			progressDialog.setMessage(message);
			if (!progressDialog.isShowing())
				progressDialog.show();
		}
	}

	public void hideDialog() {
		if (progressDialog != null && progressDialog.isShowing())
			progressDialog.dismiss();
	}

	protected void downloadForm(Handler handler) throws NullPointerException {
		showMessageDialog(getString(R.string.gettingnewform));
		APIHelper.getForms(activity, new Handler(message -> {
			boolean isSuccess = message.getData().getBoolean("isresponseok");
			String response = message.getData().getString("json");

			if (TextUtils.isEmpty(response)) throw new NullPointerException(getString(R.string.error_response_null));

			if (isSuccess) {
				FormResponseModel formResponse = new Gson().fromJson(response, FormResponseModel.class);
				if (formResponse.status == HttpURLConnection.HTTP_OK) {
					new FormSaver(handler).execute(formResponse.data.toArray());
				}
			}
			return false;
		}), Prefs.getString(getString(R.string.user_id), ""));
	}

	protected void downloadNewFormImbasPetir() {
		showMessageDialog(getString(R.string.gettingnewformimbaspetir));
		APIHelper.getFormImbasPetir(activity, formImbasPetirSaverHandler);
	}

	protected void downloadAndDeleteSchedules() {
		showMessageDialog(getString(R.string.getScheduleFromServer));
		APIHelper.getSchedules(activity, scheduleHandlerTemp, getPreference(R.string.user_id, ""));
	}

	protected void downloadSchedules() {
		showMessageDialog(getString(R.string.getScheduleFromServer));
		APIHelper.getSchedules(activity, scheduleHandler, getPreference(R.string.user_id, ""));
	}

	protected void downloadCorrectiveSchedules() {
		showMessageDialog("Mendapatkan Corrective Schedule dari server");
		APIHelper.getCorrectiveSchedule(activity, correctiveScheduleHandler, getPreference(R.string.user_id, ""));
	}

    protected void initFormImbasPetir(String json){
        Gson gson = new Gson();
        FormResponseModel formResponseModel = gson.fromJson(json, FormResponseModel.class);
        if (formResponseModel.status == 200) {
            new FormImbasPetirSaver(new Handler(message -> {
            	String response = message.getData().getString("response");
            	if (!TextUtils.isEmpty(response) && response.equalsIgnoreCase("success")) {
            		downloadSchedules();
				}
            	return true;
			})).execute(formResponseModel.data.toArray());
        }
    }

	/**
     *  navigation
     * */
	protected void addFragment(BaseFragment fragment, int viewContainerResId) {
		FragmentManager fm = getSupportFragmentManager();
		addFragment(fm, fragment, viewContainerResId);
	}

	protected void addFragment(FragmentManager fragmentManager, BaseFragment fragment, int viewContainerResId) {
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.add(viewContainerResId, fragment, fragment.getClass().getSimpleName());
		ft.commit();
	}

    protected void replaceFragmentWith(BaseFragment fragment, int viewContainerResId) {
		FragmentManager fm = getSupportFragmentManager();
		replaceFragmentWith(fm, fragment, viewContainerResId);
    }

	protected void replaceFragmentWith(FragmentManager fragmentManager, BaseFragment fragment, int viewContainerResId) {
		DebugLog.d("replace fragment");
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.replace(viewContainerResId, fragment);
		ft.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.commit();
	}

	public static void navigateToLoginActivity(Context context) {
		trackEvent("user_logout");
		Prefs.putBoolean(context.getString(R.string.keep_login),false);
		Intent i = new Intent(context, LoginActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		context.startActivity(i);
	}

	public static void navigateToGroupActivity(Context context, String scheduleId, int siteId, int workTypeId, String workTypeName, String dayDate) {
		Intent intent = new Intent(context, GroupActivity.class);
		intent.putExtra(Constants.KEY_SCHEDULEID, scheduleId);
		intent.putExtra(Constants.KEY_SITEID, siteId);
		intent.putExtra(Constants.KEY_DAYDATE, dayDate);
		intent.putExtra(Constants.KEY_WORKTYPEID, workTypeId);
		intent.putExtra(Constants.KEY_WORKTYPENAME, workTypeName);
		context.startActivity(intent);
	}

	public static void navigateToGroupWargaActivity(Context context, int dataIndex, String scheduleId, String parentId, int rowId, String workFormGroupId, String workFormGroupName, String workTypeName, String wargaId) {
		Intent intent = new Intent(context, GroupWargaActivity.class);
		intent.putExtra(Constants.KEY_DATAINDEX, dataIndex);
		intent.putExtra(Constants.KEY_SCHEDULEID, scheduleId);
		intent.putExtra(Constants.KEY_PARENTID, parentId);
		intent.putExtra(Constants.KEY_ROWID, rowId);
		intent.putExtra(Constants.KEY_WORKFORMGROUPID, workFormGroupId);
		intent.putExtra(Constants.KEY_WORKFORMGROUPNAME, workFormGroupName);
		intent.putExtra(Constants.KEY_WORKTYPENAME, workTypeName);
		intent.putExtra(Constants.KEY_WARGAID, wargaId);
		context.startActivity(intent);
	}

	public static void navigateToFormFillActivity(Context context, String scheduleId, int rowId, int workFormGroupId, String workFormGroupName, String workTypeName) {
		Intent intent = new Intent(context, FormFillActivity.class);
		intent.putExtra(Constants.KEY_SCHEDULEID, scheduleId);
		intent.putExtra(Constants.KEY_ROWID, rowId);
		intent.putExtra(Constants.KEY_WORKFORMGROUPID, workFormGroupId);
		intent.putExtra(Constants.KEY_WORKFORMGROUPNAME, workFormGroupName);
		intent.putExtra(Constants.KEY_WORKTYPENAME, workTypeName);
		context.startActivity(intent);
	}

	public static void navigateToFormFillActivity(Context context, String scheduleId, int rowId, int workFormGroupId, String workFormGroupName, String workTypeName, String wargaId, String barangId) {
		Intent intent = new Intent(context, FormFillActivity.class);
		intent.putExtra(Constants.KEY_SCHEDULEID, scheduleId);
		intent.putExtra(Constants.KEY_ROWID, rowId);
		intent.putExtra(Constants.KEY_WORKFORMGROUPID, workFormGroupId);
		intent.putExtra(Constants.KEY_WORKFORMGROUPNAME, workFormGroupName);
		intent.putExtra(Constants.KEY_WORKTYPENAME, workTypeName);
		intent.putExtra(Constants.KEY_WARGAID, wargaId);
		intent.putExtra(Constants.KEY_BARANGID, barangId);
		context.startActivity(intent);
	}

	public static void navigateToCheckinActivity(Context context, String userId, String scheduleId, int siteId, String dayDate, int workTypeId, String workTypeName) {
		Intent intent = new Intent(context, CheckInActivity.class);
		intent.putExtra(Constants.KEY_USERID, userId);
		intent.putExtra(Constants.KEY_SCHEDULEID, scheduleId);
		intent.putExtra(Constants.KEY_SITEID, siteId);
		intent.putExtra(Constants.KEY_DAYDATE, dayDate);
		intent.putExtra(Constants.KEY_WORKTYPEID, workTypeId);
		intent.putExtra(Constants.KEY_WORKTYPENAME, workTypeName);
		context.startActivity(intent);
	}

	/**
	 * ===== list all handlers ======
	 *
	 * */
	@SuppressLint("HandlerLeak")
	public Handler correctiveScheduleHandler = new Handler() {
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
						DebugLog.d("save corrective schedule config");
						TowerApplication.getInstance().toast("Corrective schedules data berhasil diunduh", Toast.LENGTH_SHORT);
					}
				} else {
					TowerApplication.getInstance().toast("JSON == null. Gagal mengunduh data schedule Corrective", Toast.LENGTH_LONG);
				}
			} else {
				TowerApplication.getInstance().toast("Response not OK. Gagal mengunduh data schedule Corrective", Toast.LENGTH_LONG);
			}
		}
	};

	@SuppressLint("HandlerLeak")
	Handler scheduleHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			Bundle bundle = msg.getData();
			Gson gson = new Gson();
			boolean isResponseOK = bundle.getBoolean("isresponseok");
			if (isResponseOK) {
				if (bundle.getString("json") != null) {
					ScheduleResponseModel scheduleResponseModel = gson.fromJson(bundle.getString("json"), ScheduleResponseModel.class);
					if (scheduleResponseModel.status == 200) {
						ScheduleSaver scheduleSaver = new ScheduleSaver();
						scheduleSaver.execute(scheduleResponseModel.data.toArray());
					}
				} else {
					hideDialog();
					Toast.makeText(activity, getString(R.string.error_failed_download_schedules), Toast.LENGTH_LONG).show();
				}
			} else {
				hideDialog();
				DebugLog.d("repsonse not ok");
			}
		}
	};

	@SuppressLint("HandlerLeak")
	protected Handler scheduleHandlerTemp = new Handler() {
		public void handleMessage(android.os.Message msg) {
			Bundle bundle = msg.getData();
			Gson gson = new Gson();
			if (bundle.getString("json") != null) {
				ScheduleResponseModel scheduleResponseModel = gson.fromJson(bundle.getString("json"), ScheduleResponseModel.class);
				DebugLog.d("scheduleResponseModel.status : " + scheduleResponseModel.status);
				if (scheduleResponseModel.status == 200) {
					DeleteAllScheduleEvent deleteAllScheduleEvent = new DeleteAllScheduleEvent();
					deleteAllScheduleEvent.scheduleResponseModel = scheduleResponseModel;
					EventBus.getDefault().post(deleteAllScheduleEvent);
				}
			} else {
				hideDialog();
				Toast.makeText(activity, getString(R.string.error_failed_download_schedules), Toast.LENGTH_LONG).show();
			}
		}
	};

    @SuppressLint("HandlerLeak")
    private Handler formImbasPetirSaverHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            Bundle bundle = msg.getData();
            boolean isResponseOK = bundle.getBoolean("isresponseok");
            if (isResponseOK) {
                if (bundle.getString("json") != null){
                    initFormImbasPetir(bundle.getString("json"));
                }else{
                    hideDialog();
                    Toast.makeText(activity, getString(R.string.formUpdateFailedFastInternet), Toast.LENGTH_LONG).show();
                }
            } else {
                hideDialog();
                DebugLog.d("repsonse not ok");
            }
        }
    };

	protected void updateAPK() {
		if (GlobalVar.getInstance().isNetworkOnline(this)) {
			new DownloadFileFromURL(new Handler(message -> {
				String response = message.getData().getString("response");
				if (!TextUtils.isEmpty(response) && response.equalsIgnoreCase("success")) {
					// install apk
					CommonUtil.installAPK(activity, this);
				} else {
					Toast.makeText(this, getString(R.string.error_update_apk), Toast.LENGTH_LONG).show();
				}
				return true;
			})).execute(Constants.APK_URL);
		} else {
			Toast.makeText(this, getString(R.string.error_disconnected), Toast.LENGTH_LONG).show();
		}
	}

	@SuppressLint("CheckResult")
	@RequiresPermission(Manifest.permission.READ_PHONE_STATE)
	public void sendRegIdtoServer(String token, String deviceId, String appVersion) {
		TowerAPIHelper.sendRegistrationFCMToken(token, deviceId, appVersion)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.doOnSubscribe(disposable -> compositeDisposable.add(disposable))
				.subscribe(response -> {
					if (response != null) {
						if (response.status == HttpURLConnection.HTTP_CREATED) {
							DebugLog.d("== device registration success ==");
							DebugLog.d("token : " + response.token);
							DebugLog.d("message : " + response.messages);
							DebugLog.d("app version : " + response.app_version);
							DebugLog.d("should update : " + response.should_udpate);
						}
					} else {
						DebugLog.e("== device registration failed. JSON response null ==");
					}
				}, error -> {
					String errorMsg = NetworkUtil.handleApiError(error);
					DebugLog.e("== device registration failed with error " + error.getMessage() + " ==");
				});
	}

	/**
	 * async task class
	 *
	 * */
	@SuppressLint("CheckResult")
	protected void saveSchedule(Object ... schedules) {
		Observable.create(emitter -> {
			if (schedules == null) {
				emitter.onError(new NullPointerException("Schedules kosong"));
			}
			for (int i = 0; i < schedules.length; i++) {
				int progress = (i+1)*100/schedules.length;
				emitter.onNext(progress);
				((ScheduleBaseModel)schedules[i]).save();
			}
			emitter.onNext(100);
			emitter.onComplete(); })
				.doOnSubscribe(disposable -> compositeDisposable.add(disposable))
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
						progress -> {
							DebugLog.d("-- menyimpan schedule " + progress + " % --");
							showMessageDialog("Menyimpan schedule " + progress + " %");
						}, error -> {
							hideDialog();
							Toast.makeText(this, getString(R.string.error_failed_save_schedule), Toast.LENGTH_LONG).show();
							DebugLog.e(error.getMessage(), error);
						}, () -> {
							hideDialog();
							Toast.makeText(this, getString(R.string.success_update_schedule), Toast.LENGTH_SHORT).show();
							DebugLog.d("-- complete -- ");
						}
				);

	}

	public class FormSaver extends AsyncTask<Object, Integer, Void> {
		private Handler handler;

		public FormSaver(Handler handler) {
			this.handler = handler;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showMessageDialog(getString(R.string.savingformspreparation));
			DbRepository.getInstance().open(TowerApplication.getInstance());
			DbRepository.getInstance().clearData(DbManager.mWorkFormItem);
			DbRepository.getInstance().clearData(DbManager.mWorkFormOption);
			DbRepository.getInstance().clearData(DbManager.mWorkFormColumn);
			DbRepository.getInstance().clearData(DbManager.mWorkFormRow);
			DbRepository.getInstance().clearData(DbManager.mWorkFormRowCol);
			DbRepository.getInstance().clearData(DbManager.mWorkFormGroup);
			DbRepository.getInstance().clearData(DbManager.mWorkForm);
			DbRepository.getInstance().close();
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
						for (WorkFormColumnModel workFormColumnModel : group.table.headers) {
							curr ++;
							publishProgress(curr*100/sum);
							workFormColumnModel.save();
						}

						for (WorkFormRowModel rowModel : group.table.rows) {
							curr ++;
							publishProgress(curr*100/sum);
							rowModel.save();
						}
					}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			DebugLog.d("saving forms "+values[0]+" %...");
			showMessageDialog("saving forms "+values[0]+" %...");
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			hideDialog();
			Bundle bundle = new Bundle();
			bundle.putString("response", "failed");
			Message message = new Message();
			message.setData(bundle);

			handler.sendMessage(message);
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			showMessageDialog(getString(R.string.savingformscomplete));

			Bundle bundle = new Bundle();
			bundle.putString("response", "success");
			Message message = new Message();
			message.setData(bundle);

			handler.sendMessage(message);
		}
	}

    public class FormImbasPetirSaver extends AsyncTask<Object, Integer, Void>{

		private Handler handler;

		public FormImbasPetirSaver(Handler handler) {
			this.handler = handler;
		}

		@Override
        protected void onPreExecute() {
            super.onPreExecute();
            showMessageDialog("Persiapan menyimpan form imbas petir");
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
                        for (WorkFormColumnModel workFormColumnModel : group.table.headers) {
                            curr ++;
                            publishProgress(curr*100/sum);
                            workFormColumnModel.save();
                        }

                        for (WorkFormRowModel rowModel : group.table.rows) {
                            curr ++;
                            publishProgress(curr*100/sum);
                            rowModel.save();
                        }
                    }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            DebugLog.d("saving forms "+values[0]+" %...");
            showMessageDialog("saving forms "+values[0]+" %...");
        }

		@Override
		protected void onCancelled() {
			super.onCancelled();
			hideDialog();
			Bundle bundle = new Bundle();
			bundle.putString("response", "failed");
			Message message = new Message();
			message.setData(bundle);
			handler.sendMessage(message);
		}

		@Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            hideDialog();
            Bundle bundle = new Bundle();
            bundle.putString("response", "success");
            Message message = new Message();
            message.setData(bundle);
            handler.sendMessage(message);
        }
    }

	/**
	 * Background Async Task to download file
	 */
	public class DownloadFileFromURL extends AsyncTask<String, String, Boolean> {

		private Handler handler;

		public DownloadFileFromURL(Handler handler) {
			this.handler = handler;
		}

		/**
		 * Before starting background thread
		 * Show Progress Bar Dialog
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showMessageDialog(getString(R.string.downloadfile));
		}

		/**
		 * Downloading file in background thread
		 */
		@Override
		protected Boolean doInBackground(String... f_url) {
			try {
				URL url = new URL(f_url[0]);
				URLConnection conection = url.openConnection();
				conection.connect();

				// this will be useful so that you can show a tipical 0-100% progress bar
				int lenghtOfFile = conection.getContentLength();

				File apkFile = new File(Constants.APK_FULL_PATH);
				File apkDir = new File(Constants.DOWNLOAD_PATH);
				if (!apkDir.exists()) apkDir.mkdirs();

				// download the file
				InputStream input = new BufferedInputStream(url.openStream(), 8192);
				OutputStream output = new FileOutputStream(apkFile);
				byte[] data = new byte[1024];
				long total = 0;

				DebugLog.d("start download with size " + lenghtOfFile);
				int count;
				while ((count = input.read(data, 0, 1024)) != -1) {
					total += count;
                    String totalMB = String.valueOf((int) total / 1048576);
                    String publishMessage = "Downloaded file size " + totalMB + " MB";
                    publishProgress(publishMessage);
                    output.write(data, 0, count);
                    DebugLog.d("(count, total, progress) : (" + count + ", " + total + ", " + (int) ((total * 100) / lenghtOfFile) + ")");
                }
				DebugLog.d("finish download");
				output.flush();
				output.close();
				input.close();
				return true;
			} catch (MalformedURLException mae) {
				DebugLog.e(mae.getMessage(), mae);
			} catch (IOException ioe) {
				DebugLog.e(ioe.getMessage(), ioe);
			}
			return false;
		}

		/**
		 * Updating progress bar
		 */
		protected void onProgressUpdate(String... publishMessage) {
			// setting progress percentage
            showMessageDialog(publishMessage[0]);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			hideDialog();
			Bundle bundle = new Bundle();
			bundle.putString("response", "failed");
			Message message = new Message();
			message.setData(bundle);

			handler.sendMessage(message);
		}

		/**
		 * After completing background task
		 * Dismiss the progress dialog
		 **/
		@Override
		protected void onPostExecute(Boolean isSuccessful) {
            hideDialog();
            Bundle bundle = new Bundle();
			if (!isSuccessful) bundle.putString("response", "failed");
			else bundle.putString("response", "success");

			Message message = new Message();
			message.setData(bundle);
			handler.sendMessage(message);
		}
	}
}
