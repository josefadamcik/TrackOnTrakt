package cz.josefadamcik.trackontrakt.testutil

import android.support.test.espresso.IdlingResource
import android.support.test.runner.lifecycle.ActivityLifecycleMonitor
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import android.support.test.runner.lifecycle.Stage


private class WaitActivityIsResumedIdlingResource(private val activityToWaitClassName: String) : IdlingResource {
    private val instance: ActivityLifecycleMonitor = ActivityLifecycleMonitorRegistry.getInstance()
    @Volatile
    private var resourceCallback: IdlingResource.ResourceCallback? = null
    internal var resumed = false

    private val isActivityLaunched: Boolean
        get() {
            val activitiesInStage = instance.getActivitiesInStage(Stage.RESUMED)
            for (activity in activitiesInStage) {
                if (activity.javaClass.name == activityToWaitClassName) {
                    return true
                }
            }
            return false
        }

    override fun getName(): String {
        return this.javaClass.name
    }

    override fun isIdleNow(): Boolean {
        resumed = isActivityLaunched
        if (resumed && resourceCallback != null) {
            resourceCallback!!.onTransitionToIdle()
        }

        return resumed
    }

    override fun registerIdleTransitionCallback(resourceCallback: IdlingResource.ResourceCallback) {
        this.resourceCallback = resourceCallback
    }
}