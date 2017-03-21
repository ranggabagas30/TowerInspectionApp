package com.sap.inspection.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rindang.zconfig.APIList;
import com.sap.inspection.MyApplication;
import com.sap.inspection.R;
import com.sap.inspection.connection.APIHelper;
import com.sap.inspection.connection.JSONConnection;
import com.sap.inspection.event.UploadProgressEvent;
import com.sap.inspection.model.responsemodel.BaseResponseModel;
import com.sap.inspection.model.value.DbRepositoryValue;
import com.sap.inspection.model.value.ItemValueModel;
import com.sap.inspection.tools.DebugLog;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;

import de.greenrobot.event.EventBus;

//import android.util.Log;

public class ItemUploadManager {

    private static ItemUploadManager instance;
    private ArrayList<ItemValueModel> itemValues;
    private ArrayList<ItemValueModel> itemValuesFailed;
    //	private ArrayList<UploadListener> listeners;
    private boolean running = false;
    public String syncDone = "Sinkronasi selesai";
    public String syncFail = "Sinkronasi gagal";
    private int retry = 0;
    private String latestStatus;
    private UploadValue uploadTask;

    public boolean isRunning() {
        return running;
    }

    private ItemUploadManager() {
        itemValues = new ArrayList<ItemValueModel>();
    }

    public static ItemUploadManager getInstance() {
        if (instance == null) {
            instance = new ItemUploadManager();
        }
        return instance;
    }

    public String getLatestStatus() {
        return latestStatus;
    }

    public void addItemValues(Collection<ItemValueModel> itemvalues) {
        DebugLog.d("itemvalues="+itemvalues.size());
        this.itemValues.clear();
        for (ItemValueModel item : itemvalues) {
            if (!item.disable) {
                this.itemValues.add(item);
            }
        }
//		this.itemValues.addAll(itemvalues);
        this.retry = 0;
        if (!running) {
            uploadTask = null;
            uploadTask = new UploadValue();
            uploadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            //"There is upload process, please wait until finish"
            MyApplication.getInstance().toast("Proses upload sedang berjalan, silahkan tunggu sampai proses selesai", Toast.LENGTH_SHORT);
        }
    }

    public void addItemValue(ItemValueModel itemvalue) {
        if (itemvalue.disable) return;
        this.itemValues.clear();
        this.itemValues.add(itemvalue);
        this.retry = 0;
        if (!running) {
            uploadTask = null;
            uploadTask = new UploadValue();
            uploadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            //There is upload process, please wait until finish
            MyApplication.getInstance().toast("Proses upload sedang berjalan, silahkan tunggu sampai proses selesai", Toast.LENGTH_SHORT);
        }
    }

    private class UploadValue extends AsyncTask<Void, String, Void> {

        // private Activity activity;
        private String json;
        private String errMsg;
        private int statusCode = 0;
        private boolean notJson = false;
        private Gson gson = new Gson();
        private String scheduleId;

        public UploadValue() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            running = true;
            // activity.setProgressBarIndeterminateVisibility(true);
        }

