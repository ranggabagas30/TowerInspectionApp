package com.sap.inspection.util;

import android.content.Context;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.ScheduleGeneral;
import com.sap.inspection.model.config.formimbaspetir.FormImbasPetirConfig;
import com.sap.inspection.model.form.RowColumnModel;
import com.sap.inspection.model.form.WorkFormItemModel;
import com.sap.inspection.model.value.ItemValueModel;
import com.sap.inspection.tools.DebugLog;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

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

        if (!TextUtils.isEmpty(wargaId)) {

            if (StringUtil.isNotRegistered(wargaId)) {
                String realwargaId  = FormImbasPetirConfig.getRegisteredWargaId(scheduleId, wargaId);
                DebugLog.d("(wargaid, realwargaid) : (" + wargaId + "," + realwargaId +")");
                return realwargaId;
            }
            return wargaId;
        }

        return null;
    }

    public static String getRegisteredBarangId(String scheduleId, String wargaId, String barangId) {

        if (!TextUtils.isEmpty(barangId) && !TextUtils.isEmpty(wargaId)) {

            if (StringUtil.isNotRegistered(barangId)) {
                String realbarangid  = FormImbasPetirConfig.getRegisteredBarangId(scheduleId, wargaId, barangId);
                DebugLog.d("(barangid, realbarangid) : (" + barangId + "," + realbarangid +")");
                return realbarangid;
            }
            return barangId;
        }

        return null;
    }

    public static String getName(String scheduleId, String wargaId, String barangId, int workFormGroupId, String lable) {

        // on sap database get rowcol model using inner join
        RowColumnModel rowColumnWarga = RowColumnModel.getRowColumnItem(workFormGroupId, lable);

        if (rowColumnWarga != null) {

            // on value database
            String realWargaId = getRegisteredWargaId(scheduleId, wargaId);
            String realBarangId = getRegisteredBarangId(scheduleId, wargaId, barangId);

            if (!TextUtils.isEmpty(realWargaId) && !TextUtils.isEmpty(realBarangId)) {

                ItemValueModel itemInformasiDiri = ItemValueModel.getItemValue(scheduleId, rowColumnWarga.row_id, realWargaId, realBarangId);
                if (itemInformasiDiri != null) {

                    DebugLog.d("full name : " + itemInformasiDiri.value);
                    String[] names = itemInformasiDiri.value.split("\\s+");
                    return names[0];
                }
            }
        }

        return "";
    }


    public static boolean isNotNullAndEmpty(String id) {

        if (!TextUtils.isEmpty(id))
            return !id.equalsIgnoreCase(Constants.EMPTY);

        return false;
    }


    public static boolean isNotRegistered(String id) {
        return id.contains("new") && !id.equalsIgnoreCase(Constants.EMPTY);
    }


}
