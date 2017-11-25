
package cz.josefadamcik.trackontrakt.data.api

import cz.josefadamcik.trackontrakt.data.api.model.OauthTokenResponse

/**
 * Provides authorization token.
 */
interface TraktAuthTokenProvider {
    val token: String?
    val tokenInfo: OauthTokenResponse?

    fun httpAuth(): String
    fun hasToken(): Boolean

}