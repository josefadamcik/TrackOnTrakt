
package cz.josefadamcik.trackontrakt.data.api

import cz.josefadamcik.trackontrakt.data.api.model.OauthTokenResponse

/**
 * Holds and stores authorization token.
 */
interface TraktAuthTokenHolder : TraktAuthTokenProvider {
    fun forgetToken()
    fun fillFromResponse(response: OauthTokenResponse)
    fun expiresSoonerThanDays(days: Int): Boolean
}