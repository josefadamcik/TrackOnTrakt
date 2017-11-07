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
        if (!forceRefresh) {
            view?.showLoading()
        }
        if (forceRefresh) {
            lastPage = 0
            loadedHistoryModel = HistoryModel()
        }
        loadingPage = lastPage + 1
        Timber.d("loadHomeStreamData page {$loadingPage}")
        disposables.add(
            userHistoryManager.loadUserHistory(loadingPage)
                .subscribe(
                    { history ->
                        Timber.d("loadHomeStreamData {$loadingPage}")
                        lastPage = loadingPage
                        loadingPage = -1
                        view?.hideLoading()
                        val allItems = loadedHistoryModel.items.toMutableList()
                        allItems.addAll(history.items)

                        loadedHistoryModel = loadedHistoryModel.copy(
                            items = allItems.toList(),
                            hasNextPage = lastPage < history.pageCount
                        )

                        view?.showHistory(loadedHistoryModel)
                    },
                    { t ->
                        Timber.e(t, "loadHomeStreamData error, page {$loadingPage}")
                        loadingPage = -1
                        view?.hideLoading()
                        view?.showError(t)
                    }
                )
        )
    }

    fun loadNextPage() {
        loadHomeStreamData(false)
    }


}