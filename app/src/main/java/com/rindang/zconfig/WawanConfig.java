package com.rindang.zconfig;

import android.view.View;


public class WawanConfig extends BaseConfig{

	public static final String SENDER_ID = "65449620027";
	
	@Override
	public String getHost() {
		return "http://192.168.88.48";
	}

	@Override
	public void setHost(String host) {
	}
	
	@Override
	public String getGCMSenderId() {
		return SENDER_ID;
	}

	@Override
	public int getURLChangingVisibility() {
		return View.GONE;
	}

	@Override
	public boolean isProduction() {
		return true;
	}

}
