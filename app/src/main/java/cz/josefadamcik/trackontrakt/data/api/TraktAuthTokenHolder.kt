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
import timber.log.Timber

/**
 * Singleton holding auth token
 */
class TraktAuthTokenHolder(val preferences: SharedPreferences) {
    companion object {
        const val PREF_KEY_TOKEN = "trakt.oauth.token"
        const val PREF_KEY_RESPONSE = "trakt.oauth.response"
    }

    var token: String? = null

    fun readFromPreferences() {
        token = preferences.getString(PREF_KEY_TOKEN, null)
        Timber.i("Loading auth token %s", token)
    }

    fun httpAuth(): String {
        return String.format("Bearer %s", token)
    }

    fun hasToken(): Boolean {
        return token != null
    }

    fun forgetToken() {
        Timber.i("Forgetting auth token")
        token = null
        preferences.edit()
            .remove(PREF_KEY_RESPONSE)
            .remove(PREF_KEY_TOKEN)
            .apply()
    }
}
