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
package cz.josefadamcik.trackontrakt.home

import com.hannesdorfmann.mosby3.mvp.MvpPresenter
import cz.josefadamcik.trackontrakt.data.api.TraktApi
import cz.josefadamcik.trackontrakt.data.api.TraktAuthTokenHolder
import cz.josefadamcik.trackontrakt.data.api.UserAccountManager
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject


class HomePresenter @Inject constructor(
    val userAccountManager: UserAccountManager,
    val traktApi: TraktApi,
    val tokenHolder: TraktAuthTokenHolder
) : MvpPresenter<HomeView> {
    var view: HomeView? = null
    val disposable = CompositeDisposable()

    override fun attachView(view: HomeView?) {
        this.view = view

        loadHomeStreamData(false)
    }

    override fun detachView(retainInstance: Boolean) {
        view = view
        disposable.clear()

    }

    fun loadHomeStreamData(forceRefresh: Boolean) {
        Timber.i("loadHomeStreamData: start")
        view?.showLoading()
        disposable.add(
            userAccountManager.loadUserHistory()
                .subscribe(
                    { history ->
                        Timber.d("loadHomeStreamData ")
                        view?.showHistory(history)
                    },
                    { t ->
                        Timber.e(t, "loadHomeStreamData error")
                        view?.hideLoading()
                        view?.showError(t)
                    }
                )
        )
    }


}