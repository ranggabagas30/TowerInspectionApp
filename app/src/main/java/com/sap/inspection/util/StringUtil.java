package com.sap.inspection.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.ScheduleGeneral;
import com.sap.inspection.model.config.formimbaspetir.FormImbasPetirConfig;
import com.sap.inspection.model.form.RowColumnModel;
import com.sap.inspection.model.value.FormValueModel;
import com.sap.inspection.tools.DebugLog;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;

public class StringUtil {

    /* Returns true if url is valid */
    public static boolean isValid(String url)
    {
        /* Try creating a valid URL */
        try {
            new URL(url).toURI();
            return true;
        }

        // If there was an Exception
        // while creating URL object
        catch (Exception e) {
            return false;
        }
    }

    public static String getNewPhotoFileName(String scheduleId, int itemId) {
        return "picture-" + scheduleId + "-" + itemId + "-" + Calendar.getInstance().getTimeInMillis()+"-";
    }

    public static String getVersionName(Context context){
        String version = null;
        try {
            version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            DebugLog.d(version);
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return version;
    }

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

    /**
     *
     * Form Imbas Petir Utils
     *
     * */
    public static int getWargaKeIndex(String wargaKeText) {

        String pattern = "Warga Ke-";
        wargaKeText = wargaKeText.replace(pattern, "");

        return Integer.valueOf(wargaKeText);
    }

    public static String getIdFromLabel(String label) {

        String patternId = Constants.regexId;
        String patternName = "\\s*\\([A-Za-z]+\\)";

        String[] wargaLabel = label.split("\\s+");

        label = wargaLabel[0].replace(patternId, "");
        DebugLog.d("Id : " + label);

        return label;
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

    public static String getRegisteredWargaId(String scheduleId, String wargaId) {

        if (isNotNullAndNotEmpty(wargaId)) {

            if (isNotRegistered(wargaId)) {
                String realwargaId  = FormImbasPetirConfig.getRegisteredWargaId(scheduleId, wargaId);
                DebugLog.d("(wargaid, realwargaid) : (" + wargaId + "," + realwargaId +")");
                return realwargaId;
            }
            return wargaId;
        }

        return null;
    }

    public static String getRegisteredBarangId(String scheduleId, String wargaId, String barangId) {

        if (isNotNullAndNotEmpty(wargaId) && isNotNullAndNotEmpty(barangId)) {

            if (isNotRegistered(barangId)) {
                String realbarangid  = FormImbasPetirConfig.getRegisteredBarangId(scheduleId, wargaId, barangId);
                DebugLog.d("(barangid, realbarangid) : (" + barangId + "," + realbarangid +")");
                return realbarangid;
            }
            return barangId;
        }

        return null;
    }

    public static String getName(String scheduleId, String wargaId, String barangId, int workFormGroupId) {

        // on sap database get rowcol model using inner join
        RowColumnModel rowColumnWarga = RowColumnModel.getRowColumnItem(workFormGroupId, "Nama");

        if (rowColumnWarga != null) {

            if (!TextUtils.isEmpty(wargaId) && !TextUtils.isEmpty(barangId)) {

                wargaId = getRegisteredWargaId(scheduleId, wargaId);

                if (!barangId.equalsIgnoreCase(Constants.EMPTY)) {

                    barangId = getRegisteredBarangId(scheduleId, wargaId, barangId);

                }

                FormValueModel itemInformasiDiri = FormValueModel.getItemValue(scheduleId, rowColumnWarga.row_id, wargaId, barangId);
                if (itemInformasiDiri != null) {

                    DebugLog.d("full name : " + itemInformasiDiri.value);
                    String[] names = itemInformasiDiri.value.split("\\s+");
                    return names[0];
                }
            }
        }

        return "";
    }

    public static String getIdWithName(String scheduleId, String rowLabel, int work_form_group_id) {

        String wargaId = StringUtil.getIdFromLabel(rowLabel);

        wargaId = getRegisteredWargaId(scheduleId, wargaId);
        String wargaName = StringUtil.getName(scheduleId, wargaId, Constants.EMPTY, work_form_group_id);

        StringBuilder wargaLabelBuilder = new StringBuilder(Constants.regexId).append(wargaId);
        if (!TextUtils.isEmpty(wargaName)) {
            wargaLabelBuilder.append(" (").append(wargaName).append(")");
        }

        return new String(wargaLabelBuilder);
    }

    public static boolean isNotNullAndNotEmpty(String id) {

        if (!TextUtils.isEmpty(id))
            return !id.equalsIgnoreCase(Constants.EMPTY);

        return false;
    }


    public static boolean isNotRegistered(String id) {
        return id.contains("new") && !id.equalsIgnoreCase(Constants.EMPTY);
    }
}
