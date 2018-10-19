package com.sap.inspection.manager;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Debug;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.rindang.zconfig.APIList;
import com.sap.inspection.BuildConfig;
import com.sap.inspection.MyApplication;
import com.sap.inspection.R;
import com.sap.inspection.connection.APIHelper;
import com.sap.inspection.connection.JSONConnection;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.constant.GlobalVar;
import com.sap.inspection.event.UploadProgressEvent;
import com.sap.inspection.model.DbRepository;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.ScheduleGeneral;
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
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.params.ConnManagerParams;
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
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import de.greenrobot.event.EventBus;

//import android.util.Log;

public class ItemUploadManager {

    private static ItemUploadManager instance;
    private ArrayList<ItemValueModel> itemValues;
    private ArrayList<ItemValueModel> itemValuesFailed;
    //	private ArrayList<UploadListener> listeners;
    private boolean running = false;
    public String syncDone = "Sinkronisasi selesai";
    public String syncFail = "Sinkronisasi gagal";
    private int retry = 0;
    private String latestStatus;
    private UploadValue uploadTask;

    public boolean isRunning() {
        return running;
    }

    private ItemUploadManager() {
        itemValues = new ArrayList<>();
        itemValuesFailed = new ArrayList<>();
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
        this.itemValuesFailed.clear();
        for (ItemValueModel item : itemvalues) {
            if (!item.disable) {
                this.itemValues.add(item);
            }
        }
        this.retry = 0;
        if (!running) {
            uploadTask = null;
            uploadTask = new UploadValue();
            uploadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            //"There is upload process, please wait until finish"
            MyApplication.getInstance().toast(MyApplication.getContext().getResources().getString(R.string.uploadProses), Toast.LENGTH_SHORT);
        }
    }

