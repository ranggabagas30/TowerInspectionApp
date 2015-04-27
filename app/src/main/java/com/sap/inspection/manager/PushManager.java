package com.sap.inspection.manager;

import java.util.Vector;

import com.sap.inspection.listener.PushListener;

//import android.util.Log;

public class PushManager {
	public static final int UNHANDLED_EXEPTION = -1;
	public static final int TIMEOUT_EXCEPTION = 1; 
	public Vector<PushListener> pushListeners;
	private PushListener notificationFragmentPush;
	
	private static PushManager instance;
	private PushManager() {
    }
	 
	public void setNotificationFragmentPush(
			PushListener notificationFragmentPush) {
		this.notificationFragmentPush = notificationFragmentPush;
	}
	
	public static PushManager getInstance() {
        if (instance == null) {
            instance = new PushManager();
        }
        return instance;
    }
	
	public void addPushListener(PushListener pushListener){
		if (this.pushListeners == null)
			this.pushListeners = new Vector<PushListener>();
		this.pushListeners.add(pushListener);
	}
	
	public void removePushListener(){
		if (this.pushListeners != null)
			this.pushListeners.clear();
	}
	
	public void push(){
		if (pushListeners != null)
			for (PushListener pushListener : pushListeners) {
				pushListener.onPush();
			}
		if (notificationFragmentPush != null)
			notificationFragmentPush.onPush();
	}

}
