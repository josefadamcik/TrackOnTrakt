

package cz.josefadamcik.trackontrakt.traktauth


import cz.josefadamcik.trackontrakt.ApplicationScope
import cz.josefadamcik.trackontrakt.data.api.ApiRxSchedulers
import cz.josefadamcik.trackontrakt.data.api.TraktApi
import cz.josefadamcik.trackontrakt.data.api.TraktApiConfig
import cz.josefadamcik.trackontrakt.data.api.TraktAuthTokenHolder
import cz.josefadamcik.trackontrakt.data.api.model.OauthTokenRequest
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject

@ApplicationScope
class AuthorizationProvider
@Inject
constructor(
    private val traktApi: TraktApi,
    private val traktApiConfig: TraktApiConfig,
    private val traktAuthTokenHolder: TraktAuthTokenHolder,
    private val rxSchedulers: ApiRxSchedulers
) {


    fun getOauthAuthorizationUrl(): String {
        val oauthUrl = String.format(
            traktApiConfig.loginUrl,
            traktApiConfig.clientId,
            traktApiConfig.oauthRedirectUrl
        )
        return oauthUrl;
    }

    /**
     * @param code - code from query parameter of redirected url
     */
    fun requestAuthToken(code: String): Single<TraktAuthorizationResult> {

        return traktApi.oauthToken(
            OauthTokenRequest(code,
                traktApiConfig.clientId,
                traktApiConfig.clientSecret,
                traktApiConfig.oauthRedirectUrl)
        )
            .subscribeOn(rxSchedulers.subscribe)
            .flatMap { res ->
                if (res.isSuccessful) {
                    val response = res.body()
                    Timber.d("obtained access_token %s; response %s", response.access_token, response)
                    traktAuthTokenHolder.fillFromResponse(response)
                    Single.just(TraktAuthorizationResult(true, response.access_token))
                } else {
                    Timber.w("failed %s : %s", res.code(), res.message())
                    Single.just(TraktAuthorizationResult(false, null))
                }

            }
            .observeOn(rxSchedulers.observe)


    }

    fun shouldHandleRedirectUrl(url: String): Boolean {
        return url.startsWith(traktApiConfig.oauthRedirectUrl)
    }
}
