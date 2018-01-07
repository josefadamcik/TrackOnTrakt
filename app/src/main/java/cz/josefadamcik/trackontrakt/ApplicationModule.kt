
package cz.josefadamcik.trackontrakt

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import cz.josefadamcik.trackontrakt.util.AndroidUriQueryParamParser
import cz.josefadamcik.trackontrakt.util.CurrentTimeProvider
import cz.josefadamcik.trackontrakt.util.CurrentTimeProviderImpl
import cz.josefadamcik.trackontrakt.util.UriQueryParamParser
import dagger.Module
import dagger.Provides

@Module
open class ApplicationModule(private val app: TrackOnTraktApplication) {

    @Provides
    @ApplicationScope
    fun provideApplication(): Application {
        return app
    }

    @Provides
    @ApplicationScope
    fun provideSharedPreferences(): SharedPreferences {
        return app.getSharedPreferences("trackontrackt", Context.MODE_PRIVATE);
    }

    @Provides
    @ApplicationScope
    fun provideCurrentTimeProvider(): CurrentTimeProvider = createCurrentTimeProvider()

    @Provides
    @ApplicationScope
    fun provideUriQueryParamParser(): UriQueryParamParser = AndroidUriQueryParamParser()

    open protected fun createCurrentTimeProvider(): CurrentTimeProvider = CurrentTimeProviderImpl()
}
