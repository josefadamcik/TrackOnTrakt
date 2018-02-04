
package cz.josefadamcik.trackontrakt.testutil

import android.app.Activity
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso
import android.support.test.espresso.base.DefaultFailureHandler
import android.support.test.espresso.intent.rule.IntentsTestRule
import com.squareup.spoon.Spoon
import cz.josefadamcik.trackontrakt.TrackOnTraktApplication
import org.junit.runner.Description
import org.junit.runners.model.Statement


class ComponentActivityTestRule<T : Activity>(
    activityClass: Class<T>,
    val beforeActivityLaunched: (TrackOnTraktApplication) -> Unit,
    initialTouchMode: Boolean = true,
    launchActivity: Boolean = false
) : IntentsTestRule<T>(activityClass, initialTouchMode, launchActivity) {

    override fun beforeActivityLaunched() {
        super.beforeActivityLaunched()
        beforeActivityLaunched(InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TrackOnTraktApplication)
    }

    override fun apply(base: Statement, description: Description): Statement {
        val testClassName = description.className
        val testMethodName = description.methodName
        val context = InstrumentationRegistry.getTargetContext()
        Espresso.setFailureHandler { throwable, matcher ->
            Spoon.screenshot(activity, "Failure", testClassName, testMethodName);
            DefaultFailureHandler(context).handle(throwable, matcher)
        }
        return super.apply(base, description)
    }
}

