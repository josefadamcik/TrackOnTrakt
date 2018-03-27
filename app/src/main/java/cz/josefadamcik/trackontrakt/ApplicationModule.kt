
package cz.josefadamcik.trackontrakt

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import cz.josefadamcik.trackontrakt.util.*
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

@Module
open class ApplicationModule(
        private val app: TrackOnTraktApplication) {

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

    @Provides
    @ApplicationScope
    fun provideApiRxSchedulers(): RxSchedulers {
        return RxSchedulers(Schedulers.io(), AndroidSchedulers.mainThread())
    }


    open protected fun createCurrentTimeProvider(): CurrentTimeProvider = CurrentTimeProviderImpl()
}
