package com.sap.inspection.manager;


import android.os.AsyncTask;
import android.os.Environment;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sap.inspection.MyApplication;
import com.sap.inspection.event.DeleteAllProgressEvent;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.value.ItemValueModel;
import com.sap.inspection.tools.DebugLog;

import java.io.File;

import de.greenrobot.event.EventBus;

public class DeleteAllDataTask extends AsyncTask<Void, Integer, Void>{

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		File tempDir= Environment.getExternalStorageDirectory();
		String path = tempDir.getAbsolutePath()+"/.temp/";
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
		ItemValueModel.deleteAll(MyApplication.getInstance());
		ScheduleBaseModel.resetAllSchedule();
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
