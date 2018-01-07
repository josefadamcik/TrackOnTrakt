
package cz.josefadamcik.trackontrakt.data.api

data class TraktApiConfig (
    val clientId: String,
    val clientSecret: String,
    val oauthRedirectUrl: String,
    val apiBaseUrl: String,
    val loginUrl: String
    )