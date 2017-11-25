
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