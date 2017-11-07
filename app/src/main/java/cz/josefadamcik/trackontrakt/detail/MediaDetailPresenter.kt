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

import cz.josefadamcik.trackontrakt.base.BasePresenter
import cz.josefadamcik.trackontrakt.data.api.model.*
import khronos.Dates
import timber.log.Timber
import javax.inject.Inject

class MediaDetailPresenter @Inject constructor(
    private val manager: MediaDetailManager
) : BasePresenter<MediaDetailView>() {

    private var identifier: MediaIdentifier? = null
    private var movieDetail: MovieDetail? = null
    private var showDetail: ShowDetail? = null
    internal var model: MediaDetailModel? = null

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
                manager.loadMovieInfo(mediaId)
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
                manager.loadShowInfo(mediaId)
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
            val request = CheckinRequest(movie = Movie(title, year, ids))
            doCheckinRequest(request)
        }
    }

    fun checkinActionClicked(episodeWProgress: EpisodeWithProgress) {
        if (episodeWProgress.progress.completed) {
            //display info
            view?.showAlreadyWatchedStats(episodeWProgress.progress.number, episodeWProgress.progress.last_watched_at)
        } else {
            Timber.d("checkinActionClicked for $episodeWProgress")
            val request = CheckinRequest(episode = episodeWProgress.episode)
            doCheckinRequest(request)

        }
    }


    private fun doCheckinRequest(request: CheckinRequest) {
        view?.showLoading()
        view?.itemCheckInactionEnabled(false)
        disposables.add(
            manager.doCheckin(request).subscribe(
                { result ->
                        Timber.d("checkin complete $result")
                        view?.hideLoading()
                        view?.itemCheckInactionEnabled(true)
                        if (result.isSuccessful && result.code() == 201) {
                            view?.showCheckinSuccess()
                            model?.let { it ->
                                model = applyCheckinOnDataModel(it, request)
                                showModel(model)
                            }
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

    private fun applyCheckinOnDataModel(model: MediaDetailModel, request: CheckinRequest): MediaDetailModel {
        var episodeFoundAndCompletedStatusChanged = false
        val modifiedSeasons = model.seasons.map { season ->
            if (season.season.number == request.episode?.season) {
                val modifiedEpisodes = season.episodes.map { e ->
                    if (e.episode.number == request.episode.number) {
                        episodeFoundAndCompletedStatusChanged = true;
                        e.copy(progress = ShowWatchedProgress.EpisodeWatchedProgress(
                            e.episode.number,
                            true,
                            Dates.today
                        ))
                    } else {
                        e
                    }
                }
                season.copy(episodes = modifiedEpisodes)
            } else {
                season
            }

        }
        return model.copy(
            seasons = modifiedSeasons,
            showProgress = model.showProgress.copy(
                completed = model.showProgress.completed + if (episodeFoundAndCompletedStatusChanged) 1 else 0,
                last_watched_at = Dates.today
            )
        )
    }

    private fun getOnError(): (Throwable?) -> Unit {
        return { t: Throwable? ->
            Timber.e(t)
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
            manager.loadShowSeasonsWithEpisodes(showId)
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
            manager.loadShowWatchedProgress(showId)
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