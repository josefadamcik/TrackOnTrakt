
package cz.josefadamcik.trackontrakt.testutil

import android.app.Activity
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso
import android.support.test.espresso.base.DefaultFailureHandler
import android.support.test.espresso.intent.rule.IntentsTestRule
import com.squareup.spoon.SpoonRule
import cz.josefadamcik.trackontrakt.TrackOnTraktApplication
import org.junit.runner.Description
import org.junit.runners.model.Statement


class ComponentActivityTestRule<T : Activity>(
    activityClass: Class<T>,
    val beforeActivityLaunched: (TrackOnTraktApplication) -> Unit,
    initialTouchMode: Boolean = true,
    launchActivity: Boolean = false
) : IntentsTestRule<T>(activityClass, initialTouchMode, launchActivity) {
    public val spoon = SpoonRule()

    override fun beforeActivityLaunched() {
        super.beforeActivityLaunched()
        beforeActivityLaunched(InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TrackOnTraktApplication)
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

    fun screenshot(s: String) { spoon.screenshot(activity, s) }
}

