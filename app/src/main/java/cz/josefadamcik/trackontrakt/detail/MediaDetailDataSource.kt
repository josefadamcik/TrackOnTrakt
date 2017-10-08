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

package cz.josefadamcik.trackontrakt.detail

import cz.josefadamcik.trackontrakt.data.api.TraktApi
import cz.josefadamcik.trackontrakt.data.api.TraktAuthTokenProvider
import cz.josefadamcik.trackontrakt.data.api.model.*
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

class MediaDetailDataSource @Inject constructor(
    private val traktApi: TraktApi,
    private val tokenHolder: TraktAuthTokenProvider) {


    fun loadMovieInfo(mediaId: MediaIdentifier): Single<MovieDetail> {
        return traktApi.movie(tokenHolder.httpAuth(), mediaId.id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    fun loadShowInfo(mediaId: MediaIdentifier): Single<ShowDetail> {
        return traktApi.show(tokenHolder.httpAuth(), mediaId.id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun doCheckin(request: CheckinRequest): Single<Response<CheckinResponse>> {
        return traktApi.checkin(tokenHolder.httpAuth(), request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun loadShowLastEpisode(showId: Long): Single<Response<Episode>> {
        return traktApi.showLastEpisode(tokenHolder.httpAuth(), showId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun loadShowSeasonsWithEpisodes(showId: Long): Single<List<Season>> {
        return traktApi.showSeasons(tokenHolder.httpAuth(), showId, TraktApi.ExtendedInfo.episodes)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->
                Timber.d("loadShowSeasonsWithEpisodes - result %s", response.code())
                var seasons = response.body()
                if (seasons.isNotEmpty() && seasons.first().number == 0) {
                    //Season 0 -> specials, put them to an and and rename to specials
                    val specialsSeason = seasons.first()
                    val modifiedSeasonsList = seasons.toMutableList()
                    modifiedSeasonsList.removeAt(0)
                    modifiedSeasonsList.add(specialsSeason)
                    seasons = modifiedSeasonsList
                }
                seasons
            }

    }
}