    public void addItemValue(ItemValueModel itemvalue) {
        if (itemvalue.disable) return;
        this.itemValues.clear();
        this.itemValuesFailed.clear();
        this.itemValues.add(itemvalue);
        this.retry = 0;
        if (!running) {
            uploadTask = null;
            uploadTask = new UploadValue();
            uploadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            //There is upload process, please wait until finish
            MyApplication.getInstance().toast(MyApplication.getContext().getResources().getString(R.string.uploadProses), Toast.LENGTH_SHORT);
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
            MyApplication.getInstance().toast(MyApplication.getContext().getResources().getString(R.string.progressUpload), Toast.LENGTH_LONG);
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
            String messageToServer = null;
            String messageFromServer = null;
            int itemValueSuccessCount = 0;

            while (itemValues.size() > 0) {

                publish(itemValues.size() + " item yang tersisa");
                latestStatus = itemValues.size() + " item yang tersisa";
                notJson = false;

                /** upload Photo **/
                response = uploadPhoto(itemValues.get(0));
                DebugLog.d("response=" + response);

                if (response != null) {

                    BaseResponseModel responseUploadItemModel = gson.fromJson(response, BaseResponseModel.class);
                    scheduleId = itemValues.get(0).scheduleId;
                    if (responseUploadItemModel.status == 201 || responseUploadItemModel.status == 200) {

                        DebugLog.d("status code : " + responseUploadItemModel.status);
                        ItemValueModel item = itemValues.remove(0);
                        item.uploadStatus = ItemValueModel.UPLOAD_DONE;
                        itemValueSuccessCount++;
                        DebugLog.d("itemValueSuccessCount : " + itemValueSuccessCount);
                        DebugLog.d("upload status : " + item.uploadStatus);
                        //scheduleId = item.scheduleId;

                        if (!DbRepositoryValue.getInstance().getDB().isOpen())
                             DbRepositoryValue.getInstance().open(MyApplication.getContext());
                        item.save();
                    }
                    else {
                        if (responseUploadItemModel.status == 400 ||
                            responseUploadItemModel.status == 422 ||
                            responseUploadItemModel.status == 403 ||
                            responseUploadItemModel.status == 404) {

                            messageFromServer = responseUploadItemModel.messages + " with statuscode : " + responseUploadItemModel.status;
                            DebugLog.d("status code : " + responseUploadItemModel.status);
                        }
                        itemValuesFailed.add(itemValues.remove(0));
                    }
                } else {
                    //stop uploading data when no response == null
                    publish(syncFail);
                    break;
                }
            }

            for (ItemValueModel item : itemValuesFailed) {
                item.uploadStatus = ItemValueModel.UPLOAD_FAIL;
                DebugLog.d("scheduleIdFailed : " + item.scheduleId);
                if (!DbRepositoryValue.getInstance().getDB().isOpen())
                    DbRepositoryValue.getInstance().open(MyApplication.getContext());
                item.save();
            }

            if (response != null) {

                if (itemValuesFailed.size() == 0) {
                    DebugLog.d("syncdone");
                    latestStatus = syncDone;
                    messageToServer = "success";
                } else {
                    DebugLog.d("syncfail");
                    latestStatus = syncFail;
                    messageToServer = "failed";
                }
            } else {
                messageToServer = "failed";
            }


            if (scheduleId != null && isPreventive(scheduleId)) {
                DebugLog.d("hit corrective");
                APIHelper.getJsonFromUrl(MyApplication.getContext(), null, APIList.uploadConfirmUrl() +
                        scheduleId + "/update");
            }

            if (getLatestStatus().equalsIgnoreCase(syncDone)) {
                MyApplication.getInstance().toast(syncDone + "\njumlah item berhasil upload = " + itemValueSuccessCount, Toast.LENGTH_LONG);
                publish(syncDone);
            } else
            if (getLatestStatus().equalsIgnoreCase(syncFail)){
                MyApplication.getInstance().toast(messageFromServer, Toast.LENGTH_SHORT);
                MyApplication.getInstance().toast("Sinkronasi gagal", Toast.LENGTH_LONG);
                publish(syncFail);
            }

            if (BuildConfig.FLAVOR.equalsIgnoreCase("sap")){

                //after uploading data, then send messageToServer to the backend server
                response = uploadStatus(scheduleId, messageToServer);
                if (response != null) {
                    BaseResponseModel responseUploadStatusModel = gson.fromJson(response, BaseResponseModel.class);
                    if (responseUploadStatusModel.status == 201) {
                        DebugLog.d("upload status berhasil");
                    } else {
                        if (responseUploadStatusModel.status == 422 ||
                            responseUploadStatusModel.status == 403 ||
                            responseUploadStatusModel.status == 404 ||
                            responseUploadStatusModel.status == 405) {

                            Crashlytics.log(Log.ERROR, "uploadstatus", "upload status gagal dengan statuscode : " + responseUploadStatusModel.status);
                            DebugLog.d("upload status gagal dengan statuscode : " + responseUploadStatusModel.status);
                        }
                    }
                }
            }

            return null;
        }

        private ArrayList<NameValuePair> getParams(ItemValueModel itemValue) {
            ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
            //params.add(new BasicNameValuePair("schedule_id", "119689099")); // dummy scheduleid to produce status : 404
            params.add(new BasicNameValuePair("schedule_id", itemValue.scheduleId));
            params.add(new BasicNameValuePair("operator_id", String.valueOf(itemValue.operatorId)));
            params.add(new BasicNameValuePair("item_id", String.valueOf(itemValue.itemId)));
            if (itemValue.value != null)
                params.add(new BasicNameValuePair("value", itemValue.value));
            if (itemValue.photoStatus != null)
                params.add(new BasicNameValuePair("photo_status", itemValue.photoStatus));

            if (itemValue.remark != null)
                params.add(new BasicNameValuePair("remark", itemValue.remark));

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
                DebugLog.d("===== START UPLOADING PHOTO === \n");
                DebugLog.d("** set params ** ");
                HttpParams httpParameters = new BasicHttpParams();


                // Set the timeout in milliseconds until a connection is established.
                // The default value is zero, that means the timeout is not used.
                //int timeoutConnection = 3000;
                int timeoutConnection = 1 * 3600 * 1000; // 1 HOUR
                HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
                // Set the default socket timeout (SO_TIMEOUT)
                // in milliseconds which is the timeout for waiting for data.
                int timeoutSocket = 1 * 3600 * 1000; // 1 HOUR
                HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

                HttpClient client = new DefaultHttpClient(httpParameters);
                HttpPost request = new HttpPost(APIList.uploadUrl() + "?access_token=" + getAccessToken(MyApplication.getContext()));
                DebugLog.d(request.getURI().toString());
                SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
                if (mPref.getString(MyApplication.getContext().getString(R.string.user_cookie), null) != null) {
                    request.setHeader("Cookie", mPref.getString(MyApplication.getContext().getString(R.string.user_cookie), ""));
                }

                MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
                ArrayList<NameValuePair> params = getParams(itemValue);
                if (null != itemValue.value && null != itemValue.photoStatus) {
                    if (itemValue.photoDate == null) {
                        params.add(new BasicNameValuePair("photo_datetime", String.valueOf(itemValue.createdAt)));
                        DebugLog.d("photo_datetime : " + itemValue.createdAt);
                    }
                    else {
                        params.add(new BasicNameValuePair("photo_datetime", String.valueOf(itemValue.photoDate)));
                        DebugLog.d("photo_datetime : " + itemValue.photoDate);
                    }
                    reqEntity.addPart("picture", new FileBody(new File(itemValue.value.replaceFirst("^file\\:\\/\\/", ""))));
                }

                for (int i = 0; i < params.size(); i++) {
                    DebugLog.d(params.get(i).getName() + " || " + params.get(i).getValue());
                    reqEntity.addPart(params.get(i).getName(), new StringBody(params.get(i).getValue()));
                }
                System.gc();

                InputStream data;
                HttpResponse response;
                request.setEntity(reqEntity);
                DebugLog.d("\n\n** execute request ** ");
                response = client.execute(request);

                Header cookie = response.getFirstHeader("Set-Cookie");
                if (cookie != null) {
                    mPref.edit().putString(MyApplication.getContext().getString(R.string.user_cookie), cookie.getValue()).commit();
                }

                data = response.getEntity().getContent();
                DebugLog.d("** response ** ");
                DebugLog.d("response string data : " + data);
                statusCode = response.getStatusLine().getStatusCode();
                DebugLog.d("response string status code : " + statusCode);
                String stringResponse = ConvertInputStreamToString(data);
                DebugLog.d("json /n" + stringResponse);
                if (!JSONConnection.checkIfContentTypeJson(response.getEntity().getContentType().getValue())) {
                    DebugLog.d("not json type");
                    notJson = true;
                    if (statusCode == 404) {
                        return stringResponse;
                    } else {
                        return null;
                    }
                }
                DebugLog.d("====== END OF UPLOAD PHOTO ===== \n\n");
                return stringResponse;
            } catch (SocketTimeoutException e) {
                errMsg = e.getMessage();
                e.printStackTrace();
                Crashlytics.log(Log.ERROR, "uploadphoto", "STATUSCODE : " + statusCode + "SE " + "Koneksi dengan server terlalu lama. Periksa jaringan Anda");
                MyApplication.getInstance().toast("upload photo, STATUSCODE : " + statusCode + "SE Koneksi dengan server terlalu lama. Periksa jaringan Anda", Toast.LENGTH_SHORT);
                return null;
            } catch(ClientProtocolException e) {
                errMsg = e.getMessage();
                e.printStackTrace();
                Crashlytics.log(Log.ERROR, "uploadphoto", "STATUSCODE : " + statusCode + "CPE " + e.getMessage());
                MyApplication.getInstance().toast("upload photo, STATUSCODE : " + statusCode + "CPE " + e.getMessage(), Toast.LENGTH_SHORT);
                return null;
            } catch (IOException e) {
                errMsg = e.getMessage();
                e.printStackTrace();
                Crashlytics.log(Log.ERROR, "uploadphoto", "STATUSCODE : " + statusCode + "IOE " + e.getMessage());
                MyApplication.getInstance().toast("upload photo, STATUSCODE : " + statusCode + "IOE " + e.getMessage(), Toast.LENGTH_SHORT);
                return null;
            } catch (NullPointerException e) {
                errMsg = e.getMessage();
                e.printStackTrace();
                Crashlytics.log(Log.ERROR, "uploadphoto", "STATUSCODE : " + statusCode + "NPE " + e.getMessage());
                MyApplication.getInstance().toast("upload photo, STATUSCODE : " + statusCode + "NPE " + e.getMessage(), Toast.LENGTH_SHORT);
                return null;
            }

        }

