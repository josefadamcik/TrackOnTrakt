

package cz.josefadamcik.trackontrakt.data.api

import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import cz.josefadamcik.trackontrakt.data.api.model.OauthTokenResponse
import timber.log.Timber
import java.util.*

/**
 * Singleton holding auth token
 */
class TraktAuthTokenHolderImpl(
    private val preferences: SharedPreferences,
    private val moshi: Moshi
) : TraktAuthTokenHolder {
    companion object {
        const val PREF_KEY_RESPONSE = "trakt.oauth.response"
    }

    private var oauthTokenResponse: OauthTokenResponse? = null

    override val token get() = oauthTokenResponse?.access_token
    override val tokenInfo get() = oauthTokenResponse


    fun readFromPreferences() {
        val serialized = preferences.getString(PREF_KEY_RESPONSE, null)
        if (serialized != null) {
            oauthTokenResponse = moshi.adapter(OauthTokenResponse::class.java).fromJson(serialized)
        }
        Timber.i("Loading oauth token response %s", oauthTokenResponse)
        logOauthResponse(oauthTokenResponse)
    }

    private fun logOauthResponse(response: OauthTokenResponse?) {
        if (response != null) {
            Timber.i("created %s expired (%s at %s) issued before %s min (%s h)",
                Date(response.created_at * 1000),
                response.expired,
                response.expiresAt,
                response.createdBefore / 60,
                response.createdBefore / 3600
            )

        }
    }

    override fun httpAuth(): String {
        return String.format("Bearer %s", token)
    }

    override fun hasToken(): Boolean {
        return token != null
    }

    override fun forgetToken() {
        Timber.i("Forgetting auth token")
        oauthTokenResponse = null
        preferences.edit()
            .remove(PREF_KEY_RESPONSE)
            .apply()
    }


    override fun fillFromResponse(response: OauthTokenResponse) {
        oauthTokenResponse = response
        val json = moshi.adapter(OauthTokenResponse::class.java).toJson(response)

        preferences.edit()
            .putString(TraktAuthTokenHolderImpl.PREF_KEY_RESPONSE, json)
            .apply()

    }

    override fun expiresSoonerThanDays(days: Int): Boolean {
        return tokenInfo?.expiresSoonerThanDays(days) ?: false
    }
}
