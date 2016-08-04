package com.rindang.zconfig;

import android.view.View;

import com.sap.inspection.R;
import com.sap.inspection.tools.PrefUtil;


public class DevConfig extends BaseConfig{

	private final String SENDER_ID = "934425217631";
	
	@Override
	public String getHost() {
		return PrefUtil.getStringPref(R.string.url_endpoint, host);
	}

	@Override
	public void setHost(String host) {
		PrefUtil.putStringPref(R.string.url_endpoint, host);
		this.host = host;
	}

	@Override
	public String getGCMSenderId() {
		return SENDER_ID;
	}

	@Override
	public int getURLChangingVisibility() {
		return View.VISIBLE;
	}

	@Override
	public boolean isProduction() {
		return false;
	}

}
