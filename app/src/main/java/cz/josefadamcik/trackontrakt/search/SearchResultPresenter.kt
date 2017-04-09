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
package cz.josefadamcik.trackontrakt.search

import com.hannesdorfmann.mosby3.mvp.MvpPresenter
import cz.josefadamcik.trackontrakt.data.api.TraktApi
import cz.josefadamcik.trackontrakt.data.api.TraktAuthTokenHolder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class SearchResultPresenter @Inject constructor(
    private val traktApi: TraktApi,
    private val tokenHolder: TraktAuthTokenHolder
)
    : MvpPresenter<SearchResultsView> {
    private var view: SearchResultsView? = null
    private val disposables = CompositeDisposable()

    override fun attachView(view: SearchResultsView?) {
        this.view = view

    }

    override fun detachView(retainInstance: Boolean) {
        view = null
        disposables.clear()
    }


    fun search(query: String?, filter: TraktFilter) {
        Timber.d("search $query $filter")

        if (query == null) {
            view?.showEmptyResult()
            return

        }
        view?.showLoading()
        disposables.add(
            traktApi.search(tokenHolder.httpAuth(), filter.forApiQuery() , query, TraktApi.ExtendedInfo.metadata)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { results ->
                        Timber.d("search $results")
                        view?.showSearchResults(results)

                    },
                    { t ->
                        Timber.e(t, "search for $query failed")
                        view?.showError(t)
                    }
                )

        )
    }
}