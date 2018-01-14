
package cz.josefadamcik.trackontrakt.data.api

import cz.josefadamcik.trackontrakt.data.api.model.OauthTokenResponse

class TestTraktAuthTokenHolder(public var authorized: Boolean = true) : TraktAuthTokenHolder {
    companion object {
        public val instance = TestTraktAuthTokenHolder(true)
    }

    override val token: String?
        get() = if (authorized) "TEST TOKEN" else null
    override val tokenInfo: OauthTokenResponse?
        get() = null
    override fun forgetToken() {}
    override fun fillFromResponse(response: OauthTokenResponse) {}
    override fun httpAuth(): String = "Authorization: TEST"
    override fun hasToken(): Boolean = authorized
    override fun expiresSoonerThanDays(days: Int): Boolean = false
}