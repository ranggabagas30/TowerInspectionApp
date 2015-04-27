package com.sap.inspection.listener;


public interface UploadListener {
	public void onUpdate(String status);
	public void onFailed();
	public void onSuccess();
}
