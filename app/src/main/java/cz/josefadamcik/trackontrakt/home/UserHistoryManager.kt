
package cz.josefadamcik.trackontrakt.home

import cz.josefadamcik.trackontrakt.ApplicationScope
import cz.josefadamcik.trackontrakt.data.api.ApiException
import cz.josefadamcik.trackontrakt.data.api.TraktApi
import cz.josefadamcik.trackontrakt.data.api.TraktAuthTokenProvider
import cz.josefadamcik.trackontrakt.data.api.model.Watching
import cz.josefadamcik.trackontrakt.util.RxSchedulers
import io.reactivex.Single
import javax.inject.Inject

@ApplicationScope
class UserHistoryManager
@Inject constructor(
    private val traktApi: TraktApi,
    private val authTokenProvider: TraktAuthTokenProvider,
    private val rxSchedulers: RxSchedulers
) {

    fun loadWatching(): Single<Watching> {
        if (authTokenProvider.hasToken().not()) {
            return Single.error(IllegalStateException("Missing authorisation token"));
        } else {
            return traktApi.watching(authTokenProvider.httpAuth())
                .subscribeOn(rxSchedulers.subscribe)
                .observeOn(rxSchedulers.observe)
                .map { r -> when {
                        r.code() == 204 -> //NotWatchingAnything
                            Watching.Nothing
                        r.isSuccessful -> r.body()
                        else -> throw ApiException("Unable to load currently watching, response failed ${r.code()} ${r.message()}", r.code(), r.message())
                    }
                }
        }
    }

    fun loadUserHistory(loadingPage: Int): Single<HistoryItems> {
        if (authTokenProvider.hasToken().not()) {
            return Single.error(IllegalStateException("Missing authorisation token"));
        } else {
            return traktApi.myHistory(authTokenProvider.httpAuth(), loadingPage)
                .subscribeOn(rxSchedulers.subscribe)
                .observeOn(rxSchedulers.observe)
                .map { r ->
                    if (r.isSuccessful) {
                        val headers = r.headers()
                        val page = headers[TraktApi.HEADER_PAGINATION_PAGE]?.toInt() ?: 0
                        val pageCount = headers[TraktApi.HEADER_PAGINATION_PAGE_COUNT]?.toInt() ?: 0
                        val itemCount = headers[TraktApi.HEADER_PAGINATION_ITEM_COUNT]?.toInt() ?: 0
                        HistoryItems(items = r.body(), page = page, pageCount = pageCount, itemCount = itemCount)
                    } else {
                        throw ApiException("Unable to load more, response not successful {${r.code()}", r.code(), r.message())
                    }
                }
        }
    }

}