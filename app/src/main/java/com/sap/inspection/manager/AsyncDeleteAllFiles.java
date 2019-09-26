package com.sap.inspection.manager;


import android.os.AsyncTask;
import android.text.TextUtils;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.event.DeleteAllProgressEvent;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.value.FormValueModel;
import com.sap.inspection.tools.DebugLog;

import java.io.File;

import de.greenrobot.event.EventBus;

public class AsyncDeleteAllFiles extends AsyncTask<Void, Integer, Void>{

	private String mPath = Constants.DIR_PHOTOS + "/";
	private String mScheduleId;

	public AsyncDeleteAllFiles() {}

	public AsyncDeleteAllFiles(String scheduleId) {
		this.mScheduleId = scheduleId;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected Void doInBackground(Void... arg0) {

		if (!TextUtils.isEmpty(mScheduleId))
			mPath += mScheduleId + "/";

		getFileCount(mPath);
		File f = new File(mPath);
		File[] files  = f.listFiles();
		publishProgress(count);
		clearImageCache();
		if (files != null)
			for(int i=0; i < files.length; i++)
			{
				publishProgress(count - i);
				File file = files[i];
				DebugLog.d("delete file : "+file.getAbsolutePath());
				file.delete();
			}
		/*
		TokenModel.delete(TowerApplication.getInstance());
		UserModel.delete(TowerApplication.getInstance());
		RoleModel.delete(TowerApplication.getInstance());
//		LoginLogModel.delete(TowerApplication.getInstance());
		ScheduleBaseModel.delete(TowerApplication.getInstance());
		SiteModel.delete(TowerApplication.getInstance());
		OperatorModel.delete(TowerApplication.getInstance());
		WorkTypeModel.delete(TowerApplication.getInstance());
		WorkFormModel.delete(TowerApplication.getInstance());
		WorkFormGroupModel.delete(TowerApplication.getInstance());
		WorkFormRowModel.delete(TowerApplication.getInstance());
		ColumnModel.delete(TowerApplication.getInstance());
		RowColumnModel.delete(TowerApplication.getInstance());
		WorkFormItemModel.delete(TowerApplication.getInstance());
		WorkFormOptionsModel.delete(TowerApplication.getInstance());
		FormValueModel.deleteAll(TowerApplication.getInstance());
		CorrectiveValueModel.deleteAll(TowerApplication.getInstance());
//		RowValueModel.deleteAll(TowerApplication.getInstance());
		ScheduleBaseModel.resetAllSchedule();

*/
		/*
		Resources r = TowerApplication.getContext().getResources();
		mPref.edit().putBoolean(Constants.LOADAFTERLOGIN, false).commit();
		mPref.edit().putString(r.getString(R.string.user_name), "").commit();
		mPref.edit().putString(r.getString(R.string.password), "").commit();
		mPref.edit().putString(r.getString(R.string.user_fullname), "").commit();
		mPref.edit().putString(r.getString(R.string.user_id), "").commit();
		mPref.edit().putString(r.getString(R.string.user_authToken), "").commit();*/

		if (TextUtils.isEmpty(mScheduleId)) {// if there is no specific dir by scheduleId, then clear all application data
			/*SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(TowerApplication.getContext());
			mPref.edit().clear().commit();
			CommonUtil.clearApplicationData();*/
			ScheduleBaseModel.delete();
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);

		// after deleting files, then delete all data in table
		if (TextUtils.isEmpty(mScheduleId)) {

			FormValueModel.deleteAll();
            EventBus.getDefault().post(new DeleteAllProgressEvent("Delete Done", true, true));

        } else {

            FormValueModel.deleteAllBy(mScheduleId);
            EventBus.getDefault().post(new DeleteAllProgressEvent("Delete Done", true, false));
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
