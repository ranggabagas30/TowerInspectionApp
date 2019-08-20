package com.sap.inspection.util;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.rindang.zconfig.AppConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

public class BundleToJson {

	@SuppressLint("NewApi")
	public static String convert(Bundle bundle){
		if (AppConfig.getInstance().config.isProduction()) return "json.toString() on convert bundletojson";
		else{
			JSONObject json = new JSONObject();
			Set<String> keys = bundle.keySet();
			for (String key : keys) {
				try {
					// json.put(key, bundle.get(key)); see edit below
					json.put(key, JSONObject.wrap(bundle.get(key)));
				} catch(JSONException e) {
					//Handle exception here
				}
			}
			return json.toString();
		}
	}
}
