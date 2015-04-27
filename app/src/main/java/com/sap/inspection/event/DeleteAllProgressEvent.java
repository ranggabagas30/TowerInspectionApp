package com.sap.inspection.event;

public class DeleteAllProgressEvent extends ProgressEvent{

	public DeleteAllProgressEvent(String progress, boolean done) {
		super(progress, done);
	}

	public DeleteAllProgressEvent(String progress) {
		super(progress);
	}
	
	

}
