package com.sap.inspection.connection;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

import com.crashlytics.android.Crashlytics;
import com.sap.inspection.R;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.StringUtil;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.LinkedList;

public class JSONConnectionPUT extends AsyncTask<Void, Void, String>{

	//	private Activity activity;
	private String url;
	private Handler handler;
	private String json;
	private String errMsg;
	private int statusCode= 0;
	private LinkedList<NameValuePair> params;
	private boolean notJson = false;
	private Context context;
	private HttpPut request;

	public JSONConnectionPUT(Context context, String url,Handler handler,LinkedList<NameValuePair> params) {
		//		this.activity = activity;
		this.params = params;
		this.url = url;
		this.handler = handler;
		this.context = context;
	}

	public JSONConnectionPUT(Context context, String url,Handler handler,NameValuePair param) {
		this.params = new LinkedList<NameValuePair>();
		this.params.add(param);
		this.url = url;
		this.handler = handler;
		this.context = context;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		//		activity.setProgressBarIndeterminateVisibility(true);
	}

	@Override
	protected String doInBackground(Void... arg0) {
		try {
			HttpParams httpParameters = new BasicHttpParams();

			// Set the timeout in milliseconds until a connection is established.
			// The default value is zero, that means the timeout is not used. 
			int timeoutConnection = 3000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			// Set the default socket timeout (SO_TIMEOUT) 
			// in milliseconds which is the timeout for waiting for data.
			int timeoutSocket = 5000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

			HttpClient client = new DefaultHttpClient(httpParameters);
			HttpPut request = new HttpPut(url);
			request.setHeader("Content-Type", "application/x-www-form-urlencoded");
			
			SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(context);
		    if (mPref.getString(context.getString(R.string.user_cookie), null) != null){
				request.setHeader("Cookie", mPref.getString(context.getString(R.string.user_cookie), ""));
			}

			if (params != null) {
				UrlEncodedFormEntity entity = null;
				entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
				request.setEntity(entity);
			}

			HttpResponse response;
			InputStream data = null;
			response = client.execute(request);
			
			//pull cookie
			Header cookie = response.getFirstHeader("Set-Cookie");
			if (cookie != null){
				mPref.edit().putString(context.getString(R.string.user_cookie), cookie.getValue()).commit();
			}
			
			statusCode = response.getStatusLine().getStatusCode();
			data = response.getEntity().getContent();
			DebugLog.d("content type name  : "+response.getEntity().getContentType().getName());
			DebugLog.d("content type value : "+response.getEntity().getContentType().getValue());
			if (!StringUtil.checkIfContentTypeJson(response.getEntity().getContentType().getValue())){
				DebugLog.d("not json type");
				DebugLog.d(ConvertInputStreamToString(data));
				notJson = true;
				return null;
			}
			String s = ConvertInputStreamToString(data);
			DebugLog.d("json /n"+s);
			return s;
		}catch (SocketTimeoutException e) {
			errMsg = e.getMessage();
			e.printStackTrace();
			DebugLog.d("err ||||| "+errMsg);
			//			ErrorManager.getInstance().setError(errMsg);
			//			ErrorManager.getInstance().setKindError(ErrorManager.TIMEOUT_EXCEPTION);
		}
		catch (ClientProtocolException e) {
			errMsg = e.getMessage();
			e.printStackTrace();
			DebugLog.d("err ||||| "+errMsg);
			//			ErrorManager.getInstance().setError(errMsg);
			//			ErrorManager.getInstance().setKindError(ErrorManager.UNHANDLED_EXEPTION);
		}
		catch (IOException e) {
			errMsg = e.getMessage();
			e.printStackTrace();
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

			//Toast.makeText(context, R.string.failed_feature_not_supported_or_removed_from_server, Toast.LENGTH_LONG).show();
			Crashlytics.log("response from server is not json format and result is null");
		}
		else if (result != null) {
			try {
				if (JSONConnection.anyServerError(result, context))
					result = null;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		DebugLog.d(url);
		Bundle bundle = new Bundle();
		bundle.putString("json", result);
		bundle.putString("url", url);
		bundle.putString("methode", "PUT");
		bundle.putInt("statusCode", statusCode);
		Message msg = new Message();
		msg.setData(bundle);
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
}
