package com.rindang.zconfig;

import com.sap.inspection.BuildConfig;

public class AppConfig{

	private static AppConfig instance;
	public BaseConfig config;
	
	private AppConfig() {
//      config = new WawanConfig();
// 		config = new ProdMobileConfig();

		if (BuildConfig.DEBUG) {
			config = new DevConfig();
		} else {
			config = new ProdConfig();
		}
	}

	public static AppConfig getInstance(){
		if (instance == null)
			instance = new AppConfig();
		return instance;
	}

	public String getV1(){
		return config.getHost()+"/v1";
	}
}
