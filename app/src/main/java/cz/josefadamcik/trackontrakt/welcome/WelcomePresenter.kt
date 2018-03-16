package cz.josefadamcik.trackontrakt.welcome

import cz.josefadamcik.trackontrakt.base.BasePresenter
import cz.josefadamcik.trackontrakt.data.api.TraktAuthTokenHolder
import timber.log.Timber
import javax.inject.Inject

/**
 */
class WelcomePresenter @Inject constructor(
        private val traktAuthTokenHolder: TraktAuthTokenHolder
        ) :  BasePresenter<WelcomeView>() {
    fun start() {
        checkOrStartAuth()
    }

    private fun checkOrStartAuth() {
        if (traktAuthTokenHolder.hasToken() && !traktAuthTokenHolder.expiresSoonerThanDays(30)) {
            Timber.d("has token: %s", traktAuthTokenHolder.token)
            view?.navigateToHome()
        }
    }

    fun onLoginButtonClick() {
        view?.navigateToLogin()
    }
}