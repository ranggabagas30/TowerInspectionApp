package com.sap.inspection.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.telephony.TelephonyManager;

import com.sap.inspection.tools.DebugLog;

import java.io.IOException;
import java.util.Date;

public class ExifUtil {
    public static Bitmap rotateBitmap(String src, Bitmap bitmap) {
        try {
            int orientation = getExifOrientation(src);
            DebugLog.d("orientation="+orientation);
            
            if (orientation == 1) {
                return bitmap;
            }
            
            Matrix matrix = new Matrix();
            switch (orientation) {
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
            }
            
            try {
                Bitmap oriented = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                bitmap.recycle();
                return oriented;
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                return bitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } 
        
        return bitmap;
    }
    
    public static int getExifOrientation(String src) throws IOException {
        int orientation = 1;
        
        try {
            /**
             * if your are targeting only api level >= 5
             * ExifInterface exif = new ExifInterface(src);
             * orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

            if (Build.VERSION.SDK_INT >= 5) {
                Class<?> exifClass = Class.forName("android.media.ExifInterface");
                Constructor<?> exifConstructor = exifClass.getConstructor(new Class[] { String.class });
                Object exifInstance = exifConstructor.newInstance(new Object[] { src });
                Method getAttributeInt = exifClass.getMethod("getAttributeInt", new Class[] { String.class, int.class });
                Field tagOrientationField = exifClass.getField("TAG_ORIENTATION");
                String tagOrientation = (String) tagOrientationField.get(null);
                orientation = (Integer) getAttributeInt.invoke(exifInstance, new Object[] { tagOrientation, 1});
            }*/
            ExifInterface exif = new ExifInterface(src);
            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        
        return orientation;
    }

    /**
     * called when exif data profile is created
     */
    public static void createExifData(Context context, ExifInterface exif, double latitude, double longitude){
        // create a reference for Latitude and Longitude
        double lat = latitude;
        if (lat < 0) {
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "S");
            lat = -lat;
        } else {
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N");
        }

        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE,
                formatLatLongString(lat));

        double lon = longitude;
        if (lon < 0) {
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "W");
            lon = -lon;
        } else {
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E");
        }
        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE,
                formatLatLongString(lon));
        try {
            exif.saveAttributes();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        final String make = android.os.Build.MANUFACTURER; // get the make of the device
        final String model = android.os.Build.MODEL; // get the model of the divice

        exif.setAttribute(ExifInterface.TAG_MAKE, make);

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = telephonyManager.getDeviceId();

        exif.setAttribute(ExifInterface.TAG_MODEL, model+" - "+imei);
        exif.setAttribute(ExifInterface.TAG_DATETIME, (new Date(System.currentTimeMillis())).toString()); // set the date & time

    }

    /**
     * format the Lat Long values according to standard exif format
     */
    private static String formatLatLongString(double d) {
        // format latitude and longitude according to exif format
        StringBuilder b = new StringBuilder();
        b.append((int) d);
        b.append("/1,");
        d = (d - (int) d) * 60;
        b.append((int) d);
        b.append("/1,");
        d = (d - (int) d) * 60000;
        b.append((int) d);
        b.append("/1000");
        return b.toString();
    }
}
