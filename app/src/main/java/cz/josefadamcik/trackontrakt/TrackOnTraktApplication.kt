

package cz.josefadamcik.trackontrakt


import android.app.Application
import com.crashlytics.android.Crashlytics
import com.jakewharton.threetenabp.AndroidThreeTen
import cz.josefadamcik.trackontrakt.data.api.ApiModule
import io.fabric.sdk.android.Fabric
import timber.log.Timber

class TrackOnTraktApplication : Application() {
    lateinit var component: ApplicationComponent

    override fun onCreate() {
        super.onCreate()

        AndroidThreeTen.init(this)
        Fabric.with(this, Crashlytics())

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

