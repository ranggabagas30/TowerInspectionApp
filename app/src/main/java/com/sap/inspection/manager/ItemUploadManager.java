package com.sap.inspection.manager;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Debug;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.rindang.zconfig.APIList;
import com.sap.inspection.BaseActivity;
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
import com.sap.inspection.model.config.formimbaspetir.FormImbasPetirConfig;
import com.sap.inspection.model.config.formimbaspetir.Warga;
import com.sap.inspection.model.form.WorkFormItemModel;
import com.sap.inspection.model.responsemodel.BaseResponseModel;
import com.sap.inspection.model.responsemodel.ItemDefaultResponseModel;
import com.sap.inspection.model.responsemodel.ItemWargaResponseModel;
import com.sap.inspection.model.value.DbRepositoryValue;
import com.sap.inspection.model.value.ItemValueModel;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.StringUtil;

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

import static com.sap.inspection.model.value.ItemValueModel.isPictureRadioItemValidated;

//import android.util.Log;

public class ItemUploadManager {

    private static ItemUploadManager instance;
    private ArrayList<ItemValueModel> itemValues;
    private ArrayList<ItemValueModel> itemValuesFailed;
    private ArrayList<ItemValueModel> itemValuesModified;

    private boolean running = false;
    public String syncDone = "Sinkronisasi selesai";
    public String syncFail = "Sinkronisasi gagal";
    private String latestStatus;
    private UploadValue uploadTask;

    public boolean isRunning() {
        return running;
    }

