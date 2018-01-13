
package cz.josefadamcik.trackontrakt.detail

import cz.josefadamcik.trackontrakt.base.BasePresenter
import cz.josefadamcik.trackontrakt.data.api.model.*
import cz.josefadamcik.trackontrakt.util.CurrentTimeProvider
import timber.log.Timber
import javax.inject.Inject

class MediaDetailPresenter @Inject constructor(
    private val manager: MediaDetailManager,
    private val currentTimeProvider: CurrentTimeProvider
) : BasePresenter<MediaDetailView>() {

    private var identifier: MediaIdentifier? = null
    private var showDetail: ShowDetail? = null
    var movieDetail: MovieDetail? = null
    var model: MediaDetailModel? = null
    var willDoCheckinForSubject: CheckinSubject? = null


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
            willDoCheckinForSubject = CheckinSubject.MovieCheckin(Movie(title, year, ids))
            view?.showCheckinDialog(title)

        }
    }

    fun checkinActionClicked(episodeWProgress: EpisodeWithProgress) {
        willDoCheckinForSubject = CheckinSubject.EpisodeCheckin(episodeWProgress.episode)
        view?.showCheckinDialog("${episodeWProgress.episode.season}x${episodeWProgress.episode.number} ${episodeWProgress.episode.title}")
    }

    fun checkinDialogDismissed() {
        willDoCheckinForSubject = null
    }

    fun checkinConfirmed(checkinTime: CheckinTime) {
        val checkinSubject = willDoCheckinForSubject ?: throw IllegalStateException("ne undergoing checkin")
        val checkin = CheckinWithTime(checkinSubject, checkinTime)
        willDoCheckinForSubject = null
        doCheckinRequest(checkin)
    }


    private fun doCheckinRequest(request: CheckinWithTime) {
        view?.showLoading()
        view?.itemCheckInactionEnabled(false)
        disposables.add(
            manager.doCheckin(request).subscribe(
                { result ->
                        Timber.d("checkin complete $result")
                        view?.hideLoading()
                        view?.itemCheckInactionEnabled(true)
                        when(result) {
                            is CheckinResult.Success -> {
                                view?.showCheckinSuccess()
                                model?.let { it ->
                                    model = applyCheckinOnDataModel(it, request)
                                    showModel(model)
                                }
                            }
                            is CheckinResult.InProgress -> view?.showCheckinAlreadyInProgress()
                            else -> view?.showError(IllegalStateException("Request failed"))

                        }
                    },
                getOnError()

            )
        )
    }

    private fun applyCheckinOnDataModel(model: MediaDetailModel, request: CheckinWithTime): MediaDetailModel {
        var episodeFoundAndCompletedStatusChanged = false
        if (request.subject is CheckinSubject.EpisodeCheckin ) {
            val modifiedSeasons = model.seasons.map { season ->
                if (season.season.number == request.subject.episode.season) {
                    val modifiedEpisodes = season.episodes.map { e ->
                        if (e.episode.number == request.subject.episode.number) {
                            episodeFoundAndCompletedStatusChanged = true;
                            e.copy(progress = ShowWatchedProgress.EpisodeWatchedProgress(
                                    e.episode.number,
                                    true,
                                    currentTimeProvider.dateTime
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
                            last_watched_at = currentTimeProvider.dateTime
                    )
            )
        } else {
            return model
        }


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