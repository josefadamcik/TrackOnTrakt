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

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import java.util.*

class OauthTokenResponseTest {

    @Test
    fun notExpiredToken() {
        val created_at: Long = Date().time / 1000 - 2785
        val value = withTokenCreatedAt(created_at)

        assertThat(value.expired, equalTo(false))
    }


    @Test
    fun expiredToken() {
        val createdAt: Long = Date().time / 1000 - 3985
        val value = withTokenCreatedAt(createdAt)

        assertThat(value.expired, equalTo(true))
    }

    @Test
    fun expiresSoonerThan() {
        val createdAt: Long = Date().time / 1000 - 35 * 24 * 60 * 60
        val value = withTokenCreatedAt(createdAt)

        assertThat(value.expiresSoonerThanDays(31), equalTo(true))
    }

    @Test
    fun doesNotExpireSoonerThan() {
        val createdAt: Long = Date().time / 1000 - 29 * 24 * 60 * 60
        val value = withTokenCreatedAt(createdAt)

        assertThat(value.expiresSoonerThanDays(31), equalTo(true))
    }


    private fun withTokenCreatedAt(createdAt: Long, expiresIn: Long = 3600): OauthTokenResponse {
        val value = OauthTokenResponse("", "", expiresIn, "", "", createdAt)
        return value
    }


}