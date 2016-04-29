package com.sap.inspection;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sap.inspection.tools.DebugLog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class UpdateActivity extends BaseActivity {

	// button to show progress dialog
	Button btnShowProgress;

	// Progress Dialog
	private ProgressDialog pDialog;
	SharedPreferences prefs;
	// Progress dialog type (0 - for Horizontal progress bar)
	public static final int progress_bar_type = 0; 

	// File url to download
	private static String file_url = "http://api.androidhive.info/progressdialog/hive.jpg";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DebugLog.d("");
		setContentView(R.layout.activity_update);
		TextView title = (TextView) findViewById(R.id.header_title);
		title.setText("Application Update");
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		file_url = prefs.getString(this.getString(R.string.url_update), "");

		// show progress bar button
		btnShowProgress = (Button) findViewById(R.id.btnProgressBar);
		// Image view to show image after downloading
		/**
		 * Show Progress bar click event
		 * */
		btnShowProgress.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (file_url.equalsIgnoreCase(""))
					finish();
				new DownloadFileFromURL().execute(file_url);
			}
		});

		Button cancel = (Button) findViewById(R.id.cancel);

		cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	/**
	 * Showing Dialog
	 * */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case progress_bar_type: // we set this to 0
			pDialog = new ProgressDialog(this);
			pDialog.setMessage("Downloading file. Please wait...");
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

	/**
	 * Background Async Task to download file
	 * */
	class DownloadFileFromURL extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread
		 * Show Progress Bar Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showDialog(progress_bar_type);
		}

		/**
		 * Downloading file in background thread
		 * */
		@Override
		protected String doInBackground(String... f_url) {
			int count;
			try {
				URL url = new URL(f_url[0]);
				URLConnection conection = url.openConnection();
				conection.connect();
				// this will be useful so that you can show a tipical 0-100% progress bar
				int lenghtOfFile = conection.getContentLength();

				// download the file
				InputStream input = new BufferedInputStream(url.openStream(), 8192);

				File tempDir= Environment.getExternalStorageDirectory();
				tempDir=new File(tempDir.getAbsolutePath()+"/Download");
				if(!tempDir.exists())
				{
					tempDir.mkdir();
				}

				// Output stream
				OutputStream output = new FileOutputStream(tempDir.getAbsolutePath()+"/sapInspection"+prefs.getString(UpdateActivity.this.getString(R.string.latest_version), "")+".apk");

				byte data[] = new byte[1024];

				long total = 0;

				while ((count = input.read(data)) != -1) {
					total += count;
					// publishing the progress....
					// After this onProgressUpdate will be called
					publishProgress(""+(int)((total*100)/lenghtOfFile));

					// writing data to file
					output.write(data, 0, count);
				}

				// flushing output
				output.flush();

				// closing streams
				output.close();
				input.close();

			} catch (Exception e) {
				Log.e("Error: ", e.getMessage());
			}

			return null;
		}

		/**
		 * Updating progress bar
		 * */
		protected void onProgressUpdate(String... progress) {
			// setting progress percentage
			pDialog.setProgress(Integer.parseInt(progress[0]));
		}

		/**
		 * After completing background task
		 * Dismiss the progress dialog
		 * **/

		@Override
		protected void onPostExecute(String file_url) {
			dismissDialog(progress_bar_type);

			File tempFile= Environment.getExternalStorageDirectory();
			tempFile=new File(tempFile.getAbsolutePath()+"/Download/sapInspection"+prefs.getString(UpdateActivity.this.getString(R.string.latest_version), "")+".apk");
			if(!tempFile.exists())
			{
				finish();
			}

			Intent intent = new Intent(Intent.ACTION_VIEW)
			.setDataAndType(Uri.fromFile(tempFile),"application/vnd.android.package-archive");
			intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent); 

		}
	}

}