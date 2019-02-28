package com.sap.inspection.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;

import com.sap.inspection.R;
import com.sap.inspection.constant.Constants;

import pub.devrel.easypermissions.EasyPermissions;

public class PermissionUtil {

    public static String READ_PHONE_STATE_PERMISSION = Manifest.permission.READ_PHONE_STATE;
    public static String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;

    public static boolean hasPermission(Context context, String permission) {

        return hasPermission(context, new String[]{permission});
    }

    public static boolean hasPermission(Context context, String ... permissions) {

        return EasyPermissions.hasPermissions(context, permissions);
    }

    public static void requestPermission(Activity activity, String reason, int RC_CODE, String permission) {

        requestPermission(activity, reason, RC_CODE, new String[]{permission});
    }

    public static void requestPermission(Activity activity, String reason, int RC_CODE, String ... permissions) {

        EasyPermissions.requestPermissions(activity, reason, RC_CODE, permissions);
    }
}
