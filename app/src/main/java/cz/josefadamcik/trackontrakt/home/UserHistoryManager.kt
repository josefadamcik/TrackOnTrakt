
package cz.josefadamcik.trackontrakt.home

import cz.josefadamcik.trackontrakt.ApplicationScope
import cz.josefadamcik.trackontrakt.data.api.TraktApi
import cz.josefadamcik.trackontrakt.data.api.TraktAuthTokenProvider
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@ApplicationScope
class UserHistoryManager
@Inject constructor(
    private val traktApi: TraktApi,
    private val authTokenProvider: TraktAuthTokenProvider
) {

    fun loadUserHistory(loadingPage: Int): Single<HistoryItems> {
        if (authTokenProvider.hasToken().not()) {
            return Single.error(IllegalStateException("Missing authorisation token"));
        } else {
            return traktApi.myHistory(authTokenProvider.httpAuth(), loadingPage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { r ->
                    if (r.isSuccessful) {
                        val headers = r.headers()
                        val page = headers["X-Pagination-Page"]?.toInt() ?: 0
                        val pageCount = headers["X-Pagination-Page-Count"]?.toInt() ?: 0
                        val itemCount = headers["X-Pagination-Item-Count"]?.toInt() ?: 0
                        HistoryItems(items = r.body(), page = page, pageCount = pageCount, itemCount = itemCount)
                    } else {
                        throw Exception("Unable to load more, response not successful {${r.code()}")
                    }
                }
        }
    }
}