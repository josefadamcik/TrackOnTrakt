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
package cz.josefadamcik.trackontrakt.data

import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import cz.josefadamcik.trackontrakt.BuildConfig
import cz.josefadamcik.trackontrakt.data.api.TraktApi
import cz.josefadamcik.trackontrakt.data.api.TraktApiConfig
import cz.josefadamcik.trackontrakt.data.api.TraktAuthTokenHolder
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import javax.inject.Named
import javax.inject.Singleton


/**
 * Module for API and networking related dependencies.
 */
@Module
class ApiModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(traktApiConfig: TraktApiConfig, @Named("traktokhttp") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(traktApiConfig.apiBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }


    @Provides
    @Singleton
    @Named("traktokhttp")
    fun provideOkHttpForTraktApi(traktApiConfig: TraktApiConfig): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .addHeader("Content-type", "application/json")
                    .addHeader("trakt-api-key", traktApiConfig.clientId)
                    .addHeader("trakt-api-version", "2")
                    .build()
                chain.proceed(request)
            }

        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor({
                Timber.tag("OkHttp").d(it);
            })
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(loggingInterceptor)
        }

        return builder.build();
    }

    @Provides
    @Singleton
    fun provideTraktApi(retrofit: Retrofit): TraktApi {
        return retrofit.create(TraktApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTraktAuthTokenHolder(preferences: SharedPreferences): TraktAuthTokenHolder {
        val instance = TraktAuthTokenHolder(preferences)
        instance.readFromPreferences()
        return instance
    }


    @Provides
    @Singleton
    fun provideTraktApiConfig(): TraktApiConfig {
        return TraktApiConfig(
            clientId = BuildConfig.TRAKT_CLIENT_ID,
            clientSecret = BuildConfig.TRAKT_CLIENT_SECRET,
            oauthRedirectUrl = BuildConfig.TRAKT_OAUTH_REDIRECT_URL,
            apiBaseUrl = BuildConfig.TRAKT_BASE_API_URL
        )
    }

}