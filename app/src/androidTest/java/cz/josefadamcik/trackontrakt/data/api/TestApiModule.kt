
package cz.josefadamcik.trackontrakt.data.api

import android.content.SharedPreferences
import android.support.test.espresso.IdlingRegistry
import com.jakewharton.espresso.OkHttp3IdlingResource
import com.squareup.moshi.Moshi
import cz.josefadamcik.trackontrakt.BuildConfig
import cz.josefadamcik.trackontrakt.TrackOnTraktApplication
import dagger.Module
import okhttp3.Cache
import okhttp3.OkHttpClient

@Module
class TestApiModule(private val app: TrackOnTraktApplication) : ApiModule(app) {

    override fun createOkHttpBuilder(cache: Cache, traktApiConfig: TraktApiConfig): OkHttpClient.Builder {
        return OkHttpClient.Builder()
    }

    override fun createTraktApiConfig(): TraktApiConfig {
        return TraktApiConfig("test", "test", "test", "http://127.0.0.1:" + BuildConfig.MOCKSERVER_PORT)
    }


    override fun createTraktAuthTokenHolderImpl(preferences: SharedPreferences, moshi: Moshi): TraktAuthTokenHolder {
        return TestTraktAuthTokenHolder()
    }

    override fun afterOkHttpClientCreated(client: OkHttpClient) {
        super.afterOkHttpClientCreated(client)
        IdlingRegistry.getInstance().register(OkHttp3IdlingResource.create("okhttp", client))
    }
}