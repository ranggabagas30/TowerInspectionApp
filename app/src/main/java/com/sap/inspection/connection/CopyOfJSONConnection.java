package com.sap.inspection.connection;

import java.io.IOException;

import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.model.ErrorSatutempatModel;

public class CopyOfJSONConnection extends AsyncTask<Void, Void, String>{

//	private Activity activity;
	private String url;
	private Handler handler;
	private String json;
	private String errMsg;
	private Context context;
	private URL address;
	private URLConnection urlConnection;
	private InputStream is;
	
	public CopyOfJSONConnection(Context context, String url,Handler handler) {
//		this.activity = activity;
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
			address = new URL(url);
			urlConnection = address.openConnection();
	        urlConnection.setConnectTimeout(3000);
	        is =  urlConnection.getInputStream();
	        Log.d(getClass().getName(), "content type : "+urlConnection.getContentType());
			String s = ConvertInputStreamToString(is);
			is.close();
//			Log.d("XML", s);
//			ErrorManager.getInstance().setError(null);
//			ErrorManager.getInstance().setKindError(0);
			return s;
		}catch (SocketTimeoutException e) {
			errMsg = e.getMessage();
			e.printStackTrace();
			Log.d("here", "err ||||| "+errMsg);
//			ErrorManager.getInstance().setError(errMsg);
//			ErrorManager.getInstance().setKindError(ErrorManager.TIMEOUT_EXCEPTION);
		}
		catch (ClientProtocolException e) {
			errMsg = e.getMessage();
			e.printStackTrace();
			Log.d("here", "err ||||| "+errMsg);
//			ErrorManager.getInstance().setError(errMsg);
//			ErrorManager.getInstance().setKindError(ErrorManager.UNHANDLED_EXEPTION);
		}
		catch (IOException e) {
			errMsg = e.getMessage();
			e.printStackTrace();
			Log.d("here", "err ||||| "+errMsg);
//			ErrorManager.getInstance().setError(errMsg);
//			ErrorManager.getInstance().setKindError(ErrorManager.UNHANDLED_EXEPTION);
		}
		
		return null;
	}
	
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
//		setJson(result);
		Log.d("here", url);
		if (result != null)
			try {
				if (anyServerError(result, context))
					result = null;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		Bundle bundle = new Bundle();
		bundle.putString("json", result);
		Message msg = new Message();
		msg.setData(bundle);
		handler.sendMessage(msg);
//		activity.setProgressBarIndeterminateVisibility(false);
	}
	
	@Override
	protected void onCancelled() {
		super.onCancelled();
//		activity.setProgressBarIndeterminateVisibility(false);
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
	
	public static boolean checkIfContentTypeJson(String contentType){
		int idxSemiColon = contentType.indexOf(Constants.JSON_CONTENT_TYPE);
		Log.e("chek if json on json connection", contentType + " | " + idxSemiColon);
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
