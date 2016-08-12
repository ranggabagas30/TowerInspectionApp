package com.sap.inspection.util;// Created by Arif Ariyan (me@arifariyan.com) on 8/8/16.

import android.content.Context;
import android.location.LocationManager;

public class Utility {
    public static boolean checkGpsStatus(Context context){
        LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
}
