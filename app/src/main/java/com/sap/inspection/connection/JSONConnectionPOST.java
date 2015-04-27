package com.sap.inspection.connection;

import java.io.IOException;

import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.LinkedList;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.sap.inspection.R;

public class JSONConnectionPOST extends AsyncTask<Void, Void, String>{

	//	private Activity activity;
	private String url;
	private Handler handler;
	private String json;
	private String errMsg;
	private LinkedList<NameValuePair> params;
	private int statusCode = 0;
	private boolean notJson = false;
	private Context context;
	private HttpClient client;
	private HttpPost request;
	private HttpResponse response;
	private InputStream data;

	public JSONConnectionPOST(Context context, String url,Handler handler,LinkedList<NameValuePair> params) {
		//		this.activity = activity;
		this.params = params;
		this.url = url;
		this.handler = handler;
		this.context = context;
	}

	public JSONConnectionPOST(Context context, String url,Handler handler,NameValuePair param) {
		this.params = new LinkedList<NameValuePair>();
		this.params.add(param);
		this.url = url;
		this.handler = handler;
		this.context = context;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		Log.d(getClass().getName(), "=============== post preexecute do BG");
	}

	@Override
	protected String doInBackground(Void... arg0) {
		try {
			Log.d(getClass().getName(), "=============== post do BG");
			HttpParams httpParameters = new BasicHttpParams();
			// Set the timeout in milliseconds until a connection is established.
			// The default value is zero, that means the timeout is not used. 
//			int timeoutConnection = 3000;
			int timeoutConnection = 120000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			// Set the default socket timeout (SO_TIMEOUT) 
			// in milliseconds which is the timeout for waiting for data.
//			int timeoutSocket = 5000;
			int timeoutSocket = 120000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

			client = new DefaultHttpClient(httpParameters);
			request = new HttpPost(url);
            Log.d(getClass().getName(), "POST "+url);
			request.setHeader("Content-Type", "application/x-www-form-urlencoded");
			SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(context);
			if (mPref.getString(context.getString(R.string.user_cookie), null) != null){
				request.setHeader("Cookie", mPref.getString(context.getString(R.string.user_cookie), ""));
			}
			
			UrlEncodedFormEntity entity = null;
			entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
			request.setEntity(entity);

			Log.d(getClass().getName(), "=============== before response");
			response = client.execute(request);
			
			for (Header header : response.getAllHeaders()){
				Log.d(getClass().getName(),header.getName()+" ||| "+header.getValue());
			}
			
			//pull cookie
			Header cookie = response.getFirstHeader("Set-Cookie");
			if (cookie != null){
				mPref.edit().putString(context.getString(R.string.user_cookie), cookie.getValue()).commit();
			}
			
			Log.d(getClass().getName(), "=============== after response");
			data = response.getEntity().getContent();
			Log.d(getClass().getName(), "=============== after get content");
			statusCode = response.getStatusLine().getStatusCode();
			Log.d(getClass().getName(), "content type name  : "+response.getEntity().getContentType().getName());
			Log.d(getClass().getName(), "content type value : "+response.getEntity().getContentType().getValue());
			if (!JSONConnection.checkIfContentTypeJson(response.getEntity().getContentType().getValue())){
				Log.d(getClass().getName(), "not json type");
				Log.e(getClass().getName(), ConvertInputStreamToString(data));
				notJson = true;
				return null;
			}
			String s = ConvertInputStreamToString(data);
			Log.d(getClass().getName(), "json /n"+s);
			return s;
		}catch (SocketTimeoutException e) {
			errMsg = e.getMessage();
			e.printStackTrace();
			Log.d(getClass().getName(), "err ||||| "+errMsg);
			//			ErrorManager.getInstance().setError(errMsg);
			//			ErrorManager.getInstance().setKindError(ErrorManager.TIMEOUT_EXCEPTION);
		}
		catch (ClientProtocolException e) {
			errMsg = e.getMessage();
			e.printStackTrace();
			Log.d(getClass().getName(), "err ||||| "+errMsg);
			//			ErrorManager.getInstance().setError(errMsg);
			//			ErrorManager.getInstance().setKindError(ErrorManager.UNHANDLED_EXEPTION);
		}
		catch (IOException e) {
			errMsg = e.getMessage();
			e.printStackTrace();
			Log.d(getClass().getName(), "err ||||| "+errMsg);
			//			ErrorManager.getInstance().setError(errMsg);
			//			ErrorManager.getInstance().setKindError(ErrorManager.UNHANDLED_EXEPTION);
		}

		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		//		setJson(result);
		if (notJson && result == null)
			Toast.makeText(context, R.string.feature_not_supported_or_removed_from_server, Toast.LENGTH_LONG).show();
		else if (result != null) {
			try {
				if (JSONConnection.anyServerError(result, context))
					result = null;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		Log.d(getClass().getName(), url);
		Bundle bundle = new Bundle();
		bundle.putString("json", result);
		bundle.putString("url", url);
		bundle.putString("methode", "POST");
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