        private void publish(String msg) {
            publishProgress(msg);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            UploadProgressEvent event = new UploadProgressEvent(values[0]);
            event.done = values[0].equalsIgnoreCase(syncDone) || values[0].equalsIgnoreCase(syncFail);
            EventBus.getDefault().post(event);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String response = null;
            itemValuesFailed = new ArrayList<>();
            while (itemValues.size() > 0) {
                publish(itemValues.size() + " item yang tersisa");
                latestStatus = itemValues.size() + " item yang tersisa";
                //				Log.d(getClass().getName(),"=========================================================");
                //				Log.d(getClass().getName(),"=========================================================");
                //				Log.d(getClass().getName(),"=========================================================");
                //				Log.d(getClass().getName(),"ids : "+itemValues.get(0).scheduleId+" | "+itemValues.get(0).operatorId+" | "+itemValues.get(0).itemId+" | "+itemValues.get(0).value);
                notJson = false;

                //check if upload with photo or default item
                //				if (itemValues.get(0).typePhoto)
                response = uploadPhoto(itemValues.get(0));
                DebugLog.d("response=" + response);
                //				else
                //					response = uploadItem(itemValues.get(0));

                //success then save the flag to done
                /*
				boolean isSuccess = isSuccess(response);
				DebugLog.d("isSuccess="+isSuccess);*/
                if (response != null) {
                    BaseResponseModel responseModel = gson.fromJson(response, BaseResponseModel.class);
                    if (responseModel.status == 201) {
                        ItemValueModel item = itemValues.remove(0);
                        item.uploadStatus = ItemValueModel.UPLOAD_DONE;
                        scheduleId = item.scheduleId;
                        if (!DbRepositoryValue.getInstance().getDB().isOpen())
                            DbRepositoryValue.getInstance().open(MyApplication.getContext());
                        item.save();
                    }

                    //retry until 5 times
                    else {
                        itemValuesFailed.add(itemValues.remove(0));
                        if (responseModel.status == 422 || responseModel.status == 403 || responseModel.status == 404) {
                            MyApplication.getInstance().toast(responseModel.messages, Toast.LENGTH_LONG);
                        }
                    }
                }
                /*
                retry++;
                if (retry == 5) {
                    break;
                }*/
            }

            for (ItemValueModel item : itemValuesFailed) {
                item.uploadStatus = ItemValueModel.UPLOAD_NONE;
                if (!DbRepositoryValue.getInstance().getDB().isOpen())
                    DbRepositoryValue.getInstance().open(MyApplication.getContext());
                item.save();
            }

            if (itemValuesFailed.size() == 0) {
                MyApplication.getInstance().toast(syncDone, Toast.LENGTH_LONG);
                publish(syncDone);
                latestStatus = syncDone;
            } else {
                //Syncronize failed \nsum item not uploaded =
                        MyApplication.getInstance().toast("Sinkronasi gagal\njumlah item gagal upload = " + itemValuesFailed.size()
                        + " items", Toast.LENGTH_LONG);
                publish(syncFail);
                latestStatus = syncFail;
            }
            if (scheduleId != null)
                DebugLog.d("hit corrective");
                APIHelper.getJsonFromUrl(MyApplication.getContext(), null, APIList.uploadConfirmUrl() +
                        scheduleId + "/update");

            return null;
        }

        private boolean isSuccess(String response) {
            if (response == null) {
                return false;
            } else
                try {
                    Gson gson = new Gson();
                    BaseResponseModel responseModel = gson.fromJson(json, BaseResponseModel.class);
                    if (responseModel.status != 201) {
                        DebugLog.d("set false");
                        return false;
                    } else {
                        DebugLog.d("set true");
                        return true;
                    }
                } catch (Exception e) {
                    return false;
                }
            //return true;
        }

        private ArrayList<NameValuePair> getParams(ItemValueModel itemValue) {
            ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("schedule_id", itemValue.scheduleId));
            params.add(new BasicNameValuePair("operator_id", String.valueOf(itemValue.operatorId)));
            params.add(new BasicNameValuePair("item_id", String.valueOf(itemValue.itemId)));
            if (itemValue.value != null)
                params.add(new BasicNameValuePair("value", itemValue.value));
            if (itemValue.photoStatus != null)
                params.add(new BasicNameValuePair("photo_status", itemValue.photoStatus));

            if (itemValue.remark != null)
                params.add(new BasicNameValuePair("remark", itemValue.remark));

//            if (itemValue.material_request != null)
//                params.add(new BasicNameValuePair("material_request", itemValue.material_request));

