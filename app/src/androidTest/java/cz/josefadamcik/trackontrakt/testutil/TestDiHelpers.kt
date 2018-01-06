
package cz.josefadamcik.trackontrakt.testutil

import android.app.Activity
import cz.josefadamcik.trackontrakt.DaggerTestApplicationComponent
import cz.josefadamcik.trackontrakt.TestApplicationModule
import cz.josefadamcik.trackontrakt.TrackOnTraktApplication
import cz.josefadamcik.trackontrakt.data.api.TestApiModule


public fun initDiWithStubApiModules(app: TrackOnTraktApplication): Unit {
    app.component = DaggerTestApplicationComponent.builder()
        .testApplicationModule(TestApplicationModule(app))
        .testApiModule(TestApiModule(app))
        .build()
}

/**
 * Shortcut for
 * rule = ComponentActivityTestRule(MyActivity::class.java ...)
 */
public inline fun <reified T : Activity> activityTestRule(): ComponentActivityTestRule<T> = ComponentActivityTestRule(T::class.java, ::initDiWithStubApiModules)