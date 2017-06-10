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

import cz.josefadamcik.trackontrakt.data.api.model.OauthTokenResponse

class TestTraktAuthTokenHolder() : TraktAuthTokenHolder {
    override val token: String?
        get() = "TEST TOKEN"
    override val tokenInfo: OauthTokenResponse?
        get() = null

    override fun forgetToken() {}

    override fun fillFromResponse(response: OauthTokenResponse) {
    }

    override fun httpAuth(): String {
        return "Authorization: TEST"
    }

    override fun hasToken(): Boolean {
        return true
    }

    override fun expiresSoonerThanDays(days: Int): Boolean {
        return false
    }
}