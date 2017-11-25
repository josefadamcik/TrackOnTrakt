
package cz.josefadamcik.trackontrakt.home

import android.support.annotation.VisibleForTesting
import cz.josefadamcik.trackontrakt.base.BasePresenter
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
            userHistoryManager.loadUserHistory(loadingPage)
                .subscribe(
                    { history ->
                        Timber.d("loadHomeStreamData done {$loadingPage}")
                        lastPage = loadingPage
                        loadingPage = -1
                        view?.hideLoading()
                        val allItems = loadedHistoryModel.items.toMutableList()
                        allItems.addAll(history.items)

                        updateHistoryModel(loadedHistoryModel.copy(
                            items = allItems.toList(),
                            hasNextPage = lastPage < history.pageCount,
                            loadingNextPage = false
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