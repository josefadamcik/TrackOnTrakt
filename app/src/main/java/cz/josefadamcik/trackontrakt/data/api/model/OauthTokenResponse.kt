
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