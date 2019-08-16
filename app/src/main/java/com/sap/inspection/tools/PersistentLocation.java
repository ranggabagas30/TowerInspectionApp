package com.sap.inspection.tools;

import android.widget.Toast;

import com.sap.inspection.view.ui.MyApplication;
import com.sap.inspection.R;
import com.sap.inspection.util.PrefUtil;

import java.util.AbstractMap;
import java.util.HashMap;

/**
 * Created by domikado on 12/18/17.
 */

public class PersistentLocation{
    private static PersistentLocation mInstance = null;
    private static AbstractMap.SimpleEntry<String, String> persistentLatLng;
    private static HashMap< String, AbstractMap.SimpleEntry<String, String> > persistentSitelocation;
    private String persistent_latitude;
    private String persistent_longitude;

    private PersistentLocation() {

    }

    public static PersistentLocation getInstance() {
        if (mInstance == null) {
            mInstance = new PersistentLocation();
        }
        return mInstance;
    }

    public void setPersistent_latitude(String persistent_latitude) {
        this.persistent_latitude = persistent_latitude;
    }

    public String getPersistent_latitude() {
        return persistent_latitude;
    }

    public void setPersistent_longitude(String persistent_longitude) {
        this.persistent_longitude = persistent_longitude;
    }

    public String getPersistent_longitude() {
        return persistent_longitude;
    }

    public HashMap< String, AbstractMap.SimpleEntry<String, String> > retreiveHashMap() {
        if (persistentSitelocation == null) {
            persistentSitelocation = new HashMap<>();
        }
        String stringSavedFromPref =  PrefUtil.getStringPref(R.string.keypersistentsitelocation, "");
        DebugLog.d("stringSavedFromPref : " + stringSavedFromPref);
        if (!stringSavedFromPref.equalsIgnoreCase("")){
            if (stringSavedFromPref.length() > Integer.MAX_VALUE) {
                MyApplication.getInstance().toast("ImbasPetirData yang tersimpan penuh. Silahkan muat ulang dan hapus jadwal", Toast.LENGTH_LONG);
                return null;
            } else  {
                stringSavedFromPref = stringWithoutColons(stringSavedFromPref);
                String[] listLocationData = stringSavedFromPref.split(",");
                for (String locationData : listLocationData) {

                    DebugLog.d("locationData : " + locationData);

                    String[] keyValue = locationData.split("=");

                    String scheduleId = keyValue[0];
                    setPersistent_latitude(keyValue[1]);
                    setPersistent_longitude(keyValue[2]);
                    DebugLog.d("scheduleId :  " + scheduleId + ", lat : " + getPersistent_latitude() + ", lng : " + getPersistent_longitude());

                    persistentLatLng = new AbstractMap.SimpleEntry<>(getPersistent_latitude(), getPersistent_longitude());
                    persistentSitelocation.put(keyValue[0], persistentLatLng);

                    DebugLog.d("save to hashmap = " + persistentSitelocation.get(keyValue[0]));
                }
            }
        }
        return persistentSitelocation;
    }

    public void savePersistentLatLng(String scheduleId) {
        String stringHashMap;
        persistentLatLng = new AbstractMap.SimpleEntry<>(getPersistent_latitude(), getPersistent_longitude());

        MyApplication.getInstance().getHashMapSiteLocation().put(scheduleId, persistentLatLng);
        stringHashMap = stringWithoutColons(MyApplication.getInstance().getHashMapSiteLocation().toString());
        DebugLog.d("savePersistentLatLng, stringHashMap : " + stringHashMap);
        PrefUtil.putStringPref(R.string.keypersistentsitelocation, stringHashMap);
    }

    public void deletePersistentLatLng() {
        String dummyDelete = "";
        PrefUtil.putStringPref(R.string.keypersistentsitelocation, dummyDelete);
        DebugLog.d("pref location is deleted!");
    }

    public boolean isScheduleIdPersistentLocationExist(String scheduleId) {
        //retreiveHashMap();
        boolean result =  MyApplication.getInstance().getHashMapSiteLocation().containsKey(scheduleId);
        DebugLog.d("result = " + result);
        return result;
    }
    
    private String stringWithoutColons(String source) {
        String result;
        StringBuffer buffer = new StringBuffer();
        for (int b = 0; b < source.length(); b++) {
            char c = source.charAt(b);
            if (c != '{' && c != '}')
                buffer.append(c);
        }
        result = buffer.toString();
        return result;
    }
}
