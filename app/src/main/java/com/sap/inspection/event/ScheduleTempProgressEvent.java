package com.sap.inspection.event;

public class ScheduleTempProgressEvent extends ProgressEvent{


	public ScheduleTempProgressEvent(int progress) {
		super(progress);
	}

	public ScheduleTempProgressEvent(int progress, boolean done) {
		super(progress,done);
	}
}
