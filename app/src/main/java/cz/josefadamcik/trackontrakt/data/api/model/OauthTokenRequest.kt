

package cz.josefadamcik.trackontrakt.data.api.model

data class OauthTokenRequest(
    val code: String,
    val client_id: String,
    val client_secret: String,
    val redirect_uri: String,
    val grant_type: String = "authorization_code"
)
