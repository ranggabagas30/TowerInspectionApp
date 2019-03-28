package com.sap.inspection.util;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.sap.inspection.constant.Constants;
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

    public static int getWargaKeIndex(String wargaKeText) {

        String pattern = "Warga Ke-";
        wargaKeText = wargaKeText.replace(pattern, "");

        return Integer.valueOf(wargaKeText);
    }
}
