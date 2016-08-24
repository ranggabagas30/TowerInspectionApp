package com.rindang.zconfig;

public abstract class BaseConfig {
	
	protected String host = "http://192.168.120.170:9292";

	private static BaseConfig getConstructor() {
		return new DevConfig();
	}
	
	public String getHost(){
		return host;
	}
	
	public abstract void setHost(String host);
	
	public abstract String getGCMSenderId();
	
	public abstract int getURLChangingVisibility();
	
	public abstract boolean isProduction();
	
}