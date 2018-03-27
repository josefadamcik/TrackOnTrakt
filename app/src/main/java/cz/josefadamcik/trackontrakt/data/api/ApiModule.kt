
package cz.josefadamcik.trackontrakt.data.api

import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.Rfc3339DateJsonAdapter
import cz.josefadamcik.trackontrakt.ApplicationScope
import cz.josefadamcik.trackontrakt.BuildConfig
import cz.josefadamcik.trackontrakt.TrackOnTraktApplication
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Named


/**
 * Module for API and networking related dependencies.
 */
@Module
open class ApiModule(private val app: TrackOnTraktApplication) {

    @Provides
    @ApplicationScope
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
            .add(LocalDateTime::class.java, LocalDateTimeJsonAdapter(Rfc3339DateJsonAdapter().nullSafe()).nullSafe())
            .add(LocalDate::class.java, LocalDateJsonAdapter(Rfc3339DateJsonAdapter().nullSafe()).nullSafe())
            .build()
    }

    @Provides
    @ApplicationScope
    fun provideRetrofit(traktApiConfig: TraktApiConfig, @Named("traktokhttp") okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(traktApiConfig.apiBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    @Provides
    @ApplicationScope
    fun provideOkHttpCache(): Cache {
        val cacheSize = 10 * 1024 * 1024 // 10 MiB
        val cache = Cache(File(app.cacheDir, "traktapi"), cacheSize.toLong())
        return cache
    }

    @Provides
    @ApplicationScope
    @Named("traktokhttp")
    fun provideOkHttpForTraktApi(traktApiConfig: TraktApiConfig, cache: Cache): OkHttpClient {
        Timber.d("provideOkHttpForTraktApi ")
        val builder = createOkHttpBuilder(cache, traktApiConfig)

        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor({
                Timber.tag("OkHttpNetwork").d(it)
            })
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(loggingInterceptor)
            builder.addInterceptor(OkHttpCacheLoggingInterceptor())
        }

        val client = builder.build()
        afterOkHttpClientCreated(client)
        return client
    }

    protected open fun afterOkHttpClientCreated(client: OkHttpClient) {
        //nop
    }

    @Provides
    @ApplicationScope
    fun provideTraktApi(retrofit: Retrofit): TraktApi {
        return retrofit.create(TraktApi::class.java)
    }


    @Provides
    @ApplicationScope
    fun provideTraktAuthTokenHolder(preferences: SharedPreferences, moshi: Moshi): TraktAuthTokenHolder {
        return createTraktAuthTokenHolderImpl(preferences, moshi)
    }


    @Provides
    @ApplicationScope
    fun provideTraktAuthTokenProvider(impl: TraktAuthTokenHolder): TraktAuthTokenProvider {
        return impl
    }


    @Provides
    @ApplicationScope
    fun provideTraktApiConfig(): TraktApiConfig {
        return createTraktApiConfig()
    }



    protected open fun createOkHttpBuilder(cache: Cache, traktApiConfig: TraktApiConfig): OkHttpClient.Builder {
        val builder = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .cache(cache)
            .addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .header("User-Agent", String.format("TrackOnTrakt %s", BuildConfig.VERSION_CODE))
                    .addHeader("Content-type", "application/json")
                    .addHeader("trakt-api-key", traktApiConfig.clientId)
                    .addHeader("trakt-api-version", "2")
                    .build()
                chain.proceed(request)
            }
        return builder
    }


    protected open fun createTraktAuthTokenHolderImpl(preferences: SharedPreferences, moshi: Moshi): TraktAuthTokenHolder {
        val instance = TraktAuthTokenHolderImpl(preferences, moshi)
        instance.readFromPreferences()
        return instance
    }

    protected open fun createTraktApiConfig(): TraktApiConfig {
        return TraktApiConfig(
            clientId = BuildConfig.TRAKT_CLIENT_ID,
            clientSecret = BuildConfig.TRAKT_CLIENT_SECRET,
            oauthRedirectUrl = BuildConfig.TRAKT_OAUTH_REDIRECT_URL,
            apiBaseUrl = BuildConfig.TRAKT_BASE_API_URL,
            loginUrl = BuildConfig.TRAKT_LOGIN_URL
        )
    }

}