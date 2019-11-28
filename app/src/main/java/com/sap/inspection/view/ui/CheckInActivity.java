package com.sap.inspection.view.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.rindang.zconfig.APIList;
import com.sap.inspection.BuildConfig;
import com.sap.inspection.R;
import com.sap.inspection.TowerApplication;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.constant.GlobalVar;
import com.sap.inspection.model.CheckinDataModel;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.ScheduleGeneral;
import com.sap.inspection.model.responsemodel.CheckinRepsonseModel;
import com.sap.inspection.model.responsemodel.FakeGPSResponseModel;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.tools.PersistentLocation;
import com.sap.inspection.util.CommonUtil;
import com.sap.inspection.util.DateUtil;
import com.sap.inspection.util.LocationRequestProvider;
import com.sap.inspection.util.PermissionUtil;
import com.sap.inspection.util.StringUtil;
import com.sap.inspection.view.customview.FormInputText;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class CheckInActivity extends BaseActivity implements LocationRequestProvider.LocationCallback, EasyPermissions.RationaleCallbacks{

    private final int DISTANCE_MINIMUM_IN_METERS = 100;
    private final int ACCURACY_MINIMUM = 50;
    private final int CHECKIN_DURATION = 3 ; // HOURS then back
    private final int CHECK_GPS_DURATION = 5; // seconds

    /* variabel for location data checking to meet criteria */
    private LocationRequestProvider mLocationRequestProvider;
    private Location mSiteCoordinate, mCurrentCoordinate, mPastCoordinate;
    private float mDistanceMeasurment, mAccuracy;
    private boolean mIsLocationRetrieved;

    /* variabel for get extra data from GroupsAdapter */
    private int mExtraSiteId, mExtraWorkTypeId;
    private String mExtraScheduleId, mExtraDayDate, mExtraWorkTypeName;

    /* variabel for intent */
    private static final int NOTIFICATION_CHECK_IN = 101;
    private NotificationManager mNotificationManager;
    private Notification.Builder mNotificationBuilder;
    private Notification mNotification;
    private PendingIntent mPendingIntent;

    /* variabel for doing post to server */
    int timeoutConnection =  1 * 3600 * 1000; // 1 HOUR
    int timeoutSocket = 1 * 3600 * 1000; // 1 HOUR

    HttpParams httpParameters;
    HttpClient client;
    HttpPost request;
    InputStream data;
    HttpResponse response;
    CheckinBackgroundTask checkinBackgroundTask;
    ScheduleGeneral mScheduleData;
    CheckinDataModel mParamObject;

    /* variabel handlers*/
    Handler mCheckoutHandler;
    Handler mCheckGPSHandler;
    Handler mSendReportFakeGPSHandler;
    Runnable mRunnableCheckoutHandler;
    Runnable mRunnableCheckGPSHandler;

    DisplayMetrics mMetrics;

    /* UI Component objects declaration */
    private ScrollView mScrollViewCheckin;
    private Button mBtnCheckin;
    private FormInputText mSiteIDCustomer, mSiteName, mPMPeriod, mSiteLat, mSiteLong;
    private FormInputText mCurrentLat, mCurrentLong, mDistanceToSite, mGPSAccuracy;
    private TextView mCheckinCriteria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);

        mPastCoordinate = new Location(LocationManager.GPS_PROVIDER);
        mCheckoutHandler = new Handler();
        mCheckGPSHandler = new Handler();

        getWindowConfiguration();
        getBundleDataFromScheduleFragment();
        preparingScheduleAndSiteData();

        mIsLocationRetrieved = false;

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mScrollViewCheckin      = findViewById(R.id.scollviewcheckin);
        mBtnCheckin             = findViewById(R.id.buttoncheckin);
        mSiteIDCustomer         = findViewById(R.id.input_text_siteid_stp);
        mSiteName               = findViewById(R.id.input_text_site_name);
        mPMPeriod               = findViewById(R.id.input_text_pmperiod);
        mSiteLat                = findViewById(R.id.input_text_site_latitude);
        mSiteLong               = findViewById(R.id.input_text_site_longitude);
        mCurrentLat             = findViewById(R.id.input_text_current_latitude);
        mCurrentLong            = findViewById(R.id.input_text_current_longitude);
        mDistanceToSite         = findViewById(R.id.input_text_distance_to_site);
        mGPSAccuracy            = findViewById(R.id.input_text_gps_accuracy);
        mCheckinCriteria        = findViewById(R.id.textcheckincriteria);

        /* disable components while retrieving location data */
        mBtnCheckin.setEnabled(true);
        mSiteIDCustomer.setEnabled(false);
        mSiteName.setEnabled(false);
        mPMPeriod.setEnabled(false);
        mSiteLat.setEnabled(false);
        mSiteLong.setEnabled(false);
        mCurrentLat.setEnabled(false);
        mCurrentLong.setEnabled(false);
        mDistanceToSite.setEnabled(false);
        mGPSAccuracy.setEnabled(false);

        mLocationRequestProvider = new LocationRequestProvider(this, this);
        mSendReportFakeGPSHandler = new Handler(message -> {
            hideDialog();
            Bundle bundle = message.getData();
            if (!TextUtils.isEmpty(bundle.getString("json"))) {
                FakeGPSResponseModel fakeGPSResponseModel = new Gson().fromJson(bundle.getString("json"), FakeGPSResponseModel.class);
                DebugLog.d(fakeGPSResponseModel.toString());
            }
            showSuccessCheckinMessage();
            startCheckoutCountdown();
            keepCurrentLocationDataTobeUsed();
            navigateToGroupActivity();
            return true;
        });

        setCheckinCriteriaText();

        mBtnCheckin.setOnClickListener(view -> {

            if (mIsLocationRetrieved) {

                postCheckinDataToSERVER();

                if (!isLocationError()) {

                    if (localValidation()) {

                        showMessageDialog("Checking");

                        if (!GlobalVar.getInstance().anyNetwork(this)) {
                            hideDialog();
                            TowerApplication.getInstance().toast(getString(R.string.error_no_internet_connection), Toast.LENGTH_LONG);
                            return;
                        }

                        if (!CommonUtil.checkFakeGPSAvailable(this, mCurrentCoordinate, String.valueOf(mExtraSiteId), mSendReportFakeGPSHandler)) {
                            hideDialog();
                            showSuccessCheckinMessage();
                            startCheckoutCountdown();
                            keepCurrentLocationDataTobeUsed();
                            navigateToGroupActivity();
                        }

                    } else {

                        showFailCheckinMessage();

                    }
                } else {

                    showLocationGPSError();

                }
            } else {

                showPleaseWaitMessage();

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
        requestLocationPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mIsLocationRetrieved = false;

        if (!CommonUtil.checkNetworkStatus(this) || !CommonUtil.checkGpsStatus(this)) {
            mLocationRequestProvider.showGPSDialog();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        DebugLog.d("onStop");
        stopLocationServices();
        mCheckGPSHandler.removeCallbacks(mRunnableCheckGPSHandler);
        mCheckoutHandler.removeCallbacks(mRunnableCheckoutHandler);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DebugLog.d("onDestroy");
    }

    @Override
    public void handleNewLocation(Location location) {

        /* check GPS every 3 seconds everytime get new location */
        startCheckGPSHandler();

        /* location is retrieved every 3 - 5 seconds !*/
        mCurrentCoordinate = location;
        mPastCoordinate = mCurrentCoordinate;
        mIsLocationRetrieved = true;

        processLocationData();
        updateCheckinDataRequirements();
        showCheckinDataRequirementsToForm();

        mBtnCheckin.setEnabled(true);
    }

    /**
     * initLocationServices() --> connect to google api client
     * stopLocationServices() --> disconnect from google api client and remove location updates
     * isCheckInGranted()     --> check devices location and accuracy, whether meets survey requirements or not
     * showFailCheckInMessages() --> show a message when check in activity returns fail result
     * showSuccessCheckinMessage() --> show a message when check in activity returns success result
     * navigateToGroupActivity() --> go to FormFillActivity (survey form)
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
        if (BuildConfig.BUILD_TYPE.equalsIgnoreCase("debug")) {
            //return CommonUtil.isCurrentLocationError(0.0, 0.0);
            return false;
        } else {
            return CommonUtil.isCurrentLocationError(mCurrentCoordinate.getLatitude(), mCurrentCoordinate.getLongitude());
        }

        //return CommonUtil.isCurrentLocationError(mCurrentCoordinate.getLatitude(), mCurrentCoordinate.getLongitude());
    }

    private boolean serverValidation() {
        return checkinBackgroundTask.serverValidationResult();
    }

    private boolean localValidation() {
        if (BuildConfig.BUILD_TYPE.equalsIgnoreCase("debug")) {
            return true;
        } else {
            return mDistanceMeasurment <= DISTANCE_MINIMUM_IN_METERS && mAccuracy <= ACCURACY_MINIMUM;
        }

        //return mDistanceMeasurment <= DISTANCE_MINIMUM_IN_METERS && mAccuracy <= ACCURACY_MINIMUM;
    }

    private void showFailCheckinMessage() {
        String mFailCheckinMessage = getResources().getString(R.string.failedmessagecheckin);

        TowerApplication.getInstance().toast(mFailCheckinMessage + ".\n" +
                "Current GPS accuration : " + mAccuracy + " meters. \n" +
                "Current lat : " + mCurrentCoordinate.getLatitude() + "\n" +
                "Current long : " + mCurrentCoordinate.getLongitude() + "\n" +
                "Current distance from site coordinate : " + mDistanceMeasurment + " meters", Toast.LENGTH_LONG);
    }

    private void showSuccessCheckinMessage() {
        String mSuccessMessage = getResources().getString(R.string.successmessagecheckin);

        TowerApplication.getInstance().toast(mSuccessMessage + ".\n" +
                "Current GPS accuration : " + mAccuracy + " meters. \n" +
                "Current lat : " + mCurrentCoordinate.getLatitude() + "\n" +
                "Current long : " + mCurrentCoordinate.getLongitude() + "\n" +
                "Current distance from site coordinate : " + mDistanceMeasurment + " meters", Toast.LENGTH_SHORT);
    }

    private void showLocationGPSError() {
        TowerApplication.getInstance().toast(this.getResources().getString(R.string.sitelocationisnotaccurate), Toast.LENGTH_LONG);
    }

    private void showPleaseWaitMessage() {
        TowerApplication.getInstance().toast("Mohon tunggu. Sedang proses mendapatkan lokasi", Toast.LENGTH_SHORT);
    }

    private void setCheckinCriteriaText() {
        String textCriteria  = "Anda harus berada dalam radius " + DISTANCE_MINIMUM_IN_METERS +
                " m dengan akurasi minimum " + ACCURACY_MINIMUM + " m untuk dapat checkin";

        mCheckinCriteria.setText(textCriteria);
    }

    private void keepCurrentLocationDataTobeUsed() {
        PersistentLocation.getInstance().deletePersistentLatLng();
        CommonUtil.setPersistentLocation(mExtraScheduleId, mCurrentLat.getText().toString(), mCurrentLong.getText().toString());
    }

    private void navigateToGroupActivity() {

        TowerApplication.getInstance().checkinDataModel = mParamObject;

        navigateToGroupActivity(
                this,
                mExtraScheduleId,
                mExtraSiteId,
                mExtraWorkTypeId,
                mExtraWorkTypeName,
                mExtraDayDate);
    }

    private void getBundleDataFromScheduleFragment() {
        Bundle receivedData = getIntent().getExtras();
        mExtraScheduleId = receivedData != null ? receivedData.getString(Constants.KEY_SCHEDULEID) : null;
        mExtraSiteId = receivedData != null ? receivedData.getInt(Constants.KEY_SITEID) : 0;
        mExtraDayDate = receivedData != null ? receivedData.getString(Constants.KEY_DAYDATE) : null;
        mExtraWorkTypeId = receivedData != null ? receivedData.getInt(Constants.KEY_WORKTYPEID) : 0;
        mExtraWorkTypeName = receivedData != null ? receivedData.getString(Constants.KEY_WORKTYPENAME) : null;
    }

    private void preparingScheduleAndSiteData() {
        mSiteCoordinate    = new Location(LocationManager.GPS_PROVIDER);
        mParamObject = new CheckinDataModel();
        mScheduleData = ScheduleBaseModel.getScheduleById(mExtraScheduleId);

        // TODO: fix on location str null
        String[] siteCoordinate = mScheduleData.site.locationStr.split(",");
        mSiteCoordinate.setLatitude(Double.parseDouble(siteCoordinate[0]));
        mSiteCoordinate.setLongitude(Double.parseDouble(siteCoordinate[1]));

        /* assigning persistent data from database */
        mParamObject.setScheduleId(Integer.parseInt(mScheduleData.id));
        mParamObject.setSiteIdCustomer(mScheduleData.site.site_id_customer);
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
        mParamObject.setTime(DateUtil.now());
        mParamObject.setStatus(localValidation() ? "success" : "failed");
        mParamObject.setAccuracy(mAccuracy);
    }

    private void showCheckinDataRequirementsToForm() {

        mSiteIDCustomer.setText(String.valueOf(mParamObject.getSiteIdCustomer()));
        mSiteName.setText(mParamObject.getSiteName());
        mPMPeriod.setText(String.valueOf(mParamObject.getPeriod()));
        mSiteLat.setText(mParamObject.getSiteLat());
        mSiteLong.setText(mParamObject.getSiteLong());
        mCurrentLat.setText(mParamObject.getCurrentLat());
        mCurrentLong.setText(mParamObject.getCurrentLong());
        mDistanceToSite.setText(mParamObject.getDistance() + " meters");
        mGPSAccuracy.setText(String.valueOf(mAccuracy));
        mGPSAccuracy.requestFocus();
    }

    private void postCheckinDataToSERVER() {

        mParamObject.compileParamNameValuePair();
        checkinBackgroundTask = new CheckinBackgroundTask();
        if (!checkinBackgroundTask.runningTask) {
            checkinBackgroundTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else {
            TowerApplication.getInstance().toast("Mohon tunggu sebentar", Toast.LENGTH_SHORT);
        }
    }

    private String sendDataToSERVER() {

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
                mPref.edit().putString(TowerApplication.getContext().getString(R.string.user_cookie), cookie.getValue()).commit();
            }

            data = response.getEntity().getContent();
            int statusCode = response.getStatusLine().getStatusCode();
            String s = ConvertInputStreamToString(data);
            DebugLog.d("response string status code : " + statusCode);
            DebugLog.d("json /n" + s);

            if (!StringUtil.checkIfContentTypeJson(response.getEntity().getContentType().getValue())) {
                DebugLog.d("not json type");

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
        CheckinRepsonseModel saveCheckinResponse = new Gson().fromJson(response, CheckinRepsonseModel.class);
        saveCheckinResponse.printLogResponse();
        DebugLog.d("status : " + saveCheckinResponse.status);
        DebugLog.d("status_code : " + saveCheckinResponse.status_code);
        return saveCheckinResponse.status == 201 || saveCheckinResponse.status == 200;
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

    private String getCookie() {
        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(TowerApplication.getContext());
        if (mPref.getString(TowerApplication.getContext().getString(R.string.user_cookie), null) != null) {
            return mPref.getString(TowerApplication.getContext().getString(R.string.user_cookie), "");
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
        mRunnableCheckoutHandler  = () -> {
            TowerApplication.getInstance().toast("Checkout success", Toast.LENGTH_SHORT);
            Intent recheckinIntent = new Intent(CheckInActivity.this, CheckInActivity.class);
            recheckinIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(recheckinIntent);
        };
        mCheckoutHandler.postDelayed(mRunnableCheckoutHandler, CHECKIN_DURATION * 3600 * 1000); // 3 hours
    }

    private void startCheckGPSHandler() {
        DebugLog.d("start check GPS...");

        mRunnableCheckGPSHandler = () -> {
            if (!CommonUtil.checkNetworkStatus(CheckInActivity.this) || !CommonUtil.checkNetworkStatus(CheckInActivity.this)) {
                mLocationRequestProvider.showGPSDialog();
            }
        };
        mCheckGPSHandler.removeCallbacks(mRunnableCheckGPSHandler);
        mCheckGPSHandler.postDelayed(mRunnableCheckGPSHandler, CHECK_GPS_DURATION * 1000); // every 5 seconds
    }

    private void getWindowConfiguration() {
        mMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
        DebugLog.d("metrics out width  : " + mMetrics.widthPixels);
        DebugLog.d("metrics out height : " + mMetrics.heightPixels);
    }

    @AfterPermissionGranted(Constants.RC_LOCATION_PERMISSION)
    private void requestLocationPermission() {

        DebugLog.d("request access location permission");
        if (PermissionUtil.hasPermission(this, PermissionUtil.ACCESS_FINE_LOCATION)) {

            // Already has permission, do the thing
            DebugLog.d("Already have permission, do the thing");
            initLocationServices();

        } else {

            // Do not have permissions, request them now
            DebugLog.d("Do not have permissions, request them now");
            PermissionUtil.requestPermission(this, getString(R.string.rationale_requestlocation), Constants.RC_LOCATION_PERMISSION, PermissionUtil.ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        DebugLog.d("request permission result");

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Constants.RC_LOCATION_PERMISSION) {

            if (PermissionUtil.hasPermission(this, PermissionUtil.ACCESS_FINE_LOCATION)) {

                DebugLog.d("access fine location allowed, start request location");
                initLocationServices();
            } else {
                finish();
            }
        }
    }

    @Override
    public void onRationaleAccepted(int requestCode) {

    }

    @Override
    public void onRationaleDenied(int requestCode) {
        if (requestCode == Constants.RC_LOCATION_PERMISSION) {
            Toast.makeText(activity, "Checkin dibatalkan. Mohon izinkan akses lokasi (Location Permission)", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
