
package cz.josefadamcik.trackontrakt.testutil

import android.app.Activity
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.intent.rule.IntentsTestRule
import cz.josefadamcik.trackontrakt.TrackOnTraktApplication

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
}

