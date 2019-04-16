package com.sap.inspection.util;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.ScheduleGeneral;
import com.sap.inspection.tools.DebugLog;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

public class StringUtil {

    public static byte[] readFile(Context context, String filename) {

        byte[] buffer = null;
        InputStream is;

        try {
            is = context.getAssets().open(filename);
            int size = is.available();
            buffer = new byte[size];
            is.read(buffer);
            is.close();
            return buffer;
        } catch (IOException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

        return null;
    }

    public static String ConvertInputStreamToString(InputStream is) {
        String str = null;
        byte[] b = null;
        try {
            StringBuffer buffer = new StringBuffer();
            b = new byte[4096];
            for (int n; (n = is.read(b)) != -1;) {
                buffer.append(new String(b, 0, n));
                DebugLog.d("== (b:" + b + ";b.length:" + b.length + ";n:" + n );
            }
            str = buffer.toString();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return str;
    }

    // check if the content type is json
    public static boolean checkIfContentTypeJson(String contentType){
        int idxSemiColon = contentType.indexOf(Constants.JSON_CONTENT_TYPE);
        DebugLog.d(contentType + " | " + idxSemiColon);
        return idxSemiColon != -1;
    }

    public static boolean isPreventive(String scheduleId) {

        ScheduleBaseModel scheduleBaseModel = new ScheduleGeneral();
        scheduleBaseModel = scheduleBaseModel.getScheduleById(scheduleId);

        String workTypeName = scheduleBaseModel.work_type.name;
        return workTypeName.matches(Constants.regexPREVENTIVE);
    }

    public static int getWargaKeIndex(String wargaKeText) {

        String pattern = "Warga Ke-";
        wargaKeText = wargaKeText.replace(pattern, "");

        return Integer.valueOf(wargaKeText);
    }

    public static String getWargaIdFromLabel(String label) {

        String pattern = Constants.regexWargaId;
        label = label.replace(pattern, "");
        DebugLog.d("wargaid : " + label);

        return label;
    }

    public static String getBarangIdFromLabel(String label) {

        if (label.contains(Constants.regexBarangId)) {
            String pattern = Constants.regexBarangId;
            label = label.replace(pattern, "");
            DebugLog.d("barangid : " + label);
            return label;
        }
        return null;
    }

    public static boolean isNotRegistered(String label) {

        return label != null && label.contains("new") && !label.equalsIgnoreCase(Constants.EMPTY); // i.e. wargaid = "new1", then it's considered as not registered yet

    }
}
