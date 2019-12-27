package com.sap.inspection.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.RequiresPermission;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.sap.inspection.TowerApplication;
import com.sap.inspection.tools.DebugLog;

public class LocationRequestProvider implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        com.google.android.gms.location.LocationListener{

    private static final int LOCATION_REQUEST_INTERVAL = 5 * 1000;
    private static final int LOCATION_REQUEST_FASTEST_INTERVAL = 3 * 1000;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private Context mContext;

    public interface LocationCallback {
        void onLocationChanged(Location location);
        void onConnected(Bundle bundle);
        void onConnectionSuspended(int i);
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
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(LOCATION_REQUEST_INTERVAL)
                .setFastestInterval(LOCATION_REQUEST_FASTEST_INTERVAL);
        mContext = context;
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public void connect() {
        if (mGoogleApiClient != null) mGoogleApiClient.connect();
    }

    public void disconnect() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        DebugLog.d("location onConnected");
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        if (mLocationCallback != null) mLocationCallback.onConnected(bundle);
    }

    /**
     * onConnectionSuspended gets called when your app gets disconnected
     * from the Google Play services package (not necessarily the Internet).
     * The callback gets invoked for instance when you go to Settings > Apps > Google Play services > Force Stop.
     * Another example is when you would uninstall Google Play services.
     * You would get onConnectionSuspended followed by onConnectionFailed after a couple of seconds (because a reconnection attempt would fail).
     8 Also do not call mGoogleApiClient.connect() from onConnectionSuspended(...). Reconnection is handled automatically.
     * */
    @Override
    public void onConnectionSuspended(int i) {
        DebugLog.d("location onConnectionSuspended : " + i);
        if (mLocationCallback != null) mLocationCallback.onConnectionSuspended(i);
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
        DebugLog.d("location (" + location.getLatitude() + ", " + location.getLongitude() + ")");
        if (mLocationCallback != null) mLocationCallback.onLocationChanged(location);
    }
}
