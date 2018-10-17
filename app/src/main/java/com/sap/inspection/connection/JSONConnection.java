package com.sap.inspection.connection;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.utils.IoUtils;
import com.sap.inspection.MyApplication;
import com.sap.inspection.R;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.model.ErrorSatutempatModel;
import com.sap.inspection.model.responsemodel.BaseResponseModel;
import com.sap.inspection.tools.DebugLog;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;

public class JSONConnection extends AsyncTask<Void, Void, String>{

	//	private Activity activity;
	private String url;
	private Handler handler;
	private String json;
	private String errMsg;
	private int statusCode = 0;
	private boolean notJson = false;
	private Context context;
	private HttpClient client;
	private HttpGet request;
	private HttpResponse response;
	private InputStream data;

	public JSONConnection(Context context, String url,Handler handler) {
		this.url = url;
		this.handler = handler;
		this.context = context;
	}

	@Override
	protected void onPreExecute() {
		DebugLog.d("url="+url);
		super.onPreExecute();
		//		activity.setProgressBarIndeterminateVisibility(true);
	}

	@Override
	protected String doInBackground(Void... arg0) {
		try {
			DebugLog.d("GET JSON URL");
			HttpParams httpParameters = new BasicHttpParams();

			/*// Set the timeout in milliseconds until a connection is established.
			// The default value is zero, that means the timeout is not used. 
			int timeoutConnection = 6000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			// Set the default socket timeout (SO_TIMEOUT) 
			// in milliseconds which is the timeout for waiting for data.
			int timeoutSocket = 6000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
*/
			client = new DefaultHttpClient(httpParameters);
			try {

				request = new HttpGet(url);
			} catch (Exception e) {
				e.printStackTrace();
				MyApplication.getInstance().toast("URL tidak benar. Periksa kembali", Toast.LENGTH_SHORT);
			}

			SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(context);
			DebugLog.d("cookie = "+mPref.getString(context.getString(R.string.user_cookie), ""));
			if (mPref.getString(context.getString(R.string.user_cookie), null) != null){
				request.addHeader("Cookie", mPref.getString(context.getString(R.string.user_cookie), ""));
			}
			
//			request.setHeader("Content-Type", "application/x-www-form-urlencoded");
			DebugLog.d("execute request ... ");
			response = client.execute(request);
			
			//pull cookie
			Header cookie = response.getFirstHeader("Set-Cookie");
			if (cookie != null){
				mPref.edit().putString(context.getString(R.string.user_cookie), cookie.getValue()).commit();
			}
			
			data = response.getEntity().getContent();
			statusCode = response.getStatusLine().getStatusCode();
			DebugLog.d("content type name  : "+ response.getEntity().getContentType().getName());
			DebugLog.d("content type value : "+ response.getEntity().getContentType().getValue());
			if (!JSONConnection.checkIfContentTypeJson(response.getEntity().getContentType().getValue())){
				DebugLog.d("not json type");
				DebugLog.e(ConvertInputStreamToString(data));
				notJson = true;
				return null;
			}
			String s = ConvertInputStreamToString(data);
			DebugLog.d("json = "+s);

			return s;
		}catch (SocketTimeoutException e) {
			errMsg = e.getMessage();
			e.printStackTrace();
			Crashlytics.log(Log.ERROR, "jsonconnection", e.getMessage());
			DebugLog.d("err ||||| "+errMsg);
			//			ErrorManager.getInstance().setError(errMsg);
			//			ErrorManager.getInstance().setKindError(ErrorManager.TIMEOUT_EXCEPTION);
		}
		catch (ClientProtocolException e) {
			errMsg = e.getMessage();
			e.printStackTrace();
			Crashlytics.log(Log.ERROR, "jsonconnection", e.getMessage());
			DebugLog.d("err ||||| "+errMsg);
			//			ErrorManager.getInstance().setError(errMsg);
			//			ErrorManager.getInstance().setKindError(ErrorManager.UNHANDLED_EXEPTION);
		}
		catch (IOException e) {
			errMsg = e.getMessage();
			e.printStackTrace();
			Crashlytics.log(Log.ERROR, "jsonconnection", e.getMessage());
			DebugLog.d("err ||||| "+errMsg);
			//			ErrorManager.getInstance().setError(errMsg);
			//			ErrorManager.getInstance().setKindError(ErrorManager.UNHANDLED_EXEPTION);
		}

		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		//		setJson(result);
		if (notJson && result == null) {

			//Toast.makeText(context, R.string.feature_not_supported_or_removed_from_server, Toast.LENGTH_LONG).show();
			Crashlytics.log("response from server is not json format and result is null");
		}
		else if (result != null) {
				BaseResponseModel responseModel = new Gson().fromJson(result, BaseResponseModel.class);
					if (responseModel.status == 422 || responseModel.status == 403 || responseModel.status == 404) {
						Crashlytics.log(Log.ERROR, "jsonconnection", "error status code : " + responseModel.status);
						MyApplication.getInstance().toast(responseModel.messages, Toast.LENGTH_LONG);
					}

/*
			try {
				if (JSONConnection.anyServerError(result, context))
					result = null;
			} catch (JSONException e) {
				e.printStackTrace();
			}*/

			//perubahan irwan
		}

		DebugLog.d(url);
		Bundle bundle = new Bundle();
		bundle.putString("json", result);
		bundle.putString("url", url);
		bundle.putString("methode", "GET");
		bundle.putInt("statusCode", statusCode);
		Message msg = new Message();
		msg.setData(bundle);
		if (handler!=null)
			handler.sendMessage(msg);
		//		activity.setProgressBarIndeterminateVisibility(false);
	}
	
	
	@Override
	protected void onCancelled(String result) {
		super.onCancelled(result);
		request.abort();
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		request.abort();
	}
	public static String ConvertInputStreamToString(InputStream is) {
		String str = null;
		byte[] b = null;
		try {
			StringBuffer buffer = new StringBuffer();
			b = new byte[4096];
			for (int n; (n = is.read(b)) != -1;) {
				buffer.append(new String(b, 0, n));
				DebugLog.d("== (b:" + b + ";b.length:" + b.length + ";n:" + n );
			}
			str = buffer.toString();

		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return str;
	}

	public void setJson(String json) {
		this.json = json;
	}

	public String getJson() {
		return json;
	}
	
	public static boolean checkIfContentTypeJson(String contentType){
		int idxSemiColon = contentType.indexOf(Constants.JSON_CONTENT_TYPE);
		DebugLog.d(contentType + " | " + idxSemiColon);
		return idxSemiColon != -1;
	}
	
	public static boolean anyServerError(String json, Context context) throws JSONException{
    	JSONObject jsonObj = new JSONObject(json);
    	JSONObject dataObj = jsonObj.optJSONObject("data");
    	if (dataObj == null)
    		return false;
		String data = jsonObj.getString("data");
		Gson gson = new Gson();
		ErrorSatutempatModel model = gson.fromJson(data,ErrorSatutempatModel.class);
		if (model != null && model.error_type != null ){
			Toast.makeText(context, model.errors, Toast.LENGTH_LONG).show();
			return true;
		}
		return false;
    }
}