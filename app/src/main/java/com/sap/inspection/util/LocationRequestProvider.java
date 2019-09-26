package com.sap.inspection.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.sap.inspection.TowerApplication;
import com.sap.inspection.R;
import com.sap.inspection.tools.DebugLog;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

public class LocationRequestProvider implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        com.google.android.gms.location.LocationListener{

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private Context mContext;

    public interface LocationCallback {
        void handleNewLocation(Location location);
    }

    public static String TAG = LocationRequestProvider.class.getSimpleName();
    private static int RC_CONNECTION_RESOLUTION = 999;

    public LocationRequestProvider(Context context, LocationCallback locationCallback) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();

        mLocationCallback = locationCallback;

        mLocationRequest = new LocationRequest().create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5 * 1000)
                .setFastestInterval(3 * 1000);
        mContext = context;
    }

    public void connect() {
        mGoogleApiClient.connect();
    }

    public void disconnect() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    public void showGPSDialog() {
        DialogUtil.showGPSdialog(mContext);
    }

    @Override
    public void onConnected(Bundle bundle) {
        DebugLog.d("location onConnected");
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        DebugLog.d("location onConnectionSuspended : " + i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution() && mContext instanceof Activity) {
            try {
                Activity activity = (Activity) mContext;
                connectionResult.startResolutionForResult(activity, RC_CONNECTION_RESOLUTION);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }

        } else {
            TowerApplication.getInstance().toast("Location connection failed with message : \n" + connectionResult.getErrorCode(), Toast.LENGTH_LONG);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        DebugLog.d("onLocationChanged : (" + location.getLatitude() + " , " + location.getLongitude() + ")");
        mLocationCallback.handleNewLocation(location);
    }
}
