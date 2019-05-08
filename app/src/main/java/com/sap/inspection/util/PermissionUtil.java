package com.sap.inspection.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;

import com.sap.inspection.R;
import com.sap.inspection.constant.Constants;

import java.util.ArrayList;

import pub.devrel.easypermissions.EasyPermissions;

public class PermissionUtil {

    public static String READ_PHONE_STATE_PERMISSION = Manifest.permission.READ_PHONE_STATE;
    public static String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static String ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static String CAMERA = Manifest.permission.CAMERA;
    public static String[] permissions = new String[] { READ_PHONE_STATE_PERMISSION, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE, ACCESS_FINE_LOCATION, CAMERA };

    public static boolean hasAllPermissions(Context context) {

        return hasPermission(context, permissions);

    }

    public static boolean hasPermission(Context context, String permission) {

        return hasPermission(context, new String[]{permission});
    }

    public static boolean hasPermission(Context context, String ... permissions) {

        return EasyPermissions.hasPermissions(context, permissions);
    }

    public static void requestAllPermissions(Activity activity, String reason, int RC_CODE) {

        requestPermission(activity, reason, RC_CODE, permissions);

    }

    public static void requestPermission(Activity activity, String reason, int RC_CODE, String permission) {

        requestPermission(activity, reason, RC_CODE, new String[]{permission});
    }

    public static void requestPermission(Activity activity, String reason, int RC_CODE, String ... permissions) {

        EasyPermissions.requestPermissions(activity, reason, RC_CODE, permissions);
    }
}
