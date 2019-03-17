package com.sap.inspection.util;

import android.content.Context;

import com.crashlytics.android.Crashlytics;

import java.io.IOException;
import java.io.InputStream;

public class StringUtil {

    public static byte[] getContentFromFile(Context context, String filename) {

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
}
