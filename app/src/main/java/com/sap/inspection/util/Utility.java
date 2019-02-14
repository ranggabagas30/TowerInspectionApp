package com.sap.inspection.util;// Created by Arif Ariyan (me@arifariyan.com) on 8/8/16.

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.sap.inspection.MyApplication;
import com.sap.inspection.R;
import com.sap.inspection.model.value.Pair;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.tools.PersistentLocation;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class Utility {
    public static boolean checkGpsStatus(Context context){
        boolean gps_enabled = false;

        LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        /*return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);*/
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {

        }

        return (gps_enabled);
    }

    public static boolean checkNetworkStatus(Context context) {
        boolean network_enabled = false;
        LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {

        }
        return network_enabled;
    }

    /**
     * tambahan Rangga
     *
     * PersistentLocation : menyimpan data lokasi tetap yang
     * didapatkan (realtime gps/network location) saat operator pertama kali melakukan pengambilan
     * foto (Photograph), sehingga dapat digunakan untuk acuan data lokasi pengambilan foto selanjutnya
     * pada scheduleid yang sama
     *
     * */
    public static Pair<String, String> getPhotoLocation(Context context, String scheduleId, LatLng currentLocation) {
        String siteLatitude;
        String siteLongitude;

        //uncomment statement below to delete existed persistent lat lng data in SharedPref
        //PersistentLocation.getInstance().deletePersistentLatLng();
        if (!MyApplication.getInstance().isHashMapInitialized()) {
            // if hashMap had not been initialized yet
            // ... then inizialize it and retreiveMap from sharedPref
            DebugLog.d("hasMap site Location had not been initialized yet");
            MyApplication.getInstance().setHashMapSiteLocation(PersistentLocation.getInstance().retreiveHashMap());
        }

        if (PersistentLocation.getInstance().isScheduleIdPersistentLocationExist(scheduleId)) {
            //if persistent location of scheduleId has been existed
            // ... then assign the pair location value to siteLatitude and siteLongitude
            //MyApplication.getInstance().toast("Persistent lat lng exist", Toast.LENGTH_SHORT);
            siteLatitude = PersistentLocation.getInstance().getPersistent_latitude();
            siteLongitude = PersistentLocation.getInstance().getPersistent_longitude();
            DebugLog.d("Use saved persistent site location with loc : " + siteLatitude + "," + siteLongitude);
        } else {
            //else if not
            // ... then assign current geo point to site location. photoItem.setImage() will insert
            //these new site location to local sqlite database
            //MyApplication.getInstance().toast("Persistent lat lng doesn't exist", Toast.LENGTH_SHORT);
            siteLatitude = String.valueOf(currentLocation.latitude);
            siteLongitude = String.valueOf(currentLocation.longitude);
            DebugLog.d( "location from current geo points : " + siteLatitude + " , " + siteLongitude);

            if (isCurrentLocationError(siteLatitude, siteLongitude)) {
                //if acquired new location (lat , lng) >= (1.0 , 1.0)
                //... then save schedule persistent site location in pref
                PersistentLocation.getInstance().setPersistent_latitude(siteLatitude);
                PersistentLocation.getInstance().setPersistent_longitude(siteLongitude);
                PersistentLocation.getInstance().savePersistentLatLng(scheduleId);
            } else {
                MyApplication.getInstance().toast(context.getResources().getString(R.string.sitelocationisnotaccurate), Toast.LENGTH_LONG);
            }
        }
        return new Pair<>(siteLatitude, siteLongitude);
    }

    public static void setPersistentLocation(String scheduleId, String latitude, String longitude) {
        if (!MyApplication.getInstance().isHashMapInitialized()) {
            // if hashMap had not been initialized yet
            // ... then inizialize it and retreiveMap from sharedPref
            DebugLog.d("hasMap site Location had not been initialized yet");
            MyApplication.getInstance().setHashMapSiteLocation(PersistentLocation.getInstance().retreiveHashMap());
        }

        PersistentLocation.getInstance().setPersistent_latitude(latitude);
        PersistentLocation.getInstance().setPersistent_longitude(longitude);
        PersistentLocation.getInstance().savePersistentLatLng(scheduleId);
    }

    public static Pair<String, String> getPersistentLocation(String scheduleId) {
        String persistent_latitude;
        String persistent_longitude;

        if (!MyApplication.getInstance().isHashMapInitialized()) {
            // if hashMap had not been initialized yet
            // ... then inizialize it and retreiveMap from sharedPref
            DebugLog.d("hasMap site Location had not been initialized yet");
            MyApplication.getInstance().setHashMapSiteLocation(PersistentLocation.getInstance().retreiveHashMap());
        }

        if (PersistentLocation.getInstance().isScheduleIdPersistentLocationExist(scheduleId)) {
            //if persistent location of scheduleId has been existed
            // ... then assign the pair location value to persistent_latitude and persistent_longitude
            //MyApplication.getInstance().toast("Persistent lat lng exist", Toast.LENGTH_SHORT);
            persistent_latitude = PersistentLocation.getInstance().getPersistent_latitude();
            persistent_longitude = PersistentLocation.getInstance().getPersistent_longitude();
            DebugLog.d("Use saved persistent location with loc : ( " + persistent_latitude + "," + persistent_longitude + " ) ");
            return new Pair<>(persistent_latitude, persistent_longitude);
        }
        return null;
    }

    public static boolean isCurrentLocationError(String latitude, String longitude) {
        double dLat = Math.abs(Double.parseDouble(latitude));
        double dLng = Math.abs(Double.parseDouble(longitude));

        return (dLat == 0.0 && dLng == 0.0);
    }

    public static boolean isCurrentLocationError(double latitude, double longitude) {
        double dLat = Math.abs(latitude);
        double dLng = Math.abs(longitude);

        return (dLat == 0.0 && dLng == 0.0);
    }

    /**
     *
     * STORAGE UTILITY
     *
     * */

    public static boolean isReadWriteStoragePermissionGranted(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // rangga
            // see : https://stackoverflow.com/questions/47787577/unable-to-save-image-file-in-android-oreo-update-how-to-do-it?rq=1

            if (ContextCompat.checkSelfPermission(MyApplication.getContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MyApplication.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                DebugLog.v("on SDK > 26, READ and WRITE Permission is granted");
                return true;

            } else {

                DebugLog.v("Permission is revoked");
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            DebugLog.v(" on SDK < 23, READ and WRITE Permission is automatically granted");
            return true;
        }
    }

    public static boolean isExternalStorageReadOnly() {
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState());
    }

    public static boolean isExternalStorageAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }


    private static final Pattern DIR_SEPARATOR = Pattern.compile("/");

    /**
     * Returns all available SD-Cards in the system (include emulated)
     * <p/>
     * Warning: Hack! Based on Android source code of version 4.3 (API 18)
     * Because there is no standard way to get it.
     * Edited by hendrawd
     *
     * @return paths to all available SD-Cards in the system (include emulated)
     */
    public static String[] getStorageDirectories(Context context) {
        // Final set of paths
        final Set<String> rv = new HashSet<>();
        // Primary physical SD-CARD (not emulated)
        final String rawExternalStorage = System.getenv("EXTERNAL_STORAGE");
        // All Secondary SD-CARDs (all exclude primary) separated by ":"
        final String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
        // Primary emulated SD-CARD
        final String rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
        if (TextUtils.isEmpty(rawEmulatedStorageTarget)) {
            //fix of empty raw emulated storage on marshmallow
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                File[] files = context.getExternalFilesDirs(null);
                for (File file : files) {
                    if (file == null) continue;
                    String applicationSpecificAbsolutePath = file.getAbsolutePath();
                    String emulatedRootPath = applicationSpecificAbsolutePath.substring(0, applicationSpecificAbsolutePath.indexOf("Android/data"));
                    rv.add(emulatedRootPath);
                }
            } else {
                // Device has physical external storage; use plain paths.
                if (TextUtils.isEmpty(rawExternalStorage)) {
                    // EXTERNAL_STORAGE undefined; falling back to default.
                    rv.addAll(Arrays.asList(getPhysicalPaths()));
                } else {
                    rv.add(rawExternalStorage);
                }
            }
        } else {
            // Device has emulated storage; external storage paths should have
            // userId burned into them.
            final String rawUserId;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                rawUserId = "";
            } else {
                final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                final String[] folders = DIR_SEPARATOR.split(path);
                final String lastFolder = folders[folders.length - 1];
                boolean isDigit = false;
                try {
                    Integer.valueOf(lastFolder);
                    isDigit = true;
                } catch (NumberFormatException ignored) {
                }
                rawUserId = isDigit ? lastFolder : "";
            }
            // /storage/emulated/0[1,2,...]
            if (TextUtils.isEmpty(rawUserId)) {
                rv.add(rawEmulatedStorageTarget);
            } else {
                rv.add(rawEmulatedStorageTarget + File.separator + rawUserId);
            }
        }
        // Add all secondary storages
        if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
            // All Secondary SD-CARDs splited into array
            final String[] rawSecondaryStorages = rawSecondaryStoragesStr.split(File.pathSeparator);
            Collections.addAll(rv, rawSecondaryStorages);
        }
        return rv.toArray(new String[rv.size()]);
    }

    /**
     * @return physicalPaths based on phone model
     */
    private static String[] getPhysicalPaths() {
        return new String[]{
                "/storage/sdcard0",
                "/storage/sdcard1",                 //Motorola Xoom
                "/storage/extsdcard",               //Samsung SGS3
                "/storage/sdcard0/external_sdcard", //User request
                "/mnt/extsdcard",
                "/mnt/sdcard/external_sd",          //Samsung galaxy family
                "/mnt/external_sd",
                "/mnt/media_rw/sdcard1",            //4.4.2 on CyanogenMod S3
                "/removable/microsd",               //Asus transformer prime
                "/mnt/emmc",
                "/storage/external_SD",             //LG
                "/storage/ext_sd",                  //HTC One Max
                "/storage/removable/sdcard1",       //Sony Xperia Z1
                "/data/sdext",
                "/data/sdext2",
                "/data/sdext3",
                "/data/sdext4",
                "/sdcard1",                         //Sony Xperia Z
                "/sdcard2",                         //HTC One M8s
                "/storage/microsd",                  //ASUS ZenFone 2
                "/storage/MicroSD"                  // ASUS ZenFone 5
        };
    }
}
