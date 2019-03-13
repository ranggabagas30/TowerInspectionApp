package com.sap.inspection.manager;


import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sap.inspection.MyApplication;
import com.sap.inspection.event.DeleteAllProgressEvent;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.CommonUtil;

import java.io.File;

import de.greenrobot.event.EventBus;

public class DeleteAllDataTask extends AsyncTask<Void, Integer, Void>{

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		File tempDir;
		if (CommonUtil.isExternalStorageAvailable())
			tempDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Camera/");
		else
			tempDir = new File(MyApplication.getContext().getFilesDir()+"/Camera/");

		String path = tempDir.getAbsolutePath()+"/TowerInspection/";
		getFileCount(path);
		File f;
		f = new File(path);
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
		TokenModel.delete(MyApplication.getInstance());
		UserModel.delete(MyApplication.getInstance());
		RoleModel.delete(MyApplication.getInstance());
//		LoginLogModel.delete(MyApplication.getInstance());
		ScheduleBaseModel.delete(MyApplication.getInstance());
		SiteModel.delete(MyApplication.getInstance());
		OperatorModel.delete(MyApplication.getInstance());
		WorkTypeModel.delete(MyApplication.getInstance());
		WorkFormModel.delete(MyApplication.getInstance());
		WorkFormGroupModel.delete(MyApplication.getInstance());
		RowModel.delete(MyApplication.getInstance());
		ColumnModel.delete(MyApplication.getInstance());
		RowColumnModel.delete(MyApplication.getInstance());
		WorkFormItemModel.delete(MyApplication.getInstance());
		WorkFormOptionsModel.delete(MyApplication.getInstance());
		ItemValueModel.deleteAll(MyApplication.getInstance());
		CorrectiveValueModel.deleteAll(MyApplication.getInstance());
//		RowValueModel.deleteAll(MyApplication.getInstance());
		ScheduleBaseModel.resetAllSchedule();

*/
		SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
		mPref.edit().clear().commit();
		/*
		Resources r = MyApplication.getContext().getResources();
		mPref.edit().putBoolean(Constants.LOADAFTERLOGIN, false).commit();
		mPref.edit().putString(r.getString(R.string.user_name), "").commit();
		mPref.edit().putString(r.getString(R.string.password), "").commit();
		mPref.edit().putString(r.getString(R.string.user_fullname), "").commit();
		mPref.edit().putString(r.getString(R.string.user_id), "").commit();
		mPref.edit().putString(r.getString(R.string.user_authToken), "").commit();*/

		CommonUtil.clearApplicationData();

		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		EventBus.getDefault().post(new DeleteAllProgressEvent("Delete Done", true));
	}

	private int count = 0;

	private int getFileCount(String dirPath) 
	{
		if (count != 0)
			return count;
		File f = new File(dirPath);
		File[] files  = f.listFiles();

		if(files != null)
			for(int i=0; i < files.length; i++)
			{
				count ++;
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