        private LinkedList<NameValuePair> getParamUploadStatus(String schedule_id, String messageToServer) {
            LinkedList<NameValuePair> params = new LinkedList<>();
            params.add(new BasicNameValuePair("schedule_id", schedule_id));
            params.add(new BasicNameValuePair("message", messageToServer));
            return params;
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
                String stringResponse = ConvertInputStreamToString(data);
                DebugLog.d("json /n" + stringResponse);
                return stringResponse;
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

        private String uploadStatus(String schedule_id, String messageToServer) {
            DebugLog.d("===== START UPLOADING STATUS === \n");
            DebugLog.d("** set params ** ");
            String responseStringData;
            try {
                //Request Part
                HttpParams httpParameters = new BasicHttpParams();


                // Set the timeout in milliseconds until a connection is established.
                // The default value is zero, that means the timeout is not used.
                int timeoutConnection = 1 * 3600 * 1000;
                HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
                // Set the default socket timeout (SO_TIMEOUT)
                // in milliseconds which is the timeout for waiting for data.
                int timeoutSocket = 1 * 36 * 1000;
                HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

                HttpClient client = new DefaultHttpClient(httpParameters);
                HttpPost request = new HttpPost(APIList.uploadStatusUrl() + "?access_token=" + getAccessToken(MyApplication.getContext()));
                DebugLog.d(request.getURI().toString());
                LinkedList<NameValuePair> params = getParamUploadStatus(schedule_id, messageToServer);
                request.setEntity(new UrlEncodedFormEntity(params));


                //Execution Part
                DebugLog.d("\n\n** execute request ** ");
                InputStream data;
                HttpResponse response;
                response = client.execute(request);


                //Response Part
                DebugLog.d("** response ** ");
                data = response.getEntity().getContent();
                int statusCode = response.getStatusLine().getStatusCode();
                responseStringData = ConvertInputStreamToString(data);
                DebugLog.d("schedule_id : " + schedule_id);
                DebugLog.d("messageToServer : " + messageToServer);
                DebugLog.d("response status code : " + statusCode);
                DebugLog.d("response string data : " + responseStringData);

                if (statusCode == 201 || statusCode == 200) {
                    return responseStringData;
                } else
                if (statusCode == 400 ||
                    statusCode == 422 ||
                    statusCode == 403 ||
                    statusCode == 404) {

                    Crashlytics.log(Log.ERROR, "updatestatus", "statuscode : " + statusCode);
                    return responseStringData;
                }

                DebugLog.d("====== END OF UPLOAD STATUS ===== \n\n");
            } catch (SocketTimeoutException e) {
                errMsg = e.getMessage();
                e.printStackTrace();
                Crashlytics.log(Log.ERROR, "updatestatus", e.getMessage());
                DebugLog.d("uploadStatus err " + errMsg);
            } catch (ClientProtocolException e) {
                errMsg = e.getMessage();
                e.printStackTrace();
                Crashlytics.log(Log.ERROR, "updatestatus", e.getMessage());
                DebugLog.d("uploadStatus err ||||| " + errMsg);
            } catch (IOException e) {
                errMsg = e.getMessage();
                e.printStackTrace();
                Crashlytics.log(Log.ERROR, "updatestatus", e.getMessage());
                DebugLog.d("uploadStatus err ||||| " + errMsg);
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

    public boolean isPreventive(String scheduleId) {
        if (!DbRepository.getInstance().getDB().isOpen())
            DbRepository.getInstance().open(MyApplication.getContext());

        ScheduleBaseModel scheduleBaseModel = new ScheduleGeneral();
        scheduleBaseModel = scheduleBaseModel.getScheduleById(scheduleId);

        if (DbRepository.getInstance().getDB().isOpen()) {
            DbRepository.getInstance().close();
        }

        String workTypeName = scheduleBaseModel.work_type.name;
        return workTypeName.matches(Constants.regexPREVENTIVE);
    }
}