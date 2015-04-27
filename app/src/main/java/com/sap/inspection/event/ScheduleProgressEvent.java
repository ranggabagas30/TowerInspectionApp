package com.sap.inspection.event;

public class ScheduleProgressEvent extends ProgressEvent{

	
	public ScheduleProgressEvent(int progress) {
		super(progress);
	}
	
	public ScheduleProgressEvent(int progress, boolean done) {
		super(progress,done);
	}
}
