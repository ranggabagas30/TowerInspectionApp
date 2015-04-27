package com.rindang.zconfig;

import android.view.View;


public class ProdMobileConfig extends BaseConfig{

	public static final String SENDER_ID = "65449620027";
	
	@Override
	public String getHost() {
		return "http://mobile-api.sekap.net";
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
