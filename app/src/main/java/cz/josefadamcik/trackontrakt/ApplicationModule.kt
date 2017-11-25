
package cz.josefadamcik.trackontrakt

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides

@Module
class ApplicationModule(private val app: TrackOnTraktApplication) {

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


}
