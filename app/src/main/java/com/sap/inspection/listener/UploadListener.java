package com.sap.inspection.listener;


public interface UploadListener {
	void onUpdate(String status);
	void onFailed();
	void onSuccess();
}
