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

import cz.josefadamcik.trackontrakt.BuildConfig
import cz.josefadamcik.trackontrakt.base.BasePresenter
import cz.josefadamcik.trackontrakt.data.api.TraktApi
import cz.josefadamcik.trackontrakt.data.api.TraktAuthTokenProvider
import cz.josefadamcik.trackontrakt.data.api.model.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class MediaDetailPresenter @Inject constructor(
    val traktApi: TraktApi,
    val tokenHolder: TraktAuthTokenProvider
) : BasePresenter<MediaDetailView>() {

    private var identifier: MediaIdentifier? = null
    private var movieDetail: MovieDetail? = null
    private var showDetail: ShowDetail? = null
    private var model: MediaDetailModel? = null

    fun load(mediaId: MediaIdentifier?, name: String?) {
        Timber.i("load: %s", mediaId)

        if (mediaId == null) {
            //fixme: error
            return
        }

        if (name != null) {
            view?.showTitle(name)
        }

        view?.itemCheckInactionVisible(false)
        view?.itemCheckInactionEnabled(false)

        view?.showLoading()
        if (mediaId.type == MediaType.movie) {
            disposables.add(
                traktApi.movie(tokenHolder.httpAuth(), mediaId.id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { movie ->
                            view?.hideLoading()
                            if (movie != null) {
                                showMovie(movie)
                            }
                        },
                        { t ->
                            view?.hideLoading()
                            view?.showError(t)
                        }

                    )
            )
        } else if (mediaId.type == MediaType.show) {
            disposables.add(
                traktApi.show(tokenHolder.httpAuth(), mediaId.id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { show ->
                            view?.hideLoading()
                            if (show != null) {
                                showShow(show)
                            }
                        },
                        { t ->
                            view?.hideLoading()
                            view?.showError(t)
                        }
                    )
            )
        }

    }


    fun checkinActionClicked() {
        this.movieDetail?.let { (title, ids, year) ->
            val request = CheckinRequest(
                movie = Movie(title, year, ids),
                app_version = BuildConfig.VERSION_NAME,
                app_date = BuildConfig.BUILD_DATE
            )
            doCheckinRequest(request)
        }
    }

    fun checkinActionClicked(episode: Episode) {
        Timber.d("checkinActionClicked for $episode")
        val request = CheckinRequest(
            episode = episode,
            app_version = BuildConfig.VERSION_NAME,
            app_date = BuildConfig.BUILD_DATE
        )
        doCheckinRequest(request)
    }


    private fun doCheckinRequest(request: CheckinRequest) {
        view?.showLoading()
        view?.itemCheckInactionEnabled(false)
        disposables.add(
            traktApi.checkin(tokenHolder.httpAuth(), request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result ->
                        Timber.d("checkin complete $result")
                        view?.hideLoading()
                        view?.itemCheckInactionEnabled(true)
                        if (result.isSuccessful && result.code() == 201) {
                            view?.showCheckinSuccess()
                        } else if (result.code() == 409) {
                            view?.showCheckinAlreadyInProgress()
                        } else {
                            Timber.e("Unexpected status code %s", result.code())
                            view?.showError(IllegalStateException("Unexpected status code " + result.code()))
                        }
                    },
                    { t: Throwable? ->
                        view?.hideLoading()
                        view?.showError(t)
                    }
                )
        )
    }

  

    private fun showShow(show: ShowDetail) {
        showDetail = show
        view?.showBasicInfo(show.year, show.certification, show.rating ?: 0.0, show.votes ?: 0)
        showModel(MediaDetailModel(MediaDetailModel.MediaDetailInfo(
            description = show.overview,
            homepage = show.homepage,
            traktPage = "https://trakt.tv/shows/${show.ids.slug}",
            rating = show.rating ?: 0.0,
            certification = show.certification,
            votes = show.votes ?: 0,
            network = show.network,
            genres = show.genres,
            date = show.first_aired,
            trailer = show.trailer,
            status = show.status,
            language = show.language
        )))

        view?.showLoading()
        Timber.d("showShow - last episode")
        disposables.add(
            traktApi.showLastEpisode(tokenHolder.httpAuth(), show.ids.trakt)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { response ->
                        Timber.d("showShow - last episode result")
                        if (response.code() == 204) {
                            Timber.d("No last episode")
                        } else {
                            val latestEpisode = response.body()
                            Timber.d("Last episode %s", latestEpisode)
                            showModel(model?.copy(latestEpisode = latestEpisode))
                        }
                        loadEpisodes(show.ids.trakt)
                    },
                    { t ->
                        view?.hideLoading()
                        view?.showError(t)
                    }
                )

        )
    }

    private fun loadEpisodes(showId: Long) {
        Timber.d("loadEpisodes ")
        disposables.add(
            traktApi.showSeasons(tokenHolder.httpAuth(), showId, TraktApi.ExtendedInfo.episodes)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { response ->
                        Timber.d("loadEpisodes - result %s", response.code())
                        view?.hideLoading()
                        var seasons = response.body()
                        if (seasons.isNotEmpty() && seasons.first().number == 0) {
                            //Season 0 -> specials, put them to an and and rename to specials
                            val specialsSeason = seasons.first()
                            val modifiedSeasonsList = seasons.toMutableList()
                            modifiedSeasonsList.removeAt(0)
                            modifiedSeasonsList.add(specialsSeason)
                            seasons = modifiedSeasonsList
                        }

                        showModel(model?.copy(seasons = seasons))
                    },
                    { t ->
                        view?.hideLoading()
                        view?.showError(t)
                    }
                )

        )
    }

    private fun showMovie(movie: MovieDetail) {
        movieDetail = movie
        view?.showBasicInfo(movie.year, movie.certification, movie.rating ?: 0.0, movie.votes ?: 0)
        view?.itemCheckInactionVisible(true)
        view?.itemCheckInactionEnabled(true)
        showModel(MediaDetailModel(MediaDetailModel.MediaDetailInfo(
            tagline = movie.tagline,
            description = movie.overview,
            homepage = movie.homepage,
            traktPage = "https://trakt.tv/movies/${movie.ids.slug}",
            rating = movie.rating ?: 0.0,
            certification = movie.certification,
            votes = movie.votes ?: 0,
            genres = movie.genres,
            date = movie.released,
            trailer = movie.trailer,
            language = movie.language
        )))

    }

    private fun showModel(model: MediaDetailModel?) {
        if (model != null) {
            this.model = model
            view?.showMedia(model = model)
        }
    }


}