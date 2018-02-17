
package cz.josefadamcik.trackontrakt.traktauth

import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.base.BasePresenter
import cz.josefadamcik.trackontrakt.data.api.TraktAuthTokenHolder
import cz.josefadamcik.trackontrakt.util.UriQueryParamParser
import timber.log.Timber
import javax.inject.Inject


class TraktAuthPresenter @Inject constructor(
    private val authorizationProvider: AuthorizationProvider,
    private val traktAuthTokenHolder: TraktAuthTokenHolder,
    private val uriQueryParamParser: UriQueryParamParser
) : BasePresenter<TraktAuthView>() {

    override fun attachView(view: TraktAuthView) {
        super.attachView(view)
    }


    fun onBrowserRedirected(url: String) : Boolean {
        if (authorizationProvider.shouldHandleRedirectUrl(url)) {
            val code = uriQueryParamParser.getUriParam(url, "code")
            if (code != null) {
                Timber.d("auth code: %s", code)
                disposables.add(
                        authorizationProvider.requestAuthToken(code)
                                .subscribe(
                                        { res ->
                                            if (res.success && res.token != null) {
                                                view?.hideProgress()
                                                view?.continueNavigation()
                                            } else {
                                                onError()
                                            }
                                        },
                                        { onError() }
                                )
                )
                return true
            }


        }
        return false
    }

    private fun onError() {
        view?.hideProgress()
        view?.showErrorView()
        view?.showErrorMessageWithRetry(R.string.err_trakt_auth_failed)
    }

    fun start() {
        checkOrStartAuth()
    }

    fun retry() {
        checkOrStartAuth()
    }


    private fun checkOrStartAuth() {
        if (traktAuthTokenHolder.hasToken() && !traktAuthTokenHolder.expiresSoonerThanDays(30)) {
            Timber.d("has token: %s", traktAuthTokenHolder.token)
            view?.continueNavigation()
        } else {
            initiateOauth()
        }
    }

    private fun initiateOauth() {
        view?.showProgress()
        val oauthUrl = authorizationProvider.getOauthAuthorizationUrl()
        view?.requestLoginToTraktInBrowser(oauthUrl)
    }




}