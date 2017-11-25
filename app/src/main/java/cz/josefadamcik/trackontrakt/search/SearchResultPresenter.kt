
package cz.josefadamcik.trackontrakt.search

import cz.josefadamcik.trackontrakt.base.BasePresenter
import cz.josefadamcik.trackontrakt.data.api.TraktApi
import cz.josefadamcik.trackontrakt.data.api.TraktAuthTokenProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class SearchResultPresenter @Inject constructor(
    private val traktApi: TraktApi,
    private val tokenHolder: TraktAuthTokenProvider
) : BasePresenter<SearchResultsView>() {


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