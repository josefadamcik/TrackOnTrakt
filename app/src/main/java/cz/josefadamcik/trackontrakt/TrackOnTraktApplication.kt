

package cz.josefadamcik.trackontrakt


import android.app.Application
import android.content.Context
import com.jakewharton.threetenabp.AndroidThreeTen
import cz.josefadamcik.trackontrakt.data.api.ApiModule
import timber.log.Timber

class TrackOnTraktApplication : Application() {
    lateinit var component: ApplicationComponent

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
//        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()

        AndroidThreeTen.init(this)

        component = DaggerApplicationComponent.builder()
                .applicationModule(ApplicationModule(this))
            .apiModule(ApiModule(this))
                .build()
        component.inject(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}

