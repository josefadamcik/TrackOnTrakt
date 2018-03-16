
package cz.josefadamcik.trackontrakt

import cz.josefadamcik.trackontrakt.data.api.ApiModule
import cz.josefadamcik.trackontrakt.detail.MediaDetailActivity
import cz.josefadamcik.trackontrakt.home.HomeActivity
import cz.josefadamcik.trackontrakt.search.SearchResultsActivity
import cz.josefadamcik.trackontrakt.traktauth.AuthorizationProvider
import cz.josefadamcik.trackontrakt.traktauth.TraktAuthActivity
import cz.josefadamcik.trackontrakt.welcome.WelcomeActivity
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
    fun inject(activity: WelcomeActivity)
}


