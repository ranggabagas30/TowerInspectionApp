package com.sap.inspection.event;

public class UploadProgressEvent extends ProgressEvent{

	public UploadProgressEvent(String progress, boolean done) {
		super(progress, done);
	}

	public UploadProgressEvent(String progress) {
		super(progress);
	}
	
}
