/*
 Copyright 2017 Josef Adamcik <josef.adamcik@gmail.com>

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
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
                            view?.hideProgress()
                            view?.continueNavigation()
                        },
                        { t ->
                            view?.hideProgress()
                            view?.showErrorView()
                            view?.showErrorMessageWithRetry(R.string.err_trakt_auth_failed)
                        }
                    )
            )
            return true;
        }
        return false
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