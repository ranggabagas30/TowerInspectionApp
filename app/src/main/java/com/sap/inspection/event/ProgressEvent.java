package com.sap.inspection.event;

public class ProgressEvent {

	public int progress;
	public String progressString;
	public boolean done = false;
	
	public ProgressEvent(int progress) {
		this.progress = progress;
	}
	
	public ProgressEvent(String progress) {
		this.progressString = progress;
	}
	
	public ProgressEvent(int progress, boolean done) {
		this.progress = progress;
		this.done = done;
	}
	
	public ProgressEvent(String progress, boolean done) {
		this.progressString = progress;
		this.done = done;
	}
}