            if (itemValue.latitude != null && !itemValue.latitude.equalsIgnoreCase("0"))
                params.add(new BasicNameValuePair("latitude", itemValue.latitude));
            if (itemValue.longitude != null && !itemValue.longitude.equalsIgnoreCase("0"))
                params.add(new BasicNameValuePair("longitude", itemValue.longitude));
            if (itemValue.gpsAccuracy != 0)
                params.add(new BasicNameValuePair("accuracy", String.valueOf(itemValue.gpsAccuracy)));
            return params;
        }

        private String uploadPhoto(ItemValueModel itemValue) {
            try {
                HttpClient client = new DefaultHttpClient();
                HttpPost request = new HttpPost(APIList.uploadUrl() + "?access_token=" + getAccessToken(MyApplication.getContext()));
                DebugLog.d(request.getURI().toString());
                SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
                if (mPref.getString(MyApplication.getContext().getString(R.string.user_cookie), null) != null) {
                    request.setHeader("Cookie", mPref.getString(MyApplication.getContext().getString(R.string.user_cookie), ""));
                }

                MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

                ByteArrayOutputStream bos;
                Bitmap bm;
                byte[] dataFile;
                ByteArrayBody bab;
                ArrayList<NameValuePair> params = getParams(itemValue);
                if (null != itemValue.value && null != itemValue.photoStatus) {
                    params.add(new BasicNameValuePair("photo_datetime", String.valueOf(itemValue.createdAt)));
                    DebugLog.d("createdAt" + itemValue.createdAt);
                    reqEntity.addPart("picture", new FileBody(new File(itemValue.value.replaceFirst("^file\\:\\/\\/", ""))));
                }

                for (int i = 0; i < params.size(); i++) {
                    DebugLog.d(params.get(i).getName() + " || " + params.get(i).getValue());
                    reqEntity.addPart(params.get(i).getName(), new StringBody(params.get(i).getValue()));
                }


                bm = null;
                bos = null;
                dataFile = null;
                bab = null;
                System.gc();

                InputStream data = null;
                HttpResponse response;
                request.setEntity(reqEntity);
                response = client.execute(request);

                Header cookie = response.getFirstHeader("Set-Cookie");
                if (cookie != null) {
                    mPref.edit().putString(MyApplication.getContext().getString(R.string.user_cookie), cookie.getValue()).commit();
                }

                data = response.getEntity().getContent();
                statusCode = response.getStatusLine().getStatusCode();
                String s = ConvertInputStreamToString(data);
                DebugLog.d("json /n" + s);
                if (!JSONConnection.checkIfContentTypeJson(response.getEntity().getContentType().getValue())) {
                    DebugLog.d("not json type");
                    notJson = true;
                    return null;
                }
                DebugLog.d("########################################################");
                return s;
            } catch (SocketTimeoutException e) {
                errMsg = e.getMessage();
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                errMsg = e.getMessage();
                e.printStackTrace();
            } catch (IOException e) {
                errMsg = e.getMessage();
                e.printStackTrace();
            } catch (NullPointerException e) {
                errMsg = e.getMessage();
                e.printStackTrace();
            }
            return null;
        }

        private String uploadItem(ItemValueModel itemValue) {
            try {
                DebugLog.d("=============== post do BG");
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
                HttpPost request = new HttpPost(APIList.uploadUrl());
                request.setHeader("Content-Type", "application/x-www-form-urlencoded");
                SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
                if (mPref.getString(MyApplication.getContext().getString(R.string.user_cookie), null) != null) {
                    request.setHeader("Cookie", mPref.getString(MyApplication.getContext().getString(R.string.user_cookie), ""));
                }

                UrlEncodedFormEntity entity = null;
                entity = new UrlEncodedFormEntity(getParams(itemValue), HTTP.UTF_8);
                request.setEntity(entity);

                DebugLog.d("=============== before response");
                HttpResponse response = client.execute(request);

                for (Header header : response.getAllHeaders()) {
                    DebugLog.d(header.getName() + " ||| " + header.getValue());
                }

                // pull cookie
                Header cookie = response.getFirstHeader("Set-Cookie");
                if (cookie != null) {
                    mPref.edit().putString(MyApplication.getContext().getString(R.string.user_cookie), cookie.getValue()).commit();
                }

                DebugLog.d("=============== after response");
                InputStream data = response.getEntity().getContent();
                DebugLog.d("=============== after get content");
                statusCode = response.getStatusLine().getStatusCode();
                DebugLog.d("content type name  : " + response.getEntity().getContentType().getName());
                DebugLog.d("content type value : " + response.getEntity().getContentType().getValue());
                if (!JSONConnection.checkIfContentTypeJson(response.getEntity().getContentType().getValue())) {
                    DebugLog.d("not json type");
                    DebugLog.d(ConvertInputStreamToString(data));
                    notJson = true;
                    return null;
                }
                String s = ConvertInputStreamToString(data);
                DebugLog.d("json /n" + s);
                return s;
            } catch (SocketTimeoutException e) {
                errMsg = e.getMessage();
                e.printStackTrace();
                DebugLog.d("err ||||| " + errMsg);
            } catch (ClientProtocolException e) {
                errMsg = e.getMessage();
                e.printStackTrace();
                DebugLog.d("err ||||| " + errMsg);
            } catch (IOException e) {
                errMsg = e.getMessage();
                e.printStackTrace();
                DebugLog.d("err ||||| " + errMsg);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            running = false;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            running = false;
            // activity.setProgressBarIndeterminateVisibility(false);
        }

        public String ConvertInputStreamToString(InputStream is) {
            String str = null;
            byte[] b = null;
            try {
                StringBuffer buffer = new StringBuffer();
                b = new byte[4096];
                for (int n; (n = is.read(b)) != -1; ) {
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

    public static String getAccessToken(Context context) {
        SharedPreferences mpref = PreferenceManager.getDefaultSharedPreferences(context);
        return mpref.getString(context.getString(R.string.user_authToken), "");
    }

    private void stop() {

    }

}