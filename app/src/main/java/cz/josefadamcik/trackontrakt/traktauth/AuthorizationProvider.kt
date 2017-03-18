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
import cz.josefadamcik.trackontrakt.data.api.OauthTokenRequest
import cz.josefadamcik.trackontrakt.data.api.OauthTokenResponse
import cz.josefadamcik.trackontrakt.data.api.TraktApi
import cz.josefadamcik.trackontrakt.data.api.TraktApiConfig
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
    val moshi: Moshi
) {
    private val prefKeyResponse = "trakt.oauth.response"
    private val prefKeyAuthToken = "trakt.oauth.token"


    fun getOauthAuthorizationUrl(): String {
        val oauthUrl = String.format("https://trakt.tv/oauth/authorize?response_type=code&client_id=%s&redirect_uri=%s",
            BuildConfig.TRAKT_CLIENT_ID,
            BuildConfig.TRAKT_OAUTH_REDIRECT_URL
        )
        return oauthUrl;
    }

    fun onTraktAuthRedirect(url: String) {
        val uri = Uri.parse(url)
        Timber.i("Parsed uri %s", uri);
        val code = uri.getQueryParameter("code")
        if (code != null) {
            Timber.d("auth code: %s", code)
        }

        traktApi.oauthToken(
            OauthTokenRequest(code, traktApiConfig.clientId, traktApiConfig.clientSecret, traktApiConfig.oauthRedirectUrl)
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { res ->
                    if (res.isSuccessful) {
                        val response = res.body()
                        Timber.d("obtained access_token %s; response %s", response.access_token, response)
                        val json = moshi.adapter(OauthTokenResponse::class.java).toJson(response);
                        preferences.edit()
                            .putString(prefKeyAuthToken, response.access_token)
                            .putString(prefKeyResponse, json)
                            .apply()

                    } else {
                        Timber.w("failed %s : %s", res.code(), res.message())
                    }
                },
                { t -> Timber.e(t, "Failed") }
            )


    }
}
