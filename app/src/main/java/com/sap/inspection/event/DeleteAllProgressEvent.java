package com.sap.inspection.event;

public class DeleteAllProgressEvent extends ProgressEvent{

	public boolean shouldRelogin;

	public DeleteAllProgressEvent(String progress, boolean done, boolean shouldRelogin) {
		super(progress, done);
		this.shouldRelogin = shouldRelogin;
	}

	public DeleteAllProgressEvent(String progress) {
		super(progress);
	}
	
	

}
