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
import com.sap.inspection.util.StringUtil;

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
	private final String TAG = JSONConnection.class.getSimpleName();
	private String url;
	private Handler handler;
	private String response;
	private int statusCode = 0;
	private boolean notJson = false;
	private boolean isResponseOK = true;
	private Context context;
	private HttpClient client;
	private HttpGet httpRequest;
	private HttpResponse httpResponse;
	private InputStream data;

	public JSONConnection(Context context, String url, Handler handler) {
		this.url = url;
		this.handler = handler;
		this.context = context;
	}

	@Override
	protected void onPreExecute() {
		DebugLog.d("url = " + url);
		super.onPreExecute();
	}

	@Override
	protected String doInBackground(Void... arg0) {
		try {
			DebugLog.d("GET JSON URL");
			HttpParams httpParameters = new BasicHttpParams();

			client = new DefaultHttpClient(httpParameters);
			try {

				httpRequest = new HttpGet(url);
			} catch (Exception e) {
				e.printStackTrace();
				MyApplication.getInstance().toast("URL tidak benar. Periksa kembali", Toast.LENGTH_SHORT);

				isResponseOK = false;
				response = e.getMessage();
				return response;
			}

			SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(context);
			DebugLog.d("cookie = "+mPref.getString(context.getString(R.string.user_cookie), ""));
			if (mPref.getString(context.getString(R.string.user_cookie), null) != null){
				httpRequest.addHeader("Cookie", mPref.getString(context.getString(R.string.user_cookie), ""));
			}

			DebugLog.d("execute request ... ");
			httpResponse = client.execute(httpRequest);
			
			//pull cookie
			Header cookie = httpResponse.getFirstHeader("Set-Cookie");
			if (cookie != null){
				mPref.edit().putString(context.getString(R.string.user_cookie), cookie.getValue()).commit();
			}
			
			data = httpResponse.getEntity().getContent();
			statusCode = httpResponse.getStatusLine().getStatusCode();

			DebugLog.d("content type name  : "+ httpResponse.getEntity().getContentType().getName() == null ? "null" : httpResponse.getEntity().getContentType().getName());
			DebugLog.d("content type value : "+ httpResponse.getEntity().getContentType().getValue() == null ? "null" : httpResponse.getEntity().getContentType().getValue());

			if (!StringUtil.checkIfContentTypeJson(httpResponse.getEntity().getContentType().getValue())){

				isResponseOK = false;
				notJson = true;

				DebugLog.d("not json type");
			}

			response = StringUtil.ConvertInputStreamToString(data);

		} catch (NullPointerException npe) {

			isResponseOK = false;
			response = npe.getMessage();
			npe.printStackTrace();

		} catch (SocketTimeoutException e) {

			isResponseOK = false;
			response = e.getMessage();
			e.printStackTrace();

		}
		catch (ClientProtocolException e) {

			isResponseOK = false;
			response = e.getMessage();
			e.printStackTrace();

		}
		catch (IOException e) {

			isResponseOK = false;
			response = e.getMessage();
			e.printStackTrace();

		}

		return response;
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);

		if (isResponseOK) {

			BaseResponseModel responseModel = new Gson().fromJson(result, BaseResponseModel.class);
			if (responseModel.status == 422 || responseModel.status == 403 || responseModel.status == 404) {
				Crashlytics.log(Log.ERROR, "jsonconnection", "error status code : " + responseModel.status);
				MyApplication.getInstance().toast(responseModel.messages, Toast.LENGTH_LONG);
			}

		} else {

			if (notJson) {

				MyApplication.getInstance().toast(context.getString(R.string.notjsontype), Toast.LENGTH_LONG);
				Crashlytics.log(context.getString(R.string.notjsontype) + " = " + result);

			} else {

				MyApplication.getInstance().toast("error : " + result, Toast.LENGTH_LONG);
				Crashlytics.log(Log.ERROR, TAG, result);

			}
		}

		Bundle bundle = new Bundle();
		bundle.putString("json", result);
		bundle.putString("url", url);
		bundle.putString("methode", "GET");
		bundle.putInt("statusCode", statusCode);
		bundle.putBoolean("isresponseok", isResponseOK);
		Message msg = new Message();
		msg.setData(bundle);

		if (handler!=null) {

			DebugLog.d("handler is not null");
			handler.sendMessage(msg);
		} else {
			DebugLog.d("handler is null");
		}
	}
	
	
	@Override
	protected void onCancelled(String result) {
		super.onCancelled(result);
		httpRequest.abort();
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		httpRequest.abort();
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