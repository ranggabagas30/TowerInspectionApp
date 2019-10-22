package com.sap.inspection.util;// Created by Arif Ariyan (me@arifariyan.com) on 8/8/16.

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.maps.model.LatLng;
import com.sap.inspection.BuildConfig;
import com.sap.inspection.R;
import com.sap.inspection.connection.APIHelper;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.model.value.Pair;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.tools.PersistentLocation;
import com.sap.inspection.view.ui.MyApplication;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import javax.crypto.Cipher;

public class CommonUtil {

    public static boolean checkGpsStatus(Context context){
        boolean gps_enabled = false;

        LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        /*return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);*/
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
            Crashlytics.logException(ex);
        }

        return (gps_enabled);
    }

    public static boolean checkNetworkStatus(Context context) {
        boolean network_enabled = false;

        LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
            Crashlytics.logException(ex);
        }
        return network_enabled;
    }

    public static boolean checkFakeGPSAvailable(Context context, Location location, String siteId, Handler handler) {
        if (CommonUtil.isMockLocationOn(location)) {
            String message = "App is using fake gps";
            sendReportFakeGPS(context, message, siteId, handler);
            return true;
        } else {
            List<String> listOfFakeGPSApp = CommonUtil.getListOfFakeLocationAppsFromAll(context);
            if (!listOfFakeGPSApp.isEmpty()) {
                String message = "Fake gps application(s) found : \n";
                message += listOfFakeGPSApp.toString();
                sendReportFakeGPS(context, message, siteId, handler);
                return true;
            }
        }
        return false;
    }

    public static void sendReportFakeGPS(Context context, String message, String siteId, Handler handler) {
        String timeDetected = CommonUtil.toDate(System.currentTimeMillis(), Constants.DATETIME_PATTERN2);
        String appVersion = BuildConfig.VERSION_NAME;
        APIHelper.reportFakeGPS(context, handler, timeDetected, appVersion, message, siteId);
    }

    public static boolean isMockLocationOn(Location location) {
        return location.isFromMockProvider();
    }

    public static List<String> getListOfFakeLocationAppsFromAll(Context context) {
        List<String> fakeApps = new ArrayList<>();
        List<ApplicationInfo> packages = context.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo aPackage : packages) {
            boolean isSystemPackage = ((aPackage.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
            if(!isSystemPackage && hasAppPermission(context, aPackage.packageName, "android.permission.ACCESS_MOCK_LOCATION")){
                fakeApps.add(aPackage.packageName);
            }
        }
        return fakeApps;
    }

    public static boolean hasAppPermission(Context context, String app, String permission){
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        try {
            packageInfo = packageManager.getPackageInfo(app, PackageManager.GET_PERMISSIONS);
            if(packageInfo.requestedPermissions!= null){
                for (String requestedPermission : packageInfo.requestedPermissions) {
                    if (requestedPermission.equals(permission)) {
                        return true;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getApplicationName(Context context, String packageName) {
        String appName = packageName;
        PackageManager packageManager = context.getPackageManager();
        try {
            appName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appName;
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

    /**
     * get DeviceID (IMEI)
     * and installing APK programmatically
     * */
    public static String getIMEI(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = telephonyManager.getDeviceId();
        DebugLog.d("imei : " + imei);
        return imei;
    }

    public static File getNewAPKpath() {

        File tempFile;
        if (CommonUtil.isExternalStorageAvailable()) {
            DebugLog.d("external storage available");
            tempFile = new File(Constants.PATH_APK);
            DebugLog.d(tempFile.getAbsolutePath());
            if (tempFile.exists()) {
                return tempFile;
            }
        }
        return null;
    }

    public static void installAPK(Activity activity, Context context) {

        File tempFile = getNewAPKpath();
        if (tempFile != null && tempFile.exists()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri uriAPK = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", tempFile);
                Intent intent = new Intent(Intent.ACTION_VIEW).setDataAndType(uriAPK, "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                activity.startActivityForResult(intent, Constants.RC_INSTALL_APK);
            } else {
                Intent intent = new Intent(Intent.ACTION_VIEW).setDataAndType(Uri.fromFile(tempFile), "application/vnd.android.package-archive");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivityForResult(intent, Constants.RC_INSTALL_APK);
            }
        } else {
            MyApplication.getInstance().toast(context.getResources().getString(R.string.error_apk_not_found), Toast.LENGTH_LONG);
            activity.finish();
        }
    }


    /**
     * Get a random boolean
     */

    private static Random mRandom;
    public static boolean getNextRandomBoolean() {

        if (mRandom == null) {
            mRandom = new Random();
        }

        return mRandom.nextBoolean();
    }

    public static boolean isAlphanumeric(String val) {

        final String regex = "[a-zA-Z0-9]+";
        return val.matches(regex);
    }

    public static boolean isNumeric(String val) {

        final String regex = "[0-9]+";
        return val.matches(regex);
    }

    public static void clearApplicationData() {
        File cache = MyApplication.getContext().getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                }
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    public static boolean isUpdateAvailable(Context context) {

        boolean isUpdateAvailable = false;
        String latestVersion = PrefUtil.getStringPref(R.string.latest_version, "");
        String appVersion = Constants.APPLICATION_VERSION;
        DebugLog.d("latestVersion\t: " + latestVersion);
        DebugLog.d("appVersion\t\t: " + appVersion);
        if (!TextUtils.isEmpty(latestVersion)) {

            latestVersion = latestVersion.replace(".","");
            int latestVersionInt = Integer.parseInt(latestVersion);

            appVersion = appVersion.replace(".","");
            int appVersionInt = Integer.parseInt(appVersion);

            if (appVersionInt > latestVersionInt) {
                StringBuilder message = new StringBuilder(context.getString(R.string.error_failed_check_apk_version));
                message.append(".").append("App version (").append(appVersion).append(") is newer than the server's (").append(latestVersion).append(")");
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            } else if (appVersionInt < latestVersionInt) {
                isUpdateAvailable = true;
            } else {
                Toast.makeText(context, context.getString(R.string.success_latest_apk), Toast.LENGTH_SHORT).show();
            }
        }
        return isUpdateAvailable;
    }

    /**
     *
     * encryption
     *
     * */
    public static String getEncryptedMD5Hex(String source) {
        return new String(Hex.encodeHex(DigestUtils.md5(source)));
    }

    private static byte[] getFile(File f) {

        //File f = new File(filePath);
        InputStream is = null;
        try {
            is = new FileInputStream(f);
        } catch (FileNotFoundException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        byte[] content = null;
        try {
            content = new byte[is.available()];
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            if (content != null) {
                is.read(content);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return content;
    }

    private static byte[] encryptPdfFile(Key key, byte[] content) {
        Cipher cipher;
        byte[] encrypted = null;
        try {
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            encrypted = cipher.doFinal(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encrypted;

    }

    private static byte[] decryptPdfFile(Key key, byte[] textCryp) {
        Cipher cipher;
        byte[] decrypted = null;
        try {
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            decrypted = cipher.doFinal(textCryp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return decrypted;
    }

    private static void saveFile(byte[] fileBytes, String filePathOutput) throws IOException {

        FileOutputStream fos = new FileOutputStream(filePathOutput);
        fos.write(fileBytes);
        fos.close();

    }

    public static void encryptFileBase64(File file, String fileOutput) {

        DebugLog.d("encrypt file");
        try {
            byte[] fileBytes = FileUtils.readFileToByteArray(file);
            byte[] encryptedBytes = new Base64().encode(fileBytes);
            FileUtils.writeByteArrayToFile(new File(fileOutput), encryptedBytes);
            //saveFile(encryptedBytes, fileOutput);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void decryptFileBase64(File file, String fileOutput) {

        DebugLog.d("decrypt file");
        try {
            byte[] fileBytes = FileUtils.readFileToByteArray(file);
            byte[] decryptedBytes = new Base64().decode(fileBytes);
            FileUtils.writeByteArrayToFile(new File(fileOutput), decryptedBytes);
            //saveFile(decryptedBytes, fileSource);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] getDecryptedByteBase64(File file) {

        DebugLog.d("get decrypted bytes base64");

        try {
            byte[] fileBytes = FileUtils.readFileToByteArray(file);
            return new Base64().decode(fileBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Time Util
     * */
    public static String toDate(long timeInMillis, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Date date = new Date(timeInMillis);
        return sdf.format(date);
    }
}
