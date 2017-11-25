
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