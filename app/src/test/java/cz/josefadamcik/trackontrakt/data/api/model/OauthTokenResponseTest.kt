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
import org.hamcrest.Matchers.*
import org.junit.Test

import java.util.*

class OauthTokenResponseTest {

    @Test
    fun notExpiredToken() {
        val created_at: Long  = Date().time - 2785
        val value = withTokenCreatedBefore(created_at)

        assertThat(value.expired, equalTo(false))
    }


    @Test
    fun expiredToken() {
        val created_at: Long  = Date().time - 3985
        val value = withTokenCreatedBefore(created_at)

        assertThat(value.expired, equalTo(true))
    }




    private fun withTokenCreatedBefore(createdAt: Long, expires_in: Long = 3600): OauthTokenResponse {
        val value = OauthTokenResponse("", "", expires_in, "", "", createdAt)
        return value
    }


}