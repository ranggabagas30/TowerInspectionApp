package com.sap.inspection;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.facebook.stetho.common.Util;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.rindang.zconfig.APIList;
import com.sap.inspection.connection.JSONConnection;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.constant.GlobalVar;
import com.sap.inspection.model.DbManager;
import com.sap.inspection.model.DbRepository;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.ScheduleGeneral;
import com.sap.inspection.model.responsemodel.BaseResponseModel;
import com.sap.inspection.model.responsemodel.CheckinRepsonseModel;
import com.sap.inspection.tools.DateTools;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.tools.PersistentLocation;
import com.sap.inspection.util.LocationRequestProvider;
import com.sap.inspection.util.Utility;
import com.sap.inspection.view.FormInputText;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.Header;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class CheckInActivity extends BaseActivity implements LocationRequestProvider.LocationCallback{

    /* variabel for location data checking to meet criteria */
    private LocationRequestProvider mLocationRequestProvider;
    private Location mSiteCoordinate, mCurrentCoordinate;
    private float mDistanceMeasurment, mAccuracy;
    private boolean mIsLocationRetrieved;

    /* variabel for get extra data from NavigationAdapter */
    private int mExtraSiteId, mExtraWorkTypeId;
    private String mExtraScheduleId, mExtraDayDate;

    /* variabel for intent */
    private static final int NOTIFICATION_CHECK_IN = 101;
    private NotificationManager mNotificationManager;
    private Notification.Builder mNotificationBuilder;
    private Notification mNotification;
    private PendingIntent mPendingIntent;
    private Intent mToFormFillActivityIntent;

    /* variabel for doing post to server */
    int timeoutConnection = 10 * 1000; // 10 seconds
    int timeoutSocket = 10 * 1000; // 10 seconds
    HttpParams httpParameters;
    HttpClient client;
    HttpPost request;
    InputStream data;
    HttpResponse response;
    CheckinBackgroundTask checkinBackgroundTask;
    ScheduleBaseModel mScheduleData;

    /* contants for parameter field */
    private static final String FIELD_SCHEDULE_ID   = "schedule_id";
    private static final String FIELD_SITE_ID       = "site_id";
    private static final String FIELD_SITE_NAME     = "site_name";
    private static final String FIELD_PERIOD        = "period";
    private static final String FIELD_SITE_LAT      = "site_lat";
    private static final String FIELD_SITE_LONG     = "site_long";
    private static final String FIELD_CURRENT_LAT   = "current_lat";
    private static final String FIELD_CURRENT_LONG  = "current_long";
    private static final String FIELD_DISTANCE      = "distance";
    private static final String FIELD_TIME          = "time";
    private static final String FIELD_STATUS        = "status";

    /* param object class */
    private class ParamCheckin {

        private int scheduleId;
        private int siteId;
        private String siteName;
        private String period;
        private String siteLat;
        private String siteLong;
        private String currentLat;
        private String currentLong;
        private float distance;
        private String time;
        private String status;

        private ArrayList<NameValuePair> paramNameValuePair;

        public ParamCheckin() {
        }

        public int getScheduleId() {
            return scheduleId;
        }

        public void setScheduleId(int scheduleId) {
            this.scheduleId = scheduleId;
        }

        public int getSiteId() {
            return siteId;
        }

        public void setSiteId(int siteId) {
            this.siteId = siteId;
        }

        public String getSiteName() {
            return siteName;
        }

        public void setSiteName(String siteName) {
            this.siteName = siteName;
        }

        public String getPeriod() {
            return period;
        }

        public void setPeriod(String period) {
            this.period = period;
        }

        public String getSiteLat() {
            return siteLat;
        }

        public void setSiteLat(String siteLat) {
            this.siteLat = siteLat;
        }

        public String getSiteLong() {
            return siteLong;
        }

        public void setSiteLong(String siteLong) {
            this.siteLong = siteLong;
        }

        public String getCurrentLat() {
            return currentLat;
        }

        public void setCurrentLat(String currentLat) {
            this.currentLat = currentLat;
        }

        public String getCurrentLong() {
            return currentLong;
        }

        public void setCurrentLong(String currentLong) {
            this.currentLong = currentLong;
        }

        public float getDistance() {
            return distance;
        }

        public void setDistance(float distance) {
            this.distance = distance;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public void compileParamNameValuePair() {
            DebugLog.d("compiling param to NameValuePair");

            paramNameValuePair = new ArrayList<>();
            paramNameValuePair.add(new BasicNameValuePair(FIELD_SCHEDULE_ID, String.valueOf(this.scheduleId)));
            paramNameValuePair.add(new BasicNameValuePair(FIELD_SITE_ID, String.valueOf(this.siteId)));
            paramNameValuePair.add(new BasicNameValuePair(FIELD_SITE_NAME, this.siteName));
            paramNameValuePair.add(new BasicNameValuePair(FIELD_PERIOD, String.valueOf(this.period)));
            paramNameValuePair.add(new BasicNameValuePair(FIELD_SITE_LAT, this.siteLat));
            paramNameValuePair.add(new BasicNameValuePair(FIELD_SITE_LONG, this.siteLong));
            paramNameValuePair.add(new BasicNameValuePair(FIELD_CURRENT_LAT, this.currentLat));
            paramNameValuePair.add(new BasicNameValuePair(FIELD_CURRENT_LONG, this.currentLong));
            paramNameValuePair.add(new BasicNameValuePair(FIELD_DISTANCE, String.valueOf(this.distance)));
            paramNameValuePair.add(new BasicNameValuePair(FIELD_TIME, this.time));
            paramNameValuePair.add(new BasicNameValuePair(FIELD_STATUS, this.status));
        }

        public ArrayList<NameValuePair> getParamNameValuePair() {
            return paramNameValuePair;
        }

    }
    ParamCheckin mParamObject;

    /* UI Component objects declaration */
    private Button mBtnCheckin;
    private FormInputText mSiteID, mSiteName, mPMPeriod, mSiteLat, mSiteLong;
    private FormInputText mCurrentLat, mCurrentLong, mDistanceToSite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);

        if (!DbRepository.getInstance().getDB().isOpen()) {
            DbRepository.getInstance().open(activity);
        }

        getBundleDataFromScheduleFragment();
        preparingScheduleAndSiteData();

        mIsLocationRetrieved = false;

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mBtnCheckin     = (Button) findViewById(R.id.buttoncheckin);
        mSiteID         = (FormInputText) findViewById(R.id.input_text_siteid_stp);
        mSiteName       = (FormInputText) findViewById(R.id.input_text_site_name);
        mPMPeriod       = (FormInputText) findViewById(R.id.input_text_pmperiod);
        mSiteLat        = (FormInputText) findViewById(R.id.input_text_site_latitude);
        mSiteLong       = (FormInputText) findViewById(R.id.input_text_site_longitude);
        mCurrentLat     = (FormInputText) findViewById(R.id.input_text_current_latitude);
        mCurrentLong    = (FormInputText) findViewById(R.id.input_text_current_longitude);
        mDistanceToSite = (FormInputText) findViewById(R.id.input_text_distance_to_site);

        /* disable components while retrieving location data */
        mBtnCheckin.setEnabled(false);
        mSiteID.setEnabled(false);
        mSiteName.setEnabled(false);
        mPMPeriod.setEnabled(false);
        mSiteLat.setEnabled(false);
        mSiteLong.setEnabled(false);
        mCurrentLat.setEnabled(false);
        mCurrentLong.setEnabled(false);
        mDistanceToSite.setEnabled(false);


        mLocationRequestProvider = new LocationRequestProvider(this, this);

        mBtnCheckin.setOnClickListener(view -> {

            postCheckinDataToSERVER();

            if (!isLocationError()) {

                if (localValidation()) {

                    showSuccessCheckinMessage();
                    //startCheckoutCountdown();
                    keepCurrentLocationDataTobeUsed();
                    navigateToFormActivity();

                } else {

                    showFailCheckinMessage();

                }
            } else {

                showLocationGPSError();

            }

        });

    }

    /**
     *  implemented interface functions
     *  onStart()  --> init location services
     *  onResume() --> check gps
     *  onStop()   -->  stop location services
     *  handleNewLocation() --> location change captured
     *  */
    @Override
    protected void onStart() {
        super.onStart();
        initLocationServices();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Utility.checkNetworkStatus(this) || !Utility.checkNetworkStatus(this)) {
            mLocationRequestProvider.showGPSDialog();
        }
        if (!DbRepository.getInstance().getDB().isOpen()) {
            DbRepository.getInstance().open(activity);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        DebugLog.d("onStop");
        stopLocationServices();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DebugLog.d("onDestroy");
        if (DbRepository.getInstance().getDB().isOpen()){
            DbRepository.getInstance().close();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void handleNewLocation(Location location) {
        /* location is retrieved every 3 - 5 seconds !*/
        mCurrentCoordinate = location;
        mIsLocationRetrieved = true;

        processLocationData();
        updateCheckinDataRequirements();
        showCheckinDataRequirementsToForm();

        mBtnCheckin.setEnabled(true);
        mSiteID.setEnabled(true);
        mSiteName.setEnabled(true);
        mPMPeriod.setEnabled(true);
        mSiteLat.setEnabled(true);
        mSiteLong.setEnabled(true);
        mCurrentLat.setEnabled(true);
        mCurrentLong.setEnabled(true);
        mDistanceToSite.setEnabled(true);
    }

    /**
     * initLocationServices() --> connect to google api client
     * stopLocationServices() --> disconnect from google api client and remove location updates
     * isCheckInGranted()     --> check devices location and accuracy, whether meets survey requirements or not
     * showFailCheckInMessages() --> show a message when check in activity returns fail result
     * showSuccessCheckinMessage() --> show a message when check in activity returns success result
     * navigateToFormActivity() --> go to FormFillActivity (survey form)
     * */
    private void initLocationServices() {
        mLocationRequestProvider.connect();
    }

    private void stopLocationServices() {
        mLocationRequestProvider.disconnect();
    }

    private boolean isCheckInGranted() {
        return serverValidation() && localValidation();
    }

    private boolean isLocationError() {
        return Utility.isCurrentLocationError(mCurrentCoordinate.getLatitude(), mCurrentCoordinate.getLongitude());
    }

    private boolean serverValidation() {
        return checkinBackgroundTask.serverValidationResult();
    }

    private boolean localValidation() {
        return true;
        //return mDistanceMeasurment <= 200 && mAccuracy <= 20;
    }

    private void showFailCheckinMessage() {
        String mFailCheckinMessage = getResources().getString(R.string.failedmessagecheckin);

        MyApplication.getInstance().toast(mFailCheckinMessage + ".\n" +
                "Current GPS accuration : " + mAccuracy + " meters. \n" +
                "Current lat : " + mCurrentCoordinate.getLatitude() + "\n" +
                "Current long : " + mCurrentCoordinate.getLongitude() + "\n" +
                "Current distance from site coordinate : " + mDistanceMeasurment + " meters", Toast.LENGTH_LONG);
    }

    private void showSuccessCheckinMessage() {
        String mSuccessMessage = getResources().getString(R.string.successmessagecheckin);

        MyApplication.getInstance().toast(mSuccessMessage + ".\n" +
                "Current GPS accuration : " + mAccuracy + " meters. \n" +
                "Current lat : " + mCurrentCoordinate.getLatitude() + "\n" +
                "Current long : " + mCurrentCoordinate.getLongitude() + "\n" +
                "Current distance from site coordinate : " + mDistanceMeasurment + " meters", Toast.LENGTH_SHORT);
    }

    private void showLocationGPSError() {
        MyApplication.getInstance().toast(this.getResources().getString(R.string.sitelocationisnotaccurate), Toast.LENGTH_SHORT);
    }

    private void keepCurrentLocationDataTobeUsed() {
        PersistentLocation.getInstance().deletePersistentLatLng();
        Utility.setPersistentLocation(mExtraScheduleId, mCurrentLat.getText().toString(), mCurrentLong.getText().toString());
    }

    private void navigateToFormActivity() {

        mToFormFillActivityIntent = new Intent(this, FormActivity.class);
        mToFormFillActivityIntent.putExtra("scheduleId", mExtraScheduleId);
        mToFormFillActivityIntent.putExtra("siteId", mExtraSiteId);
        mToFormFillActivityIntent.putExtra("dayDate", mExtraDayDate);
        mToFormFillActivityIntent.putExtra("workTypeId", mExtraWorkTypeId);

        startActivity(mToFormFillActivityIntent);
    }

    private boolean showNotification() {
        mPendingIntent = PendingIntent.getActivity(this, 0, mToFormFillActivityIntent, 0);
        mNotificationBuilder = new Notification.Builder(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mNotificationBuilder.setContentIntent(mPendingIntent)
                                .setContentTitle("Check in status")
                                .setContentText(getText(R.string.successmessagecheckin))
                                .setSmallIcon(R.drawable.logo_app)
                                .setOngoing(true)
                                .setAutoCancel(true)
                                .build();
            mNotification = mNotificationBuilder.getNotification();
            mNotificationManager.notify(NOTIFICATION_CHECK_IN, mNotification);
            return true;
        } else
            return false;

    }

    private void getBundleDataFromScheduleFragment() {
        Bundle receivedData = getIntent().getExtras();
        mExtraScheduleId = receivedData != null ? receivedData.getString("scheduleId") : null;
        mExtraSiteId = receivedData != null ? receivedData.getInt("siteId") : 0;
        mExtraDayDate = receivedData != null ? receivedData.getString("dayDate") : null;
        mExtraWorkTypeId = receivedData != null ? receivedData.getInt("workTypeId") : 0;
    }

    private void preparingScheduleAndSiteData() {
        mSiteCoordinate    = new Location(LocationManager.GPS_PROVIDER);
        mParamObject = new ParamCheckin();   
        mScheduleData = new ScheduleGeneral();
        mScheduleData = mScheduleData.getScheduleById(mExtraScheduleId);

        String[] siteCoordinate = mScheduleData.site.locationStr.split(",");
        mSiteCoordinate.setLatitude(Double.parseDouble(siteCoordinate[0]));
        mSiteCoordinate.setLongitude(Double.parseDouble(siteCoordinate[1]));

        /* assigning persistent data from database */
        mParamObject.setScheduleId(Integer.parseInt(mScheduleData.id));
        mParamObject.setSiteId(mScheduleData.site.id);
        mParamObject.setSiteName(mScheduleData.site.name);
        mParamObject.setPeriod(convertDayDateToPeriod(mScheduleData.day_date));
        mParamObject.setSiteLat(String.valueOf(mSiteCoordinate.getLatitude()));
        mParamObject.setSiteLong(String.valueOf(mSiteCoordinate.getLongitude()));
    }
    private void processLocationData() {

        mDistanceMeasurment = mSiteCoordinate.distanceTo(mCurrentCoordinate);
        mAccuracy = mCurrentCoordinate.getAccuracy();

        DebugLog.d("site coordinate : (" + mSiteCoordinate.getLatitude() + " , " + mSiteCoordinate.getLongitude() + ")");
        DebugLog.d("current coordinate : (" + mCurrentCoordinate.getLatitude() + " , " + mCurrentCoordinate.getLongitude() + ")");
    }

    private void updateCheckinDataRequirements() {
        mParamObject.setCurrentLat(String.valueOf(mCurrentCoordinate.getLatitude()));
        mParamObject.setCurrentLong(String.valueOf(mCurrentCoordinate.getLongitude()));
        mParamObject.setDistance(mDistanceMeasurment);
        mParamObject.setTime(DateTools.now());
        mParamObject.setStatus(localValidation() ? "success" : "failed");
    }

    private void showCheckinDataRequirementsToForm() {

        mSiteID.setText(String.valueOf(mParamObject.getSiteId()));
        mSiteName.setText(mParamObject.getSiteName());
        mPMPeriod.setText(String.valueOf(mParamObject.getPeriod()));
        mSiteLat.setText(mParamObject.getSiteLat());
        mSiteLong.setText(mParamObject.getSiteLong());
        mCurrentLat.setText(mParamObject.getCurrentLat());
        mCurrentLong.setText(mParamObject.getCurrentLong());
        mDistanceToSite.setText(String.valueOf(mParamObject.getDistance()));
    }

    private void postCheckinDataToSERVER() {

        mParamObject.compileParamNameValuePair();
        checkinBackgroundTask = new CheckinBackgroundTask();
        if (!checkinBackgroundTask.runningTask) {
            checkinBackgroundTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else {
            MyApplication.getInstance().toast("Mohon tunggu sebentar", Toast.LENGTH_SHORT);
        }
    }

    private String sendDataToSERVER() {
        //MyApplication.getInstance().toast("SENDING DATA TO SERVER", Toast.LENGTH_LONG);
        try {

            /* request part */
            httpParameters = new BasicHttpParams();
            client = new DefaultHttpClient(httpParameters);
            request = new HttpPost(APIList.checkinScheduleUrl(String.valueOf(mParamObject.getScheduleId())));
            request.setHeader("Cookie", getCookie());

            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

            System.gc();

            MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

            DebugLog.d("adding parameters...");
            for (NameValuePair param : mParamObject.getParamNameValuePair()) {
                reqEntity.addPart(param.getName(), new StringBody(param.getValue()));
                DebugLog.d(param.getName() + " : " + param.getValue());
            }

            /* response part */
            request.setEntity(reqEntity);
            response = client.execute(request);

            org.apache.http.Header cookie = response.getFirstHeader("Set-Cookie");
            if (cookie != null) {
                mPref.edit().putString(MyApplication.getContext().getString(R.string.user_cookie), cookie.getValue()).commit();
            }

            data = response.getEntity().getContent();
            int statusCode = response.getStatusLine().getStatusCode();
            String s = ConvertInputStreamToString(data);
            DebugLog.d("response string status code : " + statusCode);
            DebugLog.d("json /n" + s);

            if (!JSONConnection.checkIfContentTypeJson(response.getEntity().getContentType().getValue())) {
                DebugLog.d("not json type");
                boolean notJson;
                notJson = true;
                if (statusCode == 404) {
                    return s;
                } else {
                    return null;
                }
            }
            return s;

        } catch (UnsupportedEncodingException ue) {
            ue.getStackTrace();
            Crashlytics.log(Log.ERROR, "checkinactivity", ue.getMessage());
        } catch (SocketTimeoutException se) {
            se.printStackTrace();
            Crashlytics.log(Log.ERROR, "checkinactivity", se.getMessage());
        } catch (IOException ie) {
            ie.getStackTrace();
            Crashlytics.log(Log.ERROR, "checkinactivity", ie.getMessage());
        } catch (NullPointerException ne) {
            ne.printStackTrace();
            Crashlytics.log(Log.ERROR, "checkinactivity", ne.getMessage());
        }
        return null;
    }

    private boolean receiveDataFromSERVER(String response) {
        Gson gson = new Gson();

        CheckinRepsonseModel saveCheckinResponse = gson.fromJson(response, CheckinRepsonseModel.class);
        saveCheckinResponse.printLogResponse();
        DebugLog.d("status : " + saveCheckinResponse.status);
        DebugLog.d("status_code : " + saveCheckinResponse.status_code);

        if (saveCheckinResponse.status == 201 || saveCheckinResponse.status == 200) {
            //MyApplication.getInstance().toast("post data check in berhasil", Toast.LENGTH_LONG);
            return true;
        } else {
            //MyApplication.getInstance().toast("post data check in gagal", Toast.LENGTH_LONG);
            return false;
        }
    }

    private class CheckinBackgroundTask extends AsyncTask<Void, String, Void> {

        private Gson gson = new Gson();
        private String response = null;
        private boolean taskDone = false;
        private boolean runningTask = false;
        private boolean validatedByServer = false;

        public boolean serverValidationResult() {
            return !runningTask && validatedByServer;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            runningTask = true;
            mBtnCheckin.setEnabled(false);
            //MyApplication.getInstance().toast("Start check in", Toast.LENGTH_SHORT);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            DebugLog.d("progress : " + values);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            response = sendDataToSERVER();

            if (response != null) {
                validatedByServer = receiveDataFromSERVER(response);
            } else {
                DebugLog.d("Error response from server");
                //MyApplication.getInstance().toast("Error response from server", Toast.LENGTH_SHORT);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            runningTask = false;
            taskDone = true;
            mBtnCheckin.setEnabled(true);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            runningTask = false;
            taskDone = true;
            mBtnCheckin.setEnabled(true);
        }
    }

    /**
     * func def : to convert from YYYY-mm-dd format to MM-YYYY
     * @param : daydate (format YYYY-mm-dd)
     * @return : period (MM-YYYY)
     *
     * mechanism :
     * 1. split daydate by regex "-", so that arr[0] = YYYY, arr[1] = mm, and arr[2] = dd
     * 2. check arr[1] to get month index :
     *    a. IF arr[1] is a number less than 10, i.e. "04", THEN
     *       take second letter as month index (thus "4" is month index)
     *       else
     *    b. IF arr[1] is a number equal or more than 10, i.e. "11", THEN
     *       this is the month index (thus "11" is month index)
     *
     * 3. get month in string : month = Months[monthindex]
     * 4. get year in string : year = arr[0]
     * 5. return period : "month" - "year"
     *
     * */
    private String convertDayDateToPeriod(String daydate) {
        int monthindex;
        String month;
        String year;
        String period;
        String[] dateSplit;

        dateSplit = daydate.split("-");
        year  = dateSplit[0];
        month = dateSplit[1];

        DebugLog.d("month.charAt(0) : " + month.charAt(0));
        DebugLog.d("month.charAt(1) : " + month.charAt(1));

        if (month.charAt(0) == '0') {
            monthindex = Integer.parseInt(String.valueOf(month.charAt(1)));
        } else {
            monthindex = Integer.parseInt(month);
        }

        month = Constants.MONTHS[monthindex-1];

        period = month + "-" + year;

        DebugLog.d("monthindex : " + monthindex);
        DebugLog.d("month : " + month);
        DebugLog.d("period : " + period);
        return period;
    }

    private String getAccessToken(Context context) {
        SharedPreferences mpref = PreferenceManager.getDefaultSharedPreferences(context);
        return mpref.getString(context.getString(R.string.user_authToken), "");
    }

    private String getCookie() {
        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        if (mPref.getString(MyApplication.getContext().getString(R.string.user_cookie), null) != null) {
            return mPref.getString(MyApplication.getContext().getString(R.string.user_cookie), "");
        }
        return null;
    }

    private String ConvertInputStreamToString(InputStream is) {
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

    private void startCheckoutCountdown() {
        DebugLog.d("start countdown .... ");
        int CHECKIN_DURATION = 10; // seconds then back
        Runnable mRunnable  = new Runnable() {
            @Override
            public void run() {
                MyApplication.getInstance().toast("Checkout success", Toast.LENGTH_SHORT);
                Intent recheckinIntent = new Intent(CheckInActivity.this, CheckInActivity.class);
                recheckinIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(recheckinIntent);
            }
        };

        Handler mCheckoutHandler = new Handler();
        mCheckoutHandler.postDelayed(mRunnable, CHECKIN_DURATION * 1000);
    }
}
