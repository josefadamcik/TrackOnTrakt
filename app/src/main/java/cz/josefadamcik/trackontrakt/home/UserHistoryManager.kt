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