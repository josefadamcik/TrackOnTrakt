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

import cz.josefadamcik.trackontrakt.base.BasePresenter
import timber.log.Timber
import javax.inject.Inject


class HomePresenter @Inject constructor(
    private val userHistoryManager: UserHistoryManager
) : BasePresenter<HomeView>() {

    override fun attachView(view: HomeView) {
        super.attachView(view)

        loadHomeStreamData(false)
    }


    fun loadHomeStreamData(forceRefresh: Boolean) {
        Timber.i("loadHomeStreamData: start")
        if (!forceRefresh) {
            view?.showLoading()
        }
        disposables.add(
            userHistoryManager.loadUserHistory()
                .subscribe(
                    { history ->
                        Timber.d("loadHomeStreamData ")
                        view?.hideLoading()
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