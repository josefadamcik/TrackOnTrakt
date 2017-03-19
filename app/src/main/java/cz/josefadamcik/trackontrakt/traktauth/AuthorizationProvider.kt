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

package cz.josefadamcik.trackontrakt.traktauth


import android.content.SharedPreferences
import android.net.Uri
import com.squareup.moshi.Moshi
import cz.josefadamcik.trackontrakt.BuildConfig
import cz.josefadamcik.trackontrakt.data.api.TraktApi
import cz.josefadamcik.trackontrakt.data.api.TraktApiConfig
import cz.josefadamcik.trackontrakt.data.api.TraktAuthTokenHolder
import cz.josefadamcik.trackontrakt.data.api.model.OauthTokenRequest
import cz.josefadamcik.trackontrakt.data.api.model.OauthTokenResponse
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthorizationProvider
@Inject
constructor(
    val traktApi: TraktApi,
    val preferences: SharedPreferences,
    val traktApiConfig: TraktApiConfig,
    val traktAuthTokenHolder: TraktAuthTokenHolder,
    val moshi: Moshi
) {


    fun getOauthAuthorizationUrl(): String {
        val oauthUrl = String.format("https://trakt.tv/oauth/authorize?response_type=code&client_id=%s&redirect_uri=%s",
            BuildConfig.TRAKT_CLIENT_ID,
            BuildConfig.TRAKT_OAUTH_REDIRECT_URL
        )
        return oauthUrl;
    }

    fun onTraktAuthRedirect(url: String): Single<TraktAuthorisationResult> {
        val uri = Uri.parse(url)
        Timber.i("Parsed uri %s", uri);
        val code = uri.getQueryParameter("code")
        if (code != null) {
            Timber.d("auth code: %s", code)
        }

        return traktApi.oauthToken(
            OauthTokenRequest(code, traktApiConfig.clientId, traktApiConfig.clientSecret, traktApiConfig.oauthRedirectUrl)
        )
            .subscribeOn(Schedulers.io())
            .flatMap { res ->
                if (res.isSuccessful) {
                    val response = res.body()
                    Timber.d("obtained access_token %s; response %s", response.access_token, response)
                    val json = moshi.adapter(OauthTokenResponse::class.java).toJson(response)

                    traktAuthTokenHolder.token = response.access_token

                    preferences.edit()
                        .putString(TraktAuthTokenHolder.PREF_KEY_TOKEN, response.access_token)
                        .putString(TraktAuthTokenHolder.PREF_KEY_RESPONSE, json)
                        .apply()

                    Single.just(TraktAuthorisationResult(true, response.access_token))
                } else {
                    Timber.w("failed %s : %s", res.code(), res.message())
                    Single.just(TraktAuthorisationResult(false, null))
                }

            }
            .observeOn(AndroidSchedulers.mainThread())


    }
}
