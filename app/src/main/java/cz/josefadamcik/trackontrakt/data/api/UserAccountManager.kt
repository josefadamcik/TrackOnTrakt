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
package cz.josefadamcik.trackontrakt.data.api

import cz.josefadamcik.trackontrakt.ApplicationScope
import cz.josefadamcik.trackontrakt.data.api.model.HistoryItem
import cz.josefadamcik.trackontrakt.data.api.model.Settings
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


@ApplicationScope
class UserAccountManager
@Inject constructor(
    private val traktApi: TraktApi,
    private val traktAuthTokenHolder: TraktAuthTokenHolder
) {
    /**
     * Produces IllegalStateException when not authorised (missing auth token).
     */
    fun obtainUserSettings(): Single<Settings> {
        if (traktAuthTokenHolder.hasToken().not()) {
            return Single.error(IllegalStateException("Missing authorisation token"));
        } else {
            return traktApi.userSettings(traktAuthTokenHolder.httpAuth())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun loadUserHistory(): Single<List<HistoryItem>> {
        if (traktAuthTokenHolder.hasToken().not()) {
            return Single.error(IllegalStateException("Missing authorisation token"));
        } else {
            return traktApi.myHistory(traktAuthTokenHolder.httpAuth(), 1, 10)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }
}