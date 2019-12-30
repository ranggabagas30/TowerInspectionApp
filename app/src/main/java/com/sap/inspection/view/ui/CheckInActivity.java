package com.sap.inspection.view.ui;

import android.Manifest;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.sap.inspection.BuildConfig;
import com.sap.inspection.R;
import com.sap.inspection.TowerApplication;
import com.sap.inspection.connection.rest.TowerAPIHelper;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.constant.GlobalVar;
import com.sap.inspection.model.CheckinDataModel;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.ScheduleGeneral;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.tools.PersistentLocation;
import com.sap.inspection.util.CommonUtil;
import com.sap.inspection.util.DateUtil;
import com.sap.inspection.util.DialogUtil;
import com.sap.inspection.util.LocationRequestProvider;
import com.sap.inspection.util.NetworkUtil;
import com.sap.inspection.util.PermissionUtil;
import com.sap.inspection.view.customview.FormInputText;

import java.net.HttpURLConnection;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class CheckInActivity extends BaseActivity implements LocationRequestProvider.LocationCallback, EasyPermissions.RationaleCallbacks{

    private final int MAXIMUM_DISTANCE = 100;
    private final int MAXIMUM_ACCURACY = 50;

    /* variabel for location data checking to meet criteria */
    private LocationRequestProvider mLocationRequestProvider;
    private Location mSiteCoordinate, mCurrentCoordinate;
    private float mDistanceMeasurment, mAccuracy;

    /* variabel for get extra data from GroupsAdapter */
    private int mExtraSiteId, mExtraWorkTypeId;
    private String mExtraScheduleId, mExtraDayDate, mExtraWorkTypeName;

    private ScheduleGeneral mScheduleData;
    private CheckinDataModel mParamObject;

    DisplayMetrics mMetrics;

    /* UI Component objects declaration */
    private ScrollView mScrollViewCheckin;
    private Button mBtnCheckin;
    private FormInputText mSiteIDCustomer, mSiteName, mPMPeriod, mSiteLat, mSiteLong;
    private FormInputText mCurrentLat, mCurrentLong, mDistanceToSite, mGPSAccuracy;
    private TextView mCheckinCriteria;
    private CardView mErrorCard;
    private TextView mErrorMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);
        initView();
        getWindowConfiguration();
        getBundleDataFromScheduleFragment();
        preparingScheduleAndSiteData();

        mLocationRequestProvider = new LocationRequestProvider(this, this);

        mBtnCheckin.setOnClickListener(view -> {

            if (mSiteCoordinate == null) {
                TowerApplication.getInstance().toast(getString(R.string.error_site_location_empty), Toast.LENGTH_LONG);
                return;
            }

            if (!GlobalVar.getInstance().anyNetwork(this)) {
                TowerApplication.getInstance().toast(getString(R.string.error_no_internet_connection), Toast.LENGTH_LONG);
                return;
            }

            if (!CommonUtil.checkGpsStatus(this) && !CommonUtil.checkNetworkStatus(this)) {
                DialogUtil.showGPSdialog(this);
                return;
            }

            if (mCurrentCoordinate != null) {
                if (!isLocationError()) {
                    if (localValidation()) {

                        String fakeGPSReport = CommonUtil.checkFakeGPS(this, mCurrentCoordinate);
                        if (!TextUtils.isEmpty(fakeGPSReport)) {
                            hideDialog();
                            sendFakeGPSReport(fakeGPSReport, String.valueOf(mExtraSiteId));
                            TowerApplication.getInstance().toast(fakeGPSReport, Toast.LENGTH_LONG);
                            return;
                        }

                        checkIn();

                    } else {
                        if (!isDistanceValid()) TowerApplication.getInstance().toast(getString(R.string.error_distance_too_far), Toast.LENGTH_LONG);
                        else if (!isAccuracyValid()) TowerApplication.getInstance().toast(getString(R.string.error_accuracy_too_low), Toast.LENGTH_LONG);
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
     *  onLocationChanged() --> location change captured
     *  */
    @Override
    protected void onStart() {
        super.onStart();
        requestLocationPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!CommonUtil.checkPlayServices(this)) {
            Toast.makeText(activity, getString(R.string.warning_check_play_service_message), Toast.LENGTH_LONG).show();
            return;
        }

        if (!CommonUtil.checkNetworkStatus(this) || !CommonUtil.checkGpsStatus(this)) {
            DialogUtil.showGPSdialog(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationServices();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        /* location is retrieved every 3 - 5 seconds !*/
        mCurrentCoordinate = location;
        updateDistanceAndAccuracy();
        updateCheckInDataRequirements();
        updateCheckInForm();
        mBtnCheckin.setEnabled(true);
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private void initLocationServices() {
        try {
            DebugLog.d("--> start location service");
            mLocationRequestProvider.connect();
        } catch (SecurityException e) {
            DebugLog.e(e.getMessage(), e);
        }
    }

    private void stopLocationServices() {
        DebugLog.d("--> stop location service");
        mLocationRequestProvider.disconnect();
    }


    private boolean isLocationError() {
        return CommonUtil.isCurrentLocationError(mCurrentCoordinate.getLatitude(), mCurrentCoordinate.getLongitude());
    }

    private boolean isDistanceValid() {
        return mDistanceMeasurment <= MAXIMUM_DISTANCE;
    }

    private boolean isAccuracyValid() {
        return mAccuracy <= MAXIMUM_ACCURACY;
    }

    private boolean localValidation() {
        if (BuildConfig.DEBUG) return true;
        return isDistanceValid() && isAccuracyValid();
    }

    private void showSuccessCheckinMessage() {
        TowerApplication.getInstance().toast(getString(R.string.success_check_in), Toast.LENGTH_SHORT);
    }

    private void showLocationGPSError() {
        TowerApplication.getInstance().toast(this.getResources().getString(R.string.sitelocationisnotaccurate), Toast.LENGTH_LONG);
    }

    private void showPleaseWaitMessage() {
        TowerApplication.getInstance().toast(getString(R.string.error_please_wait_gps), Toast.LENGTH_LONG);
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

    private void initView() {
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
        mErrorCard              = findViewById(R.id.card_error);
        mErrorMessage           = findViewById(R.id.text_error);

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

        String textCriteria = getString(R.string.info_aturan_checkin, MAXIMUM_DISTANCE, MAXIMUM_ACCURACY);
        mCheckinCriteria.setText(textCriteria);
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
        mParamObject = new CheckinDataModel();
        mScheduleData = ScheduleBaseModel.getScheduleById(mExtraScheduleId);

        try {
            if (mScheduleData.site == null) {
                mErrorCard.setVisibility(View.VISIBLE);
                mErrorMessage.setText(R.string.error_site_empty);
                throw new NullPointerException(getString(R.string.error_site_empty));
            }
            if (TextUtils.isEmpty(mScheduleData.site.locationStr)) {
                mErrorCard.setVisibility(View.VISIBLE);
                mErrorMessage.setText(R.string.error_site_location_empty);
                throw new NullPointerException(getString(R.string.error_site_location_empty));
            }

            mSiteCoordinate = new Location(LocationManager.GPS_PROVIDER);
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

        } catch (NullPointerException e) {
            DebugLog.e(e.getMessage(), e);
            TowerApplication.getInstance().toast(e.getMessage(), Toast.LENGTH_LONG);
        }

    }

    private void updateDistanceAndAccuracy() {
        try {
            if (mSiteCoordinate == null) throw new NullPointerException("Site coordinate is null");
            mDistanceMeasurment = mSiteCoordinate.distanceTo(mCurrentCoordinate);
            mAccuracy = mCurrentCoordinate.getAccuracy();
        } catch (NullPointerException e) {
            DebugLog.e(e.getMessage(), e);
        }
    }

    private void updateCheckInDataRequirements() {
        mParamObject.setCurrentLat(String.valueOf(mCurrentCoordinate.getLatitude()));
        mParamObject.setCurrentLong(String.valueOf(mCurrentCoordinate.getLongitude()));
        mParamObject.setDistance(mDistanceMeasurment);
        mParamObject.setTime(DateUtil.now());
        mParamObject.setStatus(localValidation() ? "success" : "failed");
        mParamObject.setAccuracy(mAccuracy);
    }

    private void updateCheckInForm() {
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

    private void sendFakeGPSReport(String message, String siteId) {
        compositeDisposable.add(
                TowerAPIHelper.reportFakeGPS(String.valueOf(System.currentTimeMillis()), BuildConfig.VERSION_NAME, message,siteId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> DebugLog.d("send fake GPS report complete"),
                                error -> DebugLog.e(error.getMessage(), error)
                        )
        );
    }

    private void checkIn() {
        showMessageDialog(getString(R.string.info_sending_check_in_data));
        compositeDisposable.add(
                TowerAPIHelper.checkinSchedule(
                            mExtraScheduleId,
                            String.valueOf(mParamObject.getScheduleId()),
                            mParamObject.getSiteIdCustomer(),
                            mParamObject.getSiteName(),
                            mParamObject.getPeriod(),
                            mParamObject.getSiteLat(),
                            mParamObject.getSiteLong(),
                            mParamObject.getCurrentLat(),
                            mParamObject.getCurrentLong(),
                            String.valueOf(mParamObject.getDistance()),
                            mParamObject.getTime(),
                            mParamObject.getStatus(),
                            String.valueOf(mParamObject.getAccuracy()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    hideDialog();
                                    if (response == null || response.data == null) {
                                        throw new NullPointerException(getString(R.string.error_response_null));
                                    }

                                    if (response.status == HttpURLConnection.HTTP_CREATED) {
                                        showSuccessCheckinMessage();
                                        keepCurrentLocationDataTobeUsed();
                                        navigateToGroupActivity();
                                    } else {
                                        TowerApplication.getInstance().toast(response.messages, Toast.LENGTH_LONG);
                                    }
                                }, error -> {
                                    hideDialog();
                                    String errorMsg = NetworkUtil.handleApiError(error);
                                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                                }
                        )
        );
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
            DebugLog.d("Already have permission, start request location");
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