    private ItemUploadManager() {
        itemValues = new ArrayList<>();
        itemValuesFailed = new ArrayList<>();
        itemValuesModified = new ArrayList<>();
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
        if (itemvalues == null)
            MyApplication.getInstance().toast("Gagal upload item. Pastikan item form mandatory telah terisi semua", Toast.LENGTH_LONG);
        else {
                if (itemvalues.isEmpty()) {
                    MyApplication.getInstance().toast(MyApplication.getContext().getString(R.string.tidakadaitem), Toast.LENGTH_SHORT);
                    return;
                }

                DebugLog.d("itemvalues="+itemvalues.size());
                this.itemValues.clear();
                this.itemValuesFailed.clear();
                this.itemValuesModified.clear();
                for (ItemValueModel item : itemvalues) {
                    if (item != null && !item.disable) {
                        this.itemValues.add(item);
                    }
                }
                if (!running) {
                    uploadTask = null;
                    uploadTask = new UploadValue();
                    uploadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
        }
    }

    public void addItemValue(WorkFormItemModel workFormItem, ItemValueModel filledItem) {

        // if the filled items are mandatory, then apply strict rules
        if (workFormItem.mandatory) {

            // for non-TYPE_PICTURE_RADIO
            if (TextUtils.isEmpty(filledItem.value)) {

                if (workFormItem.field_type.equalsIgnoreCase("file") && !TextUtils.isEmpty(filledItem.photoStatus) && !filledItem.photoStatus.equalsIgnoreCase(Constants.NA))
                    MyApplication.getInstance().toast("Photo item" + workFormItem.label + " harus ada", Toast.LENGTH_LONG);
                else if (workFormItem.field_type.equalsIgnoreCase("file") && !isPictureRadioItemValidated(workFormItem, filledItem))
                    DebugLog.d("item file picture radio not validated");

                return;

            } else if (workFormItem.field_type.equalsIgnoreCase("file") && !isPictureRadioItemValidated(workFormItem, filledItem)) {
                return;
            }

        } else {

            if (workFormItem.field_type.equalsIgnoreCase("file") && !isPictureRadioItemValidated(workFormItem, filledItem))
                return;
        }

        if (filledItem.disable) return;

        this.itemValues.clear();
        this.itemValuesFailed.clear();
        this.itemValuesModified.clear();
        this.itemValues.add(filledItem);
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

        // upload variables
        private final String MESSAGE_SUCCESS = "success";
        private final String MESSAGE_FAILED  = "failed";
        private String response = null;
        private String messageToServer;
        private String simpleResponseMessage = null;
        private int itemValueSuccessCount = 0;

        private Gson gson = new Gson();
        private String scheduleId;

        private Handler responseObserver;

        public UploadValue() {}

        public UploadValue(Handler responseObserver) {
            this.responseObserver = responseObserver;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            running = true;
            MyApplication.getInstance().toast(MyApplication.getContext().getResources().getString(R.string.progressUpload), Toast.LENGTH_SHORT);
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

            while (itemValues.size() > 0) {

                publish(itemValues.size() + " item yang tersisa");
                latestStatus = itemValues.size() + " item yang tersisa";

                /*if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {
                    ItemValueModel itemChanged = checkWargaAndBarangID(itemValues.get(0));
                    itemValues.set(0, itemChanged);
                }*/

                /** upload Photo **/
                response = uploadItem2(itemValues.get(0));
                DebugLog.d("response=" + response);

                if (response != null) {

                    scheduleId = itemValues.get(0).scheduleId;
                    processUploadResponse();

                } else {

                    // stop upload
                    break;
                }
            }

            if (scheduleId != null && StringUtil.isPreventive(scheduleId)) {
                DebugLog.d("hit corrective");
                APIHelper.getJsonFromUrl(MyApplication.getContext(), null, APIList.uploadConfirmUrl() + scheduleId + "/update");
            }

            // save failed item's status
            for (ItemValueModel item : itemValuesFailed) {
                item.uploadStatus = ItemValueModel.UPLOAD_FAIL;
                item.save();
            }

            if (response != null) {

                if (itemValuesFailed.size() == 0) {
                    simpleResponseMessage = "jumlah item berhasil upload = " + itemValueSuccessCount;
                    latestStatus = syncDone;
                    messageToServer = MESSAGE_SUCCESS;
                } else {
                    latestStatus = syncFail;
                    messageToServer = MESSAGE_FAILED;
                }

            } else {
                latestStatus = syncFail;
                simpleResponseMessage = "response is null";
                messageToServer = MESSAGE_FAILED;
            }

            DebugLog.d("latest status : " + latestStatus);

            publish(latestStatus);
            MyApplication.getInstance().toast(latestStatus + "\n" + simpleResponseMessage, Toast.LENGTH_LONG);


            // SAP only
            if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)){

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

        private ItemValueModel checkWargaAndBarangID(ItemValueModel item) {

            item.wargaId    = StringUtil.getRegisteredWargaId(item.scheduleId, item.wargaId);
            item.barangId   = StringUtil.getRegisteredBarangId(item.scheduleId, item.wargaId, item.barangId);
            return item;
        }

        private void processUploadResponse() {

            ItemDefaultResponseModel responseUploadItemModel = gson.fromJson(response, ItemDefaultResponseModel.class);

            DebugLog.d("status code : " + responseUploadItemModel.status);
            if (responseUploadItemModel.status == 201 || responseUploadItemModel.status == 200) {

                ItemValueModel item = itemValues.remove(0);
                item.uploadStatus = ItemValueModel.UPLOAD_DONE;

                if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {

                    itemValuesModified.add(item);

                    // form imbas petir check new wargaid and new barangid after registration
                    String newWargaId;
                    String oldWargaId = item.wargaId;

                    String newBarangId;
                    String oldBarangId = item.barangId;

                    if (StringUtil.isNotNullAndNotEmpty(oldWargaId) && StringUtil.isNotRegistered(oldWargaId)) {

                        if (responseUploadItemModel.data.getWarga_id() != 0) {

                            DebugLog.d("upload informasi diri complete, update wargaid");
                            newWargaId = String.valueOf(responseUploadItemModel.data.getWarga_id());

                            // update all items with old 'wargaId' value on FormValue table to the new one
                            //ItemValueModel.updateWargaId(item.scheduleId, oldWargaId, newWargaId);
                            ItemValueModel.updateWargaItems(oldWargaId, newWargaId, itemValuesModified);

                            // update formimbaspetirconfig
                            updateWargaId(oldWargaId, newWargaId);

                            return;
                        }
                    }

                    if (StringUtil.isNotNullAndNotEmpty(oldBarangId) && StringUtil.isNotRegistered(oldBarangId)) {

                        if (responseUploadItemModel.data.getBarang_id() != 0) {

                            newBarangId = String.valueOf(responseUploadItemModel.data.getBarang_id());

                            // update all items with old 'barangId' value on FormValue table to the new one
                            ItemValueModel.updateBarangItems(oldWargaId, oldBarangId, newBarangId, itemValuesModified);

                            // update formimbaspetirconfig
                            updateBarangId(oldWargaId, oldBarangId, newBarangId);

                            return;
                        }
                    }

                }

                itemValueSuccessCount++;

                DebugLog.d("itemValueSuccessCount : " + itemValueSuccessCount);
                DebugLog.d("upload status : " + item.uploadStatus);

                item.save();

            } else {
                if (responseUploadItemModel.status == 400 ||
                    responseUploadItemModel.status == 422 ||
                    responseUploadItemModel.status == 403 ||
                    responseUploadItemModel.status == 404) {

                    simpleResponseMessage = " with statuscode : " + responseUploadItemModel.status + "\n" + responseUploadItemModel.messages;
                }
                itemValuesFailed.add(itemValues.remove(0));
            }
        }

        private void checkWargaIdAndBarangId(ItemValueModel item, ItemDefaultResponseModel responseUploadItemModel) {

            boolean isNotRegistered = false;
            String oldWargaId = item.wargaId;
            String oldBarangId = item.barangId;

            DebugLog.d("(oldWargaId, oldBarangId) : (" + oldWargaId + ", " + oldBarangId +")");

            if (StringUtil.isNotNullAndNotEmpty(oldWargaId)) {

                if (StringUtil.isNotRegistered(oldWargaId)) {

                    //String scheduleId = item.scheduleId;
                    String newWargaId = String.valueOf(responseUploadItemModel.data.getWarga_id());

                    // update wargaid in the config
                    updateWargaId(oldWargaId, newWargaId);

                    item.wargaId = newWargaId;

                    isNotRegistered = true;
                }
            }

            if (StringUtil.isNotNullAndNotEmpty(oldWargaId) && StringUtil.isNotNullAndNotEmpty(oldBarangId)) {

                if (StringUtil.isNotRegistered(oldBarangId)) {

                    String newBarangId = String.valueOf(responseUploadItemModel.data.getBarang_id());

                    // update barangid in the config
                    updateBarangId(oldWargaId, oldBarangId, newBarangId);

                    // update barangid in the table
                    item.barangId = newBarangId;

                    isNotRegistered = true;
                }

            }

            if (isNotRegistered) {

                // delete old items which using old wargaid or old barangid
                //DebugLog.d("-> Delete old itemid : (" + item.scheduleId + ", " + item.itemId + ", " + item.wargaId + ", " + item.barangId + ")");
                ItemValueModel.delete(item.scheduleId, item.itemId, item.operatorId, oldWargaId, oldBarangId);
            }
        }


        private void updateWargaId(String oldWargaId, String newWargaId) {

            DebugLog.d("update warga id : (old,new) = (" + oldWargaId + "," + newWargaId + ")");
            FormImbasPetirConfig.updateWargaId(scheduleId, oldWargaId, newWargaId);

        }

        private void updateBarangId(String wargaId, String oldBarangId, String newBarangId) {

            DebugLog.d("update barang id : (old,new) = (" + oldBarangId + "," + newBarangId + ")");
            FormImbasPetirConfig.updateBarangId(scheduleId, wargaId, oldBarangId, newBarangId);
        }

        private ArrayList<NameValuePair> getParams(ItemValueModel itemValue) {

            ArrayList<NameValuePair> params = new ArrayList<>();

            params.add(new BasicNameValuePair("schedule_id", itemValue.scheduleId));
            params.add(new BasicNameValuePair("operator_id", String.valueOf(itemValue.operatorId)));
            params.add(new BasicNameValuePair("item_id", String.valueOf(itemValue.itemId)));

            if (itemValue.value != null)
                params.add(new BasicNameValuePair("value", itemValue.value));
            if (itemValue.photoStatus != null) {

                params.add(new BasicNameValuePair("photo_status", itemValue.photoStatus));

                if (null != itemValue.value) {
                    if (itemValue.photoDate == null) {
                        params.add(new BasicNameValuePair("photo_datetime", String.valueOf(itemValue.createdAt)));
                        DebugLog.d("photo_datetime : " + itemValue.createdAt);
                    }
                    else {
                        params.add(new BasicNameValuePair("photo_datetime", String.valueOf(itemValue.photoDate)));
                        DebugLog.d("photo_datetime : " + itemValue.photoDate);
                    }
                }
            }
            if (itemValue.remark != null)
                params.add(new BasicNameValuePair("remark", itemValue.remark));
            if (itemValue.latitude != null && !itemValue.latitude.equalsIgnoreCase("0"))
                params.add(new BasicNameValuePair("latitude", itemValue.latitude));
            if (itemValue.longitude != null && !itemValue.longitude.equalsIgnoreCase("0"))
                params.add(new BasicNameValuePair("longitude", itemValue.longitude));
            if (itemValue.gpsAccuracy != 0)
                params.add(new BasicNameValuePair("accuracy", String.valueOf(itemValue.gpsAccuracy)));

            if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {

                String wargaId = null;
                String barangId = null;

                if (StringUtil.isNotNullAndNotEmpty(itemValue.wargaId)) {
                    wargaId = StringUtil.getRegisteredWargaId(itemValue.scheduleId, itemValue.wargaId);
                    params.add(new BasicNameValuePair("wargaid", wargaId));
                }
                if (StringUtil.isNotNullAndNotEmpty(wargaId) && StringUtil.isNotNullAndNotEmpty(itemValue.barangId)) {
                    barangId = StringUtil.getRegisteredBarangId(itemValue.scheduleId, wargaId, itemValue.barangId);
                    params.add(new BasicNameValuePair("barangid", barangId));
                }
            }

            return params;
        }

        private String uploadItem2(ItemValueModel itemValue) {
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
                HttpPost request = new HttpPost(APIList.uploadUrl() + "?access_token=" + APIHelper.getAccessToken(MyApplication.getInstance()));
                DebugLog.d(request.getURI().toString());

                SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
                if (mPref.getString(MyApplication.getContext().getString(R.string.user_cookie), null) != null) {
                    request.setHeader("Cookie", mPref.getString(MyApplication.getContext().getString(R.string.user_cookie), ""));
                }

                MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

                ArrayList<NameValuePair> params = getParams(itemValue);

                if (itemValue.photoStatus != null && itemValue.value != null)
                    reqEntity.addPart("picture", new FileBody(new File(itemValue.value.replaceFirst("^file\\:\\/\\/", ""))));

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
                DebugLog.d("json : " + stringResponse);

                if (!StringUtil.checkIfContentTypeJson(response.getEntity().getContentType().getValue())) {
                //if (true) { // debug purpose only
                    DebugLog.d("not json type");
                    if (statusCode == 422 ||
                        statusCode == 403 ||
                        statusCode == 404 ||
                        statusCode == 405) {

                        Crashlytics.log(Log.ERROR, "uploadItem2", "Not json type with code : " + statusCode + " and string response : " + stringResponse);
                        MyApplication.getInstance().toast("Not json type with code : " + statusCode  + " and string response : " + stringResponse, Toast.LENGTH_LONG);
                        return stringResponse;
                    } else {
                        Crashlytics.log(Log.ERROR, "uploadItem2", "Not json type with string response : " + stringResponse);
                        MyApplication.getInstance().toast("Not json type with string response : " + stringResponse, Toast.LENGTH_LONG);
                        return null;
                    }
                }
                DebugLog.d("====== END OF UPLOAD PHOTO ===== \n\n");
                return stringResponse;
            } catch (SocketTimeoutException e) {
                errMsg = e.getMessage();
                e.printStackTrace();
                Crashlytics.log(Log.ERROR, "uploadItem2", "STATUSCODE : " + statusCode + "SE " + "Koneksi dengan server terlalu lama. Periksa jaringan Anda");
                MyApplication.getInstance().toast("upload photo, STATUSCODE : " + statusCode + "SE Koneksi dengan server terlalu lama. Periksa jaringan Anda", Toast.LENGTH_LONG);
                return null;
            } catch(ClientProtocolException e) {
                errMsg = e.getMessage();
                e.printStackTrace();
                Crashlytics.log(Log.ERROR, "uploadItem2", "STATUSCODE : " + statusCode + "CPE " + e.getMessage());
                MyApplication.getInstance().toast("upload photo, STATUSCODE : " + statusCode + "CPE " + e.getMessage(), Toast.LENGTH_LONG);
                return null;
            } catch (IOException e) {
                errMsg = e.getMessage();
                e.printStackTrace();
                Crashlytics.log(Log.ERROR, "uploadItem2", "STATUSCODE : " + statusCode + "IOE " + e.getMessage());
                MyApplication.getInstance().toast("upload photo, STATUSCODE : " + statusCode + "IOE " + e.getMessage(), Toast.LENGTH_LONG);
                return null;
            } catch (NullPointerException e) {
                errMsg = e.getMessage();
                e.printStackTrace();
                Crashlytics.log(Log.ERROR, "uploadItem2", "STATUSCODE : " + statusCode + "NPE " + e.getMessage());
                MyApplication.getInstance().toast("upload photo, STATUSCODE : " + statusCode + "NPE " + e.getMessage(), Toast.LENGTH_LONG);
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
                if (!StringUtil.checkIfContentTypeJson(response.getEntity().getContentType().getValue())) {
                    DebugLog.d("not json type");
                    DebugLog.d(ConvertInputStreamToString(data));
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
                HttpPost request = new HttpPost(APIList.uploadStatusUrl() + "?access_token=" + APIHelper.getAccessToken(MyApplication.getInstance()));
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

}