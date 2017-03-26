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

package cz.josefadamcik.trackontrakt.data.api

import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import cz.josefadamcik.trackontrakt.data.api.model.OauthTokenResponse
import timber.log.Timber
import java.util.*

/**
 * Singleton holding auth token
 */
class TraktAuthTokenHolder(
    private val preferences: SharedPreferences,
    private val moshi: Moshi
) {
    companion object {
        const val PREF_KEY_RESPONSE = "trakt.oauth.response"
    }

    private var oauthTokenResponse: OauthTokenResponse? = null

    val token get() = oauthTokenResponse?.access_token
    val tokenInfo get() = oauthTokenResponse


    fun readFromPreferences() {
        val serialized = preferences.getString(PREF_KEY_RESPONSE, null)
        if (serialized != null) {
            oauthTokenResponse = moshi.adapter(OauthTokenResponse::class.java).fromJson(serialized)
        }
        Timber.i("Loading oauth token response %s", oauthTokenResponse)
        logOauthResponse(oauthTokenResponse)
    }

    private fun logOauthResponse(response: OauthTokenResponse?) {
        if (response != null) {
            Timber.i("created %s expired (%s at %s) issued before %s min (%s h)",
                Date(response.created_at * 1000),
                response.expired,
                response.expiresAt,
                response.createdBefore / 60,
                response.createdBefore / 3600
            )

        }
    }

    fun httpAuth(): String {
        return String.format("Bearer %s", token)
    }

    fun hasToken(): Boolean {
        return token != null
    }

    fun forgetToken() {
        Timber.i("Forgetting auth token")
        oauthTokenResponse = null
        preferences.edit()
            .remove(PREF_KEY_RESPONSE)
            .apply()
    }




    fun fillFromResponse(response: OauthTokenResponse) {
        oauthTokenResponse = response
        val json = moshi.adapter(OauthTokenResponse::class.java).toJson(response)

        preferences.edit()
            .putString(TraktAuthTokenHolder.PREF_KEY_RESPONSE, json)
            .apply()

    }

    fun expiresSoonerThanDays(days: Int): Boolean {
        return tokenInfo?.expiresSoonerThanDays(days) ?: false
    }
}
