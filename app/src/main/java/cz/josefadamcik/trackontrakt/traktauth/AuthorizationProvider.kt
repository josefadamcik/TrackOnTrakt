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


import android.net.Uri
import cz.josefadamcik.trackontrakt.ApplicationScope
import cz.josefadamcik.trackontrakt.BuildConfig
import cz.josefadamcik.trackontrakt.data.api.TraktApi
import cz.josefadamcik.trackontrakt.data.api.TraktApiConfig
import cz.josefadamcik.trackontrakt.data.api.TraktAuthTokenHolder
import cz.josefadamcik.trackontrakt.data.api.model.OauthTokenRequest
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

@ApplicationScope
class AuthorizationProvider
@Inject
constructor(
    val traktApi: TraktApi,
    val traktApiConfig: TraktApiConfig,
    val traktAuthTokenHolder: TraktAuthTokenHolder
) {


    fun getOauthAuthorizationUrl(): String {
        val oauthUrl = String.format(
            BuildConfig.TRAKT_LOGIN_URL,
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
            OauthTokenRequest(code,
                traktApiConfig.clientId,
                traktApiConfig.clientSecret,
                traktApiConfig.oauthRedirectUrl)
        )
            .subscribeOn(Schedulers.io())
            .flatMap { res ->
                if (res.isSuccessful) {
                    val response = res.body()
                    Timber.d("obtained access_token %s; response %s", response.access_token, response)
                    traktAuthTokenHolder.fillFromResponse(response)
                    Single.just(TraktAuthorisationResult(true, traktAuthTokenHolder.token))
                } else {
                    Timber.w("failed %s : %s", res.code(), res.message())
                    Single.just(TraktAuthorisationResult(false, null))
                }

            }
            .observeOn(AndroidSchedulers.mainThread())


    }

    fun shouldHandleRedirectUrl(url: String): Boolean {
        return url.startsWith(BuildConfig.TRAKT_OAUTH_REDIRECT_URL)
    }
}
