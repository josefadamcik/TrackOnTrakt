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
import cz.josefadamcik.trackontrakt.data.api.model.*
import timber.log.Timber
import javax.inject.Inject

class MediaDetailPresenter @Inject constructor(
    private val dataSource: MediaDetailDataSource
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
                dataSource.loadMovieInfo(mediaId)
                    .subscribe(
                        { movie ->
                            view?.hideLoading()
                            showMovie(movie)
                        },
                        getOnError()

                    )
            )
        } else if (mediaId.type == MediaType.show) {
            disposables.add(
                dataSource.loadShowInfo(mediaId)
                    .subscribe(
                        { show ->
                            view?.hideLoading()
                            if (show != null) {
                                showShow(show)
                            }
                        },
                        getOnError()
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
            dataSource.doCheckin(request).subscribe(
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
                getOnError()

            )
        )
    }

    private fun getOnError(): (Throwable?) -> Unit {
        return { t: Throwable? ->
            view?.hideLoading()
            view?.showError(t)
        }
    }

    private fun showShow(show: ShowDetail) {
        showDetail = show
        view?.showBasicInfo(show.year, show.certification, show.rating ?: 0.0, show.votes ?: 0)
        showModel(
            MediaDetailModel(
                MediaDetailModel.MediaDetailInfo(
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
                )
            )
        )

        loadEpisodes(show.ids.trakt)
    }

    private fun loadEpisodes(showId: Long) {
        view?.showLoading()
        Timber.d("loadEpisodes ")
        disposables.add(
            dataSource.loadShowSeasonsWithEpisodes(showId)
                .subscribe(
                    { seasons ->
                        view?.hideLoading()
                        val seasonsWithProgress = seasons.map { season ->
                            SeasonWithProgress(
                                season = season,
                                episodes = season.episodes?.map { ep -> EpisodeWithProgress(ep) } ?: emptyList()
                            )
                        }
                        showModel(model?.copy(seasons = seasonsWithProgress))
                        loadProgress(showId)
                    },
                    getOnError()
                )

        )
    }

    private fun loadProgress(showId: Long) {
        view?.showLoading()
        Timber.d("loadProgress  ")
        disposables.add(
            dataSource.loadShowWatchedProgress(showId)
                .subscribe(
                    { progress ->
                        view?.hideLoading()
                        model?.let { model ->
                            val seasonsWithProgress = combineSeasonDataWithProgress(model.seasons, progress)
                            showModel(model.copy(seasons = seasonsWithProgress, showProgress = progress))
                        }
                    },
                    getOnError()
                )

        )
    }

    internal fun combineSeasonDataWithProgress(original: List<SeasonWithProgress>, progress: ShowWatchedProgress): List<SeasonWithProgress> {
        return original.map { swp ->
            val newSeasonProgress = progress.seasons.find { it.number == swp.season.number }
                ?: ShowWatchedProgress.SeasonWatchedProgress(number = swp.season.number)

            SeasonWithProgress(
                season = swp.season,
                progress = newSeasonProgress,
                episodes = swp.episodes.map { episodeWithProgress ->
                    episodeWithProgress.copy(
                        progress = newSeasonProgress.episodes.find { ep -> ep.number == episodeWithProgress.episode.number }
                            ?: ShowWatchedProgress.EpisodeWatchedProgress(episodeWithProgress.episode.number)
                    )
                }
            )
        }
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