
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
    private var showDetail: ShowDetail? = null
    private var willDoCheckinForSubject: CheckinSubject? = null
    var movieDetail: MovieDetail? = null
    var model: MediaDetailModel? = null
    private val rowItemListFactory = MediaDetailRowItemListFactory()


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
                            showShow(show)
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
                                model = it.copyWithAppliedCheckin(request, currentTimeProvider)
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
            manager.loadShowSeasons(showId)
                .subscribe(
                    { seasons ->
                        view?.hideLoading()
                        val seasonsWithProgress = seasons.map { season -> SeasonWithProgress(season = season, episodesLoaded = false) }
                        showModel(model?.copy(seasons = seasonsWithProgress))
                        loadEpisodes(showId, seasons)
                    },
                    getOnError()
                )

        )
    }

    private fun loadEpisodes(showId: Long, seasons: List<Season>) {
        disposables.add(
                manager.loadEpisodesForSeasons(showId, seasons)
                        .subscribe(
                                //onNext -> update model and propagate to view
                                { seasonWithProgress ->
                                    model?.let { model ->
                                        showModel(model.copy(seasons = model.seasons.map { //replace loaded season
                                            if (it.season.ids.trakt == seasonWithProgress.season.ids.trakt) {
                                                seasonWithProgress
                                            } else {
                                                it
                                            }
                                        }))
                                    }
                                },
                                getOnError(),
                                //onComplete
                                {
                                    view?.hideLoading()
                                    loadProgress(showId)
                                }
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
            swp.copy(
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
            view?.showMedia(rowItemListFactory.buildItems(model))
        }
    }




}