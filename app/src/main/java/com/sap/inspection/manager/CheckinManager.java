package com.sap.inspection.manager;

import android.app.AlarmManager;
import android.os.CountDownTimer;

public class CheckinManager {

    private final String TAG = CheckinManager.class.getSimpleName();
    private static final int MAX_TIME = 5; // seconds
    private static int timeToStart;
    private AlarmManager alarmManager;

    public CheckinManager() {

    }
    private enum TimerState {
        STARTED,
        STOPPED
    }
}
