package com.sap.inspection.manager;

import android.nfc.FormatException;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rindang.zconfig.APIList;
import com.sap.inspection.BuildConfig;
import com.sap.inspection.R;
import com.sap.inspection.TowerApplication;
import com.sap.inspection.connection.APIHelper;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.event.UploadProgressEvent;
import com.sap.inspection.model.config.formimbaspetir.FormImbasPetirConfig;
import com.sap.inspection.model.form.WorkFormItemModel;
import com.sap.inspection.model.responsemodel.BaseResponseModel;
import com.sap.inspection.model.responsemodel.ItemDefaultResponseModel;
import com.sap.inspection.model.value.CorrectiveValueModel;
import com.sap.inspection.model.value.FormValueModel;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.CommonUtil;
import com.sap.inspection.util.PrefUtil;
import com.sap.inspection.util.StringUtil;

import org.apache.http.HttpEntity;
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Objects;

import de.greenrobot.event.EventBus;

import static com.sap.inspection.model.value.FormValueModel.isPictureRadioItemValidated;

public class ItemUploadManager {

    private static ItemUploadManager instance;
    private ArrayList<FormValueModel> itemValues;
    private ArrayList<FormValueModel> itemValuesFailed;
    private ArrayList<FormValueModel> itemValuesModified;

    private boolean running = false;
    public final String syncDone = "Sinkronisasi selesai";
    public final String syncFail = "Sinkronisasi gagal";
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

    public FormValueModel convertToFormValue(CorrectiveValueModel itemValue) {
        FormValueModel formValue = new FormValueModel();
        formValue.scheduleId    = itemValue.scheduleId;
        formValue.operatorId    = itemValue.operatorId;
        formValue.itemId        = itemValue.itemId;
        formValue.siteId        = itemValue.siteId;
        formValue.gpsAccuracy   = itemValue.gpsAccuracy;
        formValue.rowId         = itemValue.rowId;
        formValue.remark        = itemValue.remark;
        formValue.photoStatus   = itemValue.photoStatus;
        formValue.latitude      = itemValue.latitude;
        formValue.longitude     = itemValue.longitude;
        formValue.value         = itemValue.value;
        formValue.uploadStatus  = itemValue.uploadStatus;
        formValue.typePhoto     = itemValue.typePhoto;
        return formValue;
    }

    public void addCorrectiveValues(ArrayList<CorrectiveValueModel> correctiveValues) {
        ArrayList<FormValueModel> itemValuesForUpload = new ArrayList<>();
        for (CorrectiveValueModel itemValue : correctiveValues) {
            itemValuesForUpload.add(convertToFormValue(itemValue));
        }
        addItemValues(itemValuesForUpload);
    }

    public void addCorrectiveValue(WorkFormItemModel workFormItem, CorrectiveValueModel correctiveValue) {
        addItemValue(workFormItem, convertToFormValue(correctiveValue));
    }

