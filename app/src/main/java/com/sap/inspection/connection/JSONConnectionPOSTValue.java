package com.sap.inspection.connection;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;
import com.sap.inspection.model.ErrorSatutempatModel;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.StringUtil;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.LinkedList;

public class JSONConnectionPOSTValue extends AsyncTask<Void, Void, String>{

	//	private Activity activity;
	private String url;
	private Handler handler;
	private String json;
	private String errMsg;
	private String filePath;
	private String fileName;
	private LinkedList<NameValuePair> params;
	private int statusCode = 0;
	private boolean notJson = false;
	public boolean working = false;

	public JSONConnectionPOSTValue(String url,Handler handler,LinkedList<NameValuePair> params, String filePath, String fileName) {
		//		this.activity = activity;
		this.params = params;
		this.url = url;
		this.handler = handler;
		this.filePath = filePath;
		this.fileName = fileName;
	}

	public JSONConnectionPOSTValue(String url,Handler handler,NameValuePair param, String filePath, String fileName) {
		this.params = new LinkedList<NameValuePair>();
		this.params.add(param);
		this.url = url;
		this.handler = handler;
		this.filePath = filePath;
		this.fileName = fileName;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		working = true;
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
			HttpPost request = new HttpPost(url);
//			request.setHeader("Content-Type", "application/x-www-form-urlencoded");
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			Bitmap bm = BitmapFactory.decodeFile(filePath);
			bm.compress(CompressFormat.JPEG, 75, bos);
			byte[] dataFile = bos.toByteArray();
			ByteArrayBody bab = new ByteArrayBody(dataFile, fileName);

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			reqEntity.addPart(fileName, bab);
			for (int i = 0 ; i< params.size(); i++){
				reqEntity.addPart(params.get(i).getName(), new StringBody(params.get(i).getValue()));
			}
//			reqEntity.addPart("photoCaption", new StringBody("sfsdfsdf"));
			
			InputStream data = null;
			HttpResponse response;
			request.setEntity(reqEntity);
			response = client.execute(request);
			
			data = response.getEntity().getContent();
			statusCode = response.getStatusLine().getStatusCode();
//			Log.d(getClass().getName(), "content type name  : "+response.getEntity().getContentType().getName());
//			Log.d(getClass().getName(), "content type value : "+response.getEntity().getContentType().getValue());
			if (!StringUtil.checkIfContentTypeJson(response.getEntity().getContentType().getValue())){
//				Log.d("here", "not json type");
//				Log.e("json connection post", ConvertInputStreamToString(data));
				notJson = true;
				return null;
			}
			String s = ConvertInputStreamToString(data);
//			Log.d("here", "json /n"+s);
			return s;
		}catch (SocketTimeoutException e) {
			errMsg = e.getMessage();
			e.printStackTrace();
//			Log.d("here", "err ||||| "+errMsg);
			//			ErrorManager.getInstance().setError(errMsg);
			//			ErrorManager.getInstance().setKindError(ErrorManager.TIMEOUT_EXCEPTION);
		}
		catch (ClientProtocolException e) {
			errMsg = e.getMessage();
			e.printStackTrace();
//			Log.d("here", "err ||||| "+errMsg);
			//			ErrorManager.getInstance().setError(errMsg);
			//			ErrorManager.getInstance().setKindError(ErrorManager.UNHANDLED_EXEPTION);
		}
		catch (IOException e) {
			errMsg = e.getMessage();
			e.printStackTrace();
//			Log.d("here", "err ||||| "+errMsg);
			//			ErrorManager.getInstance().setError(errMsg);
			//			ErrorManager.getInstance().setKindError(ErrorManager.UNHANDLED_EXEPTION);
		} 

		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		//		setJson(result);
		if (notJson && result == null){
//			Toast.makeText(context, R.string.failed_feature_not_supported_or_removed_from_server, Toast.LENGTH_LONG).show();
		}
		else if (result != null) {
			try {
				if (anyServerError(result))
					result = null;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		DebugLog.d( url);
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
	
	public static boolean anyServerError(String json) throws JSONException{
    	JSONObject jsonObj = new JSONObject(json);
    	JSONObject dataObj = jsonObj.optJSONObject("data");
    	if (dataObj == null)
    		return false;
		String data = jsonObj.getString("data");
		Gson gson = new Gson();
		ErrorSatutempatModel model = gson.fromJson(data,ErrorSatutempatModel.class);
        return model != null && model.error_type != null;
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
}
