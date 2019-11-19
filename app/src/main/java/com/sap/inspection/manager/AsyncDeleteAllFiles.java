package com.sap.inspection.manager;


import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sap.inspection.BuildConfig;
import com.sap.inspection.TowerApplication;
import com.sap.inspection.connection.APIHelper;
import com.sap.inspection.connection.rest.TowerAPIHelper;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.event.DeleteAllProgressEvent;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.value.FormValueModel;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.CommonUtil;

import org.apache.http.HttpStatus;
import org.apache.http.protocol.HTTP;

import java.io.File;

import de.greenrobot.event.EventBus;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class AsyncDeleteAllFiles extends AsyncTask<Void, Integer, Void>{

	private String mPath = Constants.DIR_PHOTOS + "/";
	private ScheduleBaseModel mSchedule;
	private CompositeDisposable compositeDisposable;

	public AsyncDeleteAllFiles() {}

	public AsyncDeleteAllFiles(ScheduleBaseModel schedule) {
		this.mSchedule = schedule;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		compositeDisposable = new CompositeDisposable();
	}

	@Override
	protected Void doInBackground(Void... arg0) {

		// delete files
		if (mSchedule != null && !TextUtils.isEmpty(mSchedule.id))
			mPath += mSchedule.id + "/";

		getFileCount(mPath);
		File f = new File(mPath);
		File[] files  = f.listFiles();
		publishProgress(count);

		if (files != null)
			for(int i=0; i < files.length; i++)
			{
				publishProgress(count - i);
				File file = files[i];
				DebugLog.d("delete file : "+file.getAbsolutePath());
				file.delete();
			}

		if (mSchedule == null) {// clear all data
			clearImageCache(); // clear image loader cache
			FormValueModel.deleteAll(); // clear form value data
			CommonUtil.clearApplicationData(); // clear application cache
			SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(TowerApplication.getContext());
			mPref.edit().clear().apply(); // clear shared pref
		} else {
			if (!TextUtils.isEmpty(mSchedule.id))
				FormValueModel.deleteAllBy(mSchedule.id);
		}
		return null;
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		compositeDisposable.dispose();
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);

		// after deleting files, then delete all data in table
		if (mSchedule == null) {
			//FormValueModel.deleteAll();
            EventBus.getDefault().post(new DeleteAllProgressEvent("Success delete all data", true, true));
        } else {
            //FormValueModel.deleteAllBy(mScheduleId);
			if (!TextUtils.isEmpty(mSchedule.id)) {
				if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP) && mSchedule.work_type.name.matches(Constants.regexFOCUT)) {
					compositeDisposable.add(
							TowerAPIHelper.deleteSchedule(mSchedule.id)
										 .subscribeOn(Schedulers.io())
										 .observeOn(AndroidSchedulers.mainThread())
							 			 .subscribe(
							 			 		response -> {
							 			 			if (response.status == HttpStatus.SC_OK) {
							 			 				ScheduleBaseModel.delete(mSchedule.id);
														EventBus.getDefault().post(new DeleteAllProgressEvent(response.messages, true, false));
													} else {
														EventBus.getDefault().post(new DeleteAllProgressEvent("Failed (error code: " + response.status + ")", true, false));
													}
												}, error ->  {
													 EventBus.getDefault().post(new DeleteAllProgressEvent("Failed delete schedule", true, false));
													 DebugLog.e(error.getMessage(), error);
												 }
										 )
					);
				} else {
					EventBus.getDefault().post(new DeleteAllProgressEvent("Success delete all " + mSchedule.id + " data", true, false));
				}
			} else {
				EventBus.getDefault().post(new DeleteAllProgressEvent("Failed delete schedule. Schedule id not found", true, false));
			}
        }
	}

	private int count = 0;

	private int getFileCount(String dirPath) {
		if (count != 0)
			return count;
		File f = new File(dirPath);
		File[] files  = f.listFiles();

		if(files != null)
			for(int i=0; i < files.length; i++)
			{
				count++;
				File file = files[i];
				if(file.isDirectory())
				{   
					getFileCount(file.getAbsolutePath()); 
				}
			}
		return count;
	}

	private void clearImageCache(){
		ImageLoader.getInstance().clearDiscCache();
		ImageLoader.getInstance().clearMemoryCache();
	}

	String progress;

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		progress = values[0]+" items deleted from "+count;
		EventBus.getDefault().post(new DeleteAllProgressEvent(progress));
	}


}
