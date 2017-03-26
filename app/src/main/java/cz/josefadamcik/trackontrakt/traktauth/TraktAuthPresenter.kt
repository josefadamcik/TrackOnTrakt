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

import com.hannesdorfmann.mosby3.mvp.MvpPresenter
import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.data.api.TraktAuthTokenHolder
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject


class TraktAuthPresenter @Inject constructor(
    private val authorizationProvider: AuthorizationProvider,
    private val traktAuthTokenHolder: TraktAuthTokenHolder
) : MvpPresenter<TraktAuthView> {

    private var view: TraktAuthView? = null
    private val disposable = CompositeDisposable()

    override fun attachView(view: TraktAuthView?) {
        this.view = view;
        checkOrStartAuth()
    }


    override fun detachView(retainInstance: Boolean) {
        disposable.clear()
        view = null
    }

    fun onBrowserRedirected(url: String) : Boolean {
        if (authorizationProvider.shouldHandleRedirectUrl(url)) {
            disposable.add(
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