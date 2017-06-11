/*
 Copyright 2017 Josef Adamcik <josef.adamcik@gmail.com>

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
package cz.josefadamcik.trackontrakt.testutil

import android.app.Activity
import cz.josefadamcik.trackontrakt.ApplicationModule
import cz.josefadamcik.trackontrakt.DaggerTestApplicationComponent
import cz.josefadamcik.trackontrakt.TrackOnTraktApplication
import cz.josefadamcik.trackontrakt.data.api.TestApiModule


public fun initDiWithStubApiModules(app: TrackOnTraktApplication): Unit {
    app.component = DaggerTestApplicationComponent.builder()
        .applicationModule(ApplicationModule(app))
        .testApiModule(TestApiModule(app))
        .build()
}

/**
 * Shortcut for
 * rule = ComponentActivityTestRule(MyActivity::class.java ...)
 */
public inline fun <reified T : Activity> activityTestRule(): ComponentActivityTestRule<T> = ComponentActivityTestRule(T::class.java, ::initDiWithStubApiModules)