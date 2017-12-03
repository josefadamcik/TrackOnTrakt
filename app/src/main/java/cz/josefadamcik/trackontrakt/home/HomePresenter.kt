
package cz.josefadamcik.trackontrakt.home

import android.support.annotation.VisibleForTesting
import cz.josefadamcik.trackontrakt.base.BasePresenter
import cz.josefadamcik.trackontrakt.data.api.model.Watching
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import org.threeten.bp.LocalDateTime
import timber.log.Timber
import javax.inject.Inject


class HomePresenter @Inject constructor(
    private val userHistoryManager: UserHistoryManager
) : BasePresenter<HomeView>() {

    @VisibleForTesting
    private var lastPage = 0
    private var loadingPage = -1
    private var loadedHistoryModel = HistoryModel()


    override fun attachView(view: HomeView) {
        super.attachView(view)

        loadHomeStreamData(false)
    }

    fun loadHomeStreamData(forceRefresh: Boolean) {
        Timber.i("loadHomeStreamData: start")
        if (!forceRefresh && lastPage == 0) {
            view?.showLoading()
        }
        if (forceRefresh) {
            lastPage = 0
            updateHistoryModel(HistoryModel())
        }
        loadingPage = lastPage + 1
        Timber.d("loadHomeStreamData page {$loadingPage}")
        if (loadingPage > 1) {
            updateHistoryModel(loadedHistoryModel.copy(loadingNextPage = true))
        }
        disposables.add(
            (if (loadingPage == 1) loadFirstPage() else loadAnotherPage(loadingPage))
                .subscribe(
                    { (history, watching) ->
                        Timber.d("loadHomeStreamData done {$loadingPage}")
                        lastPage = loadingPage
                        loadingPage = -1
                        view?.hideLoading()
                        val allItems = loadedHistoryModel.items.toMutableList()
                        val now = LocalDateTime.now()
                        allItems.addAll(history.items)

                        updateHistoryModel(loadedHistoryModel.copy(
                            items = allItems.filter { it.watched_at.isBefore(now) },
                            hasNextPage = lastPage < history.pageCount,
                            loadingNextPage = false,
                            watching = if (watching.isExpired()) Watching.Nothing else watching
                        ))
                    },
                    { t ->
                        Timber.e(t, "loadHomeStreamData error, page {$loadingPage}")
                        loadingPage = -1
                        view?.hideLoading()
                        view?.showError(t)
                        if (loadedHistoryModel.loadingNextPage) {
                            updateHistoryModel(loadedHistoryModel.copy(loadingNextPage = false))
                        }
                    }
                )
        )
    }


    /** For the first page we will also load the "currently watching" item */
    private fun loadFirstPage(): Single<Pair<HistoryItems, Watching>> {
        return userHistoryManager.loadUserHistory(1)
            .zipWith(
                userHistoryManager.loadWatching(),
                BiFunction<HistoryItems, Watching, Pair<HistoryItems, Watching>> { history, watching ->
                    Pair(history, watching)
                }
            )
    }

    /**
     * For another pages we will not fetch the "currently watching" item
     */
    private fun loadAnotherPage(page: Int): Single<Pair<HistoryItems, Watching>> {
        return userHistoryManager.loadUserHistory(page)
            .zipWith(
                Single.just(loadedHistoryModel.watching),
                BiFunction<HistoryItems, Watching, Pair<HistoryItems, Watching>> { history, watching ->
                    Pair(history, watching)
                }
            )
    }


    fun loadNextPage() {
        loadHomeStreamData(false)
    }

    /**
     * Update local model state and propagate it to view
     */
    private fun updateHistoryModel(model: HistoryModel) {
        loadedHistoryModel = model
        view?.showHistory(model)
    }


}