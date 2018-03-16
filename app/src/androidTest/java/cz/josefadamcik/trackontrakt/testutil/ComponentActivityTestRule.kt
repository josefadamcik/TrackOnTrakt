
package cz.josefadamcik.trackontrakt.testutil

import android.app.Activity
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso
import android.support.test.espresso.base.DefaultFailureHandler
import android.support.test.espresso.intent.Intents
import android.support.test.rule.ActivityTestRule
import com.squareup.spoon.SpoonRule
import cz.josefadamcik.trackontrakt.TrackOnTraktApplication
import org.junit.runner.Description
import org.junit.runners.model.Statement


class ComponentActivityTestRule<T : Activity>(
        activityClass: Class<T>,
        private val beforeActivityLaunched: (TrackOnTraktApplication) -> Unit,
        initialTouchMode: Boolean = true,
        launchActivity: Boolean = false
) : ActivityTestRule<T>(activityClass, initialTouchMode, launchActivity) {
    private val spoon = SpoonRule()
    private var intentsInitialized = false

    override fun beforeActivityLaunched() {
        super.beforeActivityLaunched()
        beforeActivityLaunched(InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TrackOnTraktApplication)
        Intents.init() //similat to IntentsTestRule, but initialization is done before the activity is launched so we can catch intens executed during start.
        intentsInitialized = true
    }

    override fun apply(base: Statement, description: Description): Statement {
        spoon.apply(base, description)
        val testClassName = description.className
        val testMethodName = description.methodName
        val context = InstrumentationRegistry.getTargetContext()
        Espresso.setFailureHandler { throwable, matcher ->
            spoon.screenshot(activity, "failure");
            DefaultFailureHandler(context).handle(throwable, matcher)
        }

        return super.apply(base, description)
    }

    override fun afterActivityLaunched() {
        super.afterActivityLaunched()
        spoon.screenshot(activity, "launched")

    }

    override fun afterActivityFinished() {
        super.afterActivityFinished()
        if (intentsInitialized) {
            // Otherwise will throw a NPE if Intents.init() wasn't called.
            Intents.release()
            intentsInitialized = false
        }
    }

    fun screenshot(s: String) { spoon.screenshot(activity, s) }
}

