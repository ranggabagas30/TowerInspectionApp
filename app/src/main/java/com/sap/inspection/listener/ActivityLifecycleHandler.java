package com.sap.inspection.listener;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.sap.inspection.TowerApplication;
import com.sap.inspection.tools.DebugLog;

/**
 * A convenience lifecycle handler that tracks whether the overall application is
 * started, in the foreground, in the background or stopped and ignores transitions
 * between individual activities.
 */
public class ActivityLifecycleHandler implements Application.ActivityLifecycleCallbacks {

    private LifecycleListener listener;
    private int started, resumed;
    private boolean transitionPossible;

    public ActivityLifecycleHandler(LifecycleListener listener) {
        this.listener = listener;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        trackThisPage(activity.getLocalClassName() + " created");
    }

    @Override
    public void onActivityStarted(Activity activity) {
        trackThisPage(activity.getLocalClassName() + " started");
        if (started == 0 && listener != null) {
            listener.onApplicationStarted();
        }
        started++;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        trackThisPage(activity.getLocalClassName() + " resumed");
        if (resumed == 0 && !transitionPossible && listener != null) {
            listener.onApplicationResumed();
        }
        transitionPossible = false;
        resumed++;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        trackThisPage(activity.getLocalClassName() + " paused");
        transitionPossible = true;
        resumed--;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        trackThisPage(activity.getLocalClassName() + " stopped");
        if (started == 1 && listener != null) {
            // We only know the application was paused when it's stopped (because transitions always pause activities)
            // http://developer.android.com/guide/components/activities.html#CoordinatingActivities
            if (transitionPossible && resumed == 0)
                listener.onApplicationPaused();
            listener.onApplicationStopped();
        }

        transitionPossible = false;
        started--;
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        trackThisPage(activity.getLocalClassName() + " destroyed");

    }

    private void trackThisPage(String message) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, message);
        FirebaseAnalytics mFirebaseAnalytics = TowerApplication.getInstance().getDefaultAnalytics();
        mFirebaseAnalytics.logEvent("track_page", bundle);
        DebugLog.d(message);
    }

    /** Informs the listener about application lifecycle events. */
    public interface LifecycleListener {
        /** Called right before the application is stopped. */
        void onApplicationStopped();

        /** Called right after the application has been started. */
        void onApplicationStarted();

        /** Called when the application has gone to the background. */
        void onApplicationPaused();

        /** Called right after the application has come to the foreground. */
        void onApplicationResumed();
    }
}
