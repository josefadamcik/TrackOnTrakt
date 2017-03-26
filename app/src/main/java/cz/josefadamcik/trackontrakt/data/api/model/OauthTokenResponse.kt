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
package cz.josefadamcik.trackontrakt.data.api.model

import java.util.*

data class OauthTokenResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Long,
    val refresh_token: String,
    val scope: String,
    val created_at: Long
) {
    val expired: Boolean
        get() {
            val now = Date()
            return now.time > created_at * 1000 + expires_in * 1000
        }

    val createdBefore: Long
        get() {
            return Date().time - created_at * 1000
        }

    val expiresAt: Date
        get() = Date((created_at + expires_in) * 1000)

    fun expiresSoonerThanDays(days: Int): Boolean {
        return expiresAt.before(Date(Date().time + days * 24 * 60 * 60 * 1000))
    }
}