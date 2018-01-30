package com.sap.inspection.connection;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

import com.sap.inspection.R;
import com.sap.inspection.tools.DebugLog;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.LinkedList;

public class JSONConnectionDelete extends AsyncTask<Void, Void, String>{

	private String url;
	private Handler handler;
	private String errMsg;
	private boolean notJson = false;
	private int statusCode = 0;
	private LinkedList<NameValuePair> params;
	private HttpDelete request;
	private Context context;

	public JSONConnectionDelete(Context context,String url,Handler handler) {
		this.context = context;
		this.url = url;
		this.handler = handler;
	}

	public JSONConnectionDelete(Context context, String url,Handler handler, LinkedList<NameValuePair> params) {
		this.context = context;
		this.url = url;
		this.handler = handler;
		this.params = params;
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
			
			request = new HttpDelete(url);

			for (int i = 0; i < params.size(); i++)
				httpParameters.setParameter(params.get(i).getName(), params.get(i).getValue());
			
			SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(context);
			if (mPref.getString(context.getString(R.string.user_cookie), null) != null){
				request.setHeader("Cookie", mPref.getString(context.getString(R.string.user_cookie), ""));
			}
			
			request.setParams(httpParameters);

			HttpResponse response;
			InputStream data = null;
			response = client.execute(request);
			
			//pull cookie
			Header cookie = response.getFirstHeader("Set-Cookie");
			if (cookie != null){
				mPref.edit().putString(context.getString(R.string.user_cookie), cookie.getValue()).commit();
			}
			
//			data = response.getEntity().getContent();
			statusCode = response.getStatusLine().getStatusCode();
//			DebugLog.d("content type name  : "+response.getEntity().getContentType().getName());
//			DebugLog.d("content type value : "+response.getEntity().getContentType().getValue());
//			if (!JSONConnection.checkIfContentTypeJson(response.getEntity().getContentType().getValue())){
//				DebugLog.d("not json type");
//				Log.e(getClass().getName(), ConvertInputStreamToString(data));
//				notJson = true;
//				return null;
//			}
//			String s = ConvertInputStreamToString(data);
//			DebugLog.d("json /n"+s);
			
			return null;
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
		DebugLog.d(url);
		Bundle bundle = new Bundle();
		bundle.putString("json", result);
		bundle.putString("url", url);
		bundle.putString("methode", "DELETE");
		bundle.putInt("statusCode", statusCode);
		Message msg = new Message();
		msg.setData(bundle);
		handler.sendMessage(msg);
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
	
}