    public void addItemValues(ArrayList<FormValueModel> itemvalues) {
        if (itemvalues == null)
            return;

        this.itemValues.clear();
        this.itemValuesFailed.clear();
        this.itemValuesModified.clear();
        for (FormValueModel item : itemvalues) {
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

    public void addItemValue(WorkFormItemModel workFormItem, FormValueModel filledItem) {

        // if the filled items are mandatory, then apply strict rules
        if (workFormItem.mandatory) {

            // for non-TYPE_PICTURE_RADIO
            if (TextUtils.isEmpty(filledItem.value)) {

                if (workFormItem.field_type.equalsIgnoreCase("file") && !TextUtils.isEmpty(filledItem.photoStatus) && !filledItem.photoStatus.equalsIgnoreCase(Constants.NA))
                    TowerApplication.getInstance().toast("Photo item" + workFormItem.label + " harus ada", Toast.LENGTH_LONG);
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
            TowerApplication.getInstance().toast(TowerApplication.getContext().getResources().getString(R.string.uploadProses), Toast.LENGTH_SHORT);
        }

    }

    private class UploadValue extends AsyncTask<Void, String, Boolean> {

        private String scheduleId;

        // upload variables
        private final String MESSAGE_SUCCESS = "success";
        private final String MESSAGE_FAILED  = "failed";
        private String message = "";
        private String messageToServer;
        private int itemValueSuccessCount = 0;

        // connnection setup
        private final int TIMEOUT_CONNECTION = 1 * 3600 * 1000;
        private final int TIMEOUT_SOCKET = 1 * 3600 * 1000;
        private final String UPLOAD_URL = APIList.uploadUrl() + "?access_token=" + APIHelper.getAccessToken(TowerApplication.getInstance());
        private HttpClient httpClient;

        public UploadValue() {}

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            running = true;
            initConnection();
        }

        private void publish(String msg) {
            publishProgress(msg);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            UploadProgressEvent event = new UploadProgressEvent(values[0]);
            if (!running) event = new UploadProgressEvent(values[0], true);
            EventBus.getDefault().post(event);
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {

            Boolean result = true;
            try {
                while (itemValues.size() > 0) {
                    publish(itemValues.size() + " item yang tersisa");

                    /** upload Photo **/
                    HttpResponse httpResponse = uploadItem2(itemValues.get(0));
                    if (httpResponse != null) {
                        HttpEntity httpEntity = httpResponse.getEntity();
                        ItemDefaultResponseModel responseUploadItemModel;
                        InputStream data = httpEntity.getContent();
                        String jsonResponse = StringUtil.ConvertInputStreamToString(data); // get json response
                        if (!TextUtils.isEmpty(jsonResponse)) {
                            if (!StringUtil.checkIfContentTypeJson(httpResponse.getEntity().getContentType().getValue())) {
                                result = false;
                                throw new FormatException("upload: response is not json type");
                            }
                            scheduleId = itemValues.get(0).scheduleId;
                            FormValueModel item = itemValues.remove(0);
                            item.uploadStatus = FormValueModel.UPLOAD_DONE;
                            responseUploadItemModel = new Gson().fromJson(jsonResponse, ItemDefaultResponseModel.class);
                            if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {
                                checkWargaAndBarangIDafterUpload(item, responseUploadItemModel);
                            }
                            item.save();

                            itemValueSuccessCount++;
                            DebugLog.d("== response ==");
                            DebugLog.d(responseUploadItemModel.toString());

                            int statuscode = responseUploadItemModel.status;
                            if (statuscode >= 200 && statuscode < 300) {
                                DebugLog.d("item with id(" + item.itemId + ") upload success");
                            } else {
                                itemValuesFailed.add(item);
                                message += responseUploadItemModel.messages + "\n";
                                DebugLog.e("item with id(" + item.itemId + ") upload failed");
                                result = false;
                            }
                        } else {
                            result = false;
                            throw new NullPointerException("upload: json response is null");
                        }
                    } else {
                        result = false;
                        throw new NullPointerException("upload: http response is null");
                    }
                }
            } catch (IOException e) {
                DebugLog.e("upload: " + e.getMessage(), e);
            } catch (NullPointerException e) {
                DebugLog.e("upload: " + e.getMessage(), e);
            } catch (FormatException e) {
                DebugLog.e("upload: " + e.getMessage(), e);
            }
            return result;
        }

        private void initConnection() {
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_CONNECTION);
            HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_SOCKET);
            httpClient = new DefaultHttpClient(httpParams);
        }

        private HttpResponse uploadItem2(FormValueModel itemValue) throws IOException {
            DebugLog.d("===== START UPLOADING PHOTO === \n");
            HttpPost httpPost = new HttpPost(UPLOAD_URL);
            httpPost.setHeader("Cookie", PrefUtil.getStringPref(R.string.user_cookie, ""));
            DebugLog.d(httpPost.getURI().toString());

            MultipartEntity reqEntity = getUploadEntity(itemValue);
            httpPost.setEntity(reqEntity);
            return httpClient.execute(httpPost);
        }

        private MultipartEntity getUploadEntity(FormValueModel itemValue) throws IOException {
            MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            ArrayList<NameValuePair> params = getParams(itemValue);

            if (!TextUtils.isEmpty(itemValue.photoStatus) && !TextUtils.isEmpty(itemValue.value)) {

                File fileUpload = new File(itemValue.value.replaceFirst("^file\\:\\/\\/", ""));
                if (fileUpload.exists() && fileUpload.isFile()) {
                    String fileName = fileUpload.getName();
                    if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP))
                        reqEntity.addPart("picture", new ByteArrayBody(Objects.requireNonNull(CommonUtil.getDecryptedByteBase64(fileUpload)), fileName));
                    else if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_STP))
                        reqEntity.addPart("picture", new FileBody(fileUpload));
                } else {
                    throw new IOException(itemValue.value + " (No such file or directory)");
                }
            }

            for (int i = 0; i < params.size(); i++) {
                DebugLog.d(params.get(i).getName() + " || " + params.get(i).getValue());
                reqEntity.addPart(params.get(i).getName(), new StringBody(params.get(i).getValue()));
            }

            System.gc();
            return reqEntity;
        }

        private ArrayList<NameValuePair> getParams(FormValueModel itemValue) {

            ArrayList<NameValuePair> params = new ArrayList<>();

            params.add(new BasicNameValuePair("schedule_id", itemValue.scheduleId));
            params.add(new BasicNameValuePair("operator_id", String.valueOf(itemValue.operatorId)));
            params.add(new BasicNameValuePair("item_id", String.valueOf(itemValue.itemId)));

            if (!TextUtils.isEmpty(itemValue.value))
                params.add(new BasicNameValuePair("value", itemValue.value));
            if (!TextUtils.isEmpty(itemValue.photoStatus)) {
                params.add(new BasicNameValuePair("photo_status", itemValue.photoStatus));
                if (!TextUtils.isEmpty(itemValue.value)) {
                    if (!TextUtils.isEmpty(itemValue.photoDate)) {
                        params.add(new BasicNameValuePair("photo_datetime", itemValue.photoDate));
                        DebugLog.d("photo_datetime : " + itemValue.photoDate);
                    } else {
                        params.add(new BasicNameValuePair("photo_datetime", String.valueOf(itemValue.createdAt)));
                        DebugLog.d("photo_datetime : " + itemValue.createdAt);
                    }
                }
            }
            if (!TextUtils.isEmpty(itemValue.remark))
                params.add(new BasicNameValuePair("remark", itemValue.remark));
            if (!TextUtils.isEmpty(itemValue.latitude) && !itemValue.latitude.equalsIgnoreCase("0"))
                params.add(new BasicNameValuePair("latitude", itemValue.latitude));
            if (!TextUtils.isEmpty(itemValue.longitude) && !itemValue.longitude.equalsIgnoreCase("0"))
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

        private ArrayList<NameValuePair> getParamUploadStatus(String schedule_id, String messageToServer) {
            ArrayList<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("schedule_id", schedule_id));
            params.add(new BasicNameValuePair("message", messageToServer));
            return params;
        }

        private void checkWargaAndBarangIDafterUpload(FormValueModel item, ItemDefaultResponseModel responseUploadItemModel) {

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
                    //FormValueModel.updateWargaId(item.scheduleId, oldWargaId, newWargaId);
                    FormValueModel.updateWargaItems(oldWargaId, newWargaId, itemValuesModified);

                    // update formimbaspetirconfig
                    updateWargaId(oldWargaId, newWargaId);

                    return;
                }
            }

            if (StringUtil.isNotNullAndNotEmpty(oldBarangId) && StringUtil.isNotRegistered(oldBarangId)) {

                if (responseUploadItemModel.data.getBarang_id() != 0) {

                    newBarangId = String.valueOf(responseUploadItemModel.data.getBarang_id());

                    // update all items with old 'barangId' value on FormValue table to the new one
                    FormValueModel.updateBarangItems(oldWargaId, oldBarangId, newBarangId, itemValuesModified);

                    // update formimbaspetirconfig
                    updateBarangId(oldWargaId, oldBarangId, newBarangId);

                    return;
                }
            }
        }

        private FormValueModel checkWargaAndBarangID(FormValueModel item) {
            item.wargaId    = StringUtil.getRegisteredWargaId(item.scheduleId, item.wargaId);
            item.barangId   = StringUtil.getRegisteredBarangId(item.scheduleId, item.wargaId, item.barangId);
            return item;
        }

        private void checkWargaIdAndBarangId(FormValueModel item, ItemDefaultResponseModel responseUploadItemModel) {

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
                FormValueModel.delete(item.scheduleId, item.itemId, item.operatorId, oldWargaId, oldBarangId);
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

        private void doUploadStatus() {
            // SAP only
            new Thread(() -> { // upload status in a new thread
                if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)){
                    //after uploading data, then send messageToServer to the backend server
                    String response = uploadStatus(scheduleId, messageToServer);
                    if (!TextUtils.isEmpty(response)) {
                        BaseResponseModel responseUploadStatus = new Gson().fromJson(response, BaseResponseModel.class);
                        if (responseUploadStatus.status >= 200 && responseUploadStatus.status < 300) {
                            DebugLog.d("upload status berhasil");
                        } else if (responseUploadStatus.status >= 400) {
                            DebugLog.e("upload status gagal dengan statuscode : " + responseUploadStatus.status);
                        }
                    }
                }
            }).start();
        }

        private String uploadStatus(String schedule_id, String messageToServer) {
            DebugLog.d("===== START UPLOADING STATUS === \n");
            String responseStringData = null;
            try {
                HttpPost request = new HttpPost(APIList.uploadStatusUrl() + "?access_token=" + APIHelper.getAccessToken(TowerApplication.getInstance()));
                DebugLog.d(request.getURI().toString());

                ArrayList<NameValuePair> params = getParamUploadStatus(schedule_id, messageToServer);
                request.setEntity(new UrlEncodedFormEntity(params));

                //Execution Part
                DebugLog.d("\n\n** execute request ** ");
                InputStream data;
                HttpResponse response = httpClient.execute(request);

                //Response Part
                DebugLog.d("** response ** ");
                data = response.getEntity().getContent();
                int statusCode = response.getStatusLine().getStatusCode();
                responseStringData = StringUtil.ConvertInputStreamToString(data);
                DebugLog.d("schedule_id : " + schedule_id);
                DebugLog.d("messageToServer : " + messageToServer);
                DebugLog.d("response status code : " + statusCode);
                DebugLog.d("response string data : " + responseStringData);
                DebugLog.d("====== END OF UPLOAD STATUS ===== \n\n");
            } catch (SocketTimeoutException e) {
                DebugLog.e("upload status: " + e.getMessage(), e);
            } catch (ClientProtocolException e) {
                DebugLog.e("upload status: " + e.getMessage(), e);
            } catch (IOException e) {
                DebugLog.e("upload status: " + e.getMessage(), e);
            }
            return responseStringData;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (scheduleId != null && StringUtil.isPreventive(scheduleId)) {
                DebugLog.d("hit corrective");
                APIHelper.getJsonFromUrl(TowerApplication.getContext(), null, APIList.uploadConfirmUrl() + scheduleId + "/update");
            }

            if (!result) { // failed upload items
                latestStatus = syncFail;
                messageToServer = MESSAGE_FAILED;

                // save failed item's status
                if (!itemValuesFailed.isEmpty()) {
                    for (FormValueModel item : itemValuesFailed) {
                        item.uploadStatus = FormValueModel.UPLOAD_FAIL;
                        item.save();
                    }
                    message += itemValuesFailed.size() + " item gagal diupload";
                } else {
                    message += TowerApplication.getContext().getString(R.string.error_upload_item);
                }

            } else { // success upload items
                latestStatus = syncDone;
                messageToServer = MESSAGE_SUCCESS;
                message = itemValueSuccessCount + " item berhasil diupload";
            }

            running = false;
            publish(latestStatus + "\n" + message);
            TowerApplication.getInstance().toast(latestStatus + "\n" + message, Toast.LENGTH_LONG);

            // SAP only
            doUploadStatus();

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            latestStatus = syncFail;
            messageToServer = MESSAGE_FAILED;
            running = false;
            publish(latestStatus + "\ncancelled");

            // SAP only
            doUploadStatus();

        }
    }


}