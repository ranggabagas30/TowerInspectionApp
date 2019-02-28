/*
 * CommonUtils.java
 * 
 * Copyright (C) 2013 6 Wunderkinder GmbH.
 * 
 * @author      Jose L Ugia - @Jl_Ugia
 * @version     1.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.slidinglayer.util;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.sap.inspection.MyApplication;
import com.sap.inspection.R;
import com.sap.inspection.tools.DebugLog;

import java.io.File;
import java.util.Random;

/**
 * Common Utils for Android.
 * 
 */
public class CommonUtils {

    private static Random mRandom;

    /**
     * Get a random boolean
     */
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

    public static void fixVersion(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String versionPref = prefs.getString(context.getString(R.string.latest_version), "");
        ContextWrapper contextWrapper = (ContextWrapper)context;
        try {
            String versionApp = context.getPackageManager().getPackageInfo(contextWrapper.getPackageName(), 0).versionName;
            DebugLog.d("(latest version) versionPref="+versionPref+" versionApp="+versionApp);
            if (!versionPref.isEmpty()) {
                versionPref = versionPref.replace(".","");
                int versionPrefInt = Integer.parseInt(versionPref);
                String ver = versionApp.replace(".","");
                int versionAppInt = Integer.parseInt(ver);
                DebugLog.d("versionPrefInt="+versionPrefInt+" verApp="+versionAppInt);
                if (versionAppInt>versionPrefInt) {
                    DebugLog.d("app>pref, fix pref version!!");
                    prefs.edit().putString(context.getString(R.string.latest_version), versionApp).commit();
                }
            } else {
                prefs.edit().putString(context.getString(R.string.latest_version), versionApp).commit();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static boolean isUpdateAvailable(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String versionPref = prefs.getString(context.getString(R.string.latest_version), "");
        ContextWrapper contextWrapper = (ContextWrapper)context;
        try {
            String versionApp = context.getPackageManager().getPackageInfo(contextWrapper.getPackageName(), 0).versionName;
            DebugLog.d("versionPref="+versionPref+" versionApp="+versionApp);
            if (!versionPref.isEmpty()) {
                versionPref = versionPref.replace(".","");
                int versionPrefInt = Integer.parseInt(versionPref);
                String ver = versionApp.replace(".","");
                int versionAppInt = Integer.parseInt(ver);
                DebugLog.d("versionPrefInt="+versionPrefInt+" verApp="+versionAppInt);
                if (versionAppInt<versionPrefInt) {
                    DebugLog.d("update available!!");
                    return true;
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }


}