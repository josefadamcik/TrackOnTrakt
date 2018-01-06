
package cz.josefadamcik.trackontrakt.traktauth

import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.base.BasePresenter
import cz.josefadamcik.trackontrakt.data.api.TraktAuthTokenHolder
import timber.log.Timber
import javax.inject.Inject


class TraktAuthPresenter @Inject constructor(
    private val authorizationProvider: AuthorizationProvider,
    private val traktAuthTokenHolder: TraktAuthTokenHolder
) : BasePresenter<TraktAuthView>() {

    override fun attachView(view: TraktAuthView) {
        super.attachView(view)
        checkOrStartAuth()
    }


    fun onBrowserRedirected(url: String) : Boolean {
        if (authorizationProvider.shouldHandleRedirectUrl(url)) {
            disposables.add(
                authorizationProvider.onTraktAuthRedirect(url)
                    .subscribe(
                        { res ->
                            if (res.success && res.token != null) {
                                view?.hideProgress()
                                view?.continueNavigation()
                            } else {
                                onError()
                            }
                        },
                        { t -> onError() }
                    )
            )
            return true
        }
        return false
    }

    private fun onError() {
        view?.hideProgress()
        view?.showErrorView()
        view?.showErrorMessageWithRetry(R.string.err_trakt_auth_failed)
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