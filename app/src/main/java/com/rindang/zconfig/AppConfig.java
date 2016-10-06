package com.rindang.zconfig;

public class AppConfig{

	private static AppConfig instance;
	public BaseConfig config;
	
	private AppConfig() {
//        config = new WawanConfig();
		config = new ProdConfig();
//		config = new ProdMobileConfig();
//		config = new DevConfig();
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
