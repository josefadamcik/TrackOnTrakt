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
package cz.josefadamcik.trackontrakt

import cz.josefadamcik.trackontrakt.data.api.ApiModule
import cz.josefadamcik.trackontrakt.detail.MediaDetailActivity
import cz.josefadamcik.trackontrakt.home.HomeActivity
import cz.josefadamcik.trackontrakt.search.SearchResultsActivity
import cz.josefadamcik.trackontrakt.traktauth.AuthorizationProvider
import cz.josefadamcik.trackontrakt.traktauth.TraktAuthActivity
import dagger.Component

@ApplicationScope
@Component(modules = arrayOf(ApplicationModule::class, ApiModule::class))
interface ApplicationComponent {
    fun inject(authorizationProvider: AuthorizationProvider)
    fun inject(application: TrackOnTraktApplication)
    fun inject(activity: HomeActivity)
    fun inject(activity: TraktAuthActivity)
    fun inject(activity: SearchResultsActivity)
    fun inject(activity: MediaDetailActivity)
}


