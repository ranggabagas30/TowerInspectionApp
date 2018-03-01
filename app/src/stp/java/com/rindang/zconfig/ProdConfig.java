package com.rindang.zconfig;

import android.view.View;


public class ProdConfig extends BaseConfig{

	public static final String SENDER_ID = "934425217631";
	
	@Override
	public String getHost() {
		//return "http://103.17.55.74";
		return "http://pmt-api.domikado.com";
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
