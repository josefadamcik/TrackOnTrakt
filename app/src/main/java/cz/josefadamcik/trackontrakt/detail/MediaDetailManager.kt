

package cz.josefadamcik.trackontrakt.detail

import cz.josefadamcik.trackontrakt.data.api.ApiException
import cz.josefadamcik.trackontrakt.data.api.TraktApi
import cz.josefadamcik.trackontrakt.data.api.TraktAuthTokenProvider
import cz.josefadamcik.trackontrakt.data.api.model.*
import cz.josefadamcik.trackontrakt.util.RxSchedulers
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import retrofit2.Response
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MediaDetailManager @Inject constructor(
    private val traktApi: TraktApi,
    private val tokenHolder: TraktAuthTokenProvider,
    private val rxSchedulers: RxSchedulers
    ) {


    fun loadMovieInfo(mediaId: MediaIdentifier): Single<MovieDetail> {
        return traktApi.movie(tokenHolder.httpAuth(), mediaId.id)
            .subscribeOn(rxSchedulers.subscribe)
            .observeOn(rxSchedulers.observe)
            .doOnError(Consumer { Timber.e(it, "loadMovieInfo error") })
    }

    fun loadShowInfo(mediaId: MediaIdentifier): Single<ShowDetail> {
        return traktApi.show(tokenHolder.httpAuth(), mediaId.id)
            .subscribeOn(rxSchedulers.subscribe)
            .observeOn(rxSchedulers.observe)
            .doOnError(Consumer { Timber.e(it, "loadShowInfo error") })
    }

    fun doCheckin(checkin: CheckinWithTime): Single<CheckinResult> {
        return when (checkin.time) {
            is CheckinTime.Now -> callCheckin(when(checkin.subject) {
                is CheckinSubject.EpisodeCheckin -> CheckinRequest(episode = checkin.subject.episode)
                is CheckinSubject.MovieCheckin -> CheckinRequest(movie = checkin.subject.movie)
            }).map { res ->
                if (res.isSuccessful && res.code() == 409) {
                    CheckinResult.InProgress
                } else if (res.isSuccessful) {
                    CheckinResult.Success
                } else {
                    CheckinResult.Failed
                }
            }
            is CheckinTime.At -> {
                val records = when(checkin.subject) {
                    is CheckinSubject.EpisodeCheckin -> HistoryRecords(
                            episodes = listOf(HistoryRecord(checkin.time.dateTime, checkin.subject.episode.ids))
                    )
                    is CheckinSubject.MovieCheckin -> HistoryRecords(
                            movies = listOf(HistoryRecord(checkin.time.dateTime, checkin.subject.movie.ids))
                    )
                }
                addToHistory(records)
                        .map { res -> if (res.isSuccessful) CheckinResult.Success else CheckinResult.Failed}
            }
        }
    }

    fun loadShowWatchedProgress(showId: Long): Single<ShowWatchedProgress> {
        return traktApi.showWatchedProgress(tokenHolder.httpAuth(), showId, specials = true, countSpecials = false)
                .subscribeOn(rxSchedulers.subscribe)
                .observeOn(rxSchedulers.observe)
                .doOnError({ Timber.e(it, "loadShowWatchedProgress error") })
                .map { t: Response<ShowWatchedProgress> ->
                    if (!t.isSuccessful)
                        throw ApiException("Unable to load watched progress", t.code(), t.message())
                    else t.body()
                }
    }

    fun loadShowSeasons(showId: Long): Single<List<Season>> {
        return loadShowSeasonsInner(showId)
                .observeOn(rxSchedulers.observe)
    }

    /**
     * Loads episodes for seasons and emits a value for each season.
     */
    fun loadEpisodesForSeasons(showId: Long, seasons: List<Season>): Observable<SeasonWithProgress> {
        return Observable.fromIterable(seasons)
                .subscribeOn(rxSchedulers.subscribe)
                .flatMap { s -> Observable.zip(
                        traktApi.showSeasonEpisodes(tokenHolder.httpAuth(), showId, s.number, TraktApi.ExtendedInfo.full).toObservable(),
                        Observable.just(s),
                        BiFunction { t1: Response<List<Episode>>, t2: Season -> Pair(t1, t2) }
                ) }
                //FIXME: remove after testing
//                .concatMap({ i -> Observable.just(i).delay(4, TimeUnit.SECONDS)})
                .map { (response, season) ->
                    if (response.isSuccessful) {
                        SeasonWithProgress(season, response.body()?.map { ep -> EpisodeWithProgress(ep) } ?: emptyList(), episodesLoaded = true)
                    } else {
                        throw ApiException("Unable to load episodes for season $season", response.code(), response.message())
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())

    }

    private fun callCheckin(request: CheckinRequest): Single<Response<CheckinResponse>> {
        return traktApi.checkin(tokenHolder.httpAuth(), request)
            .subscribeOn(rxSchedulers.subscribe)
            .observeOn(rxSchedulers.observe)
            .doOnError({ Timber.e(it, "doCheckin error") })
    }

    private fun addToHistory(request: HistoryRecords): Single<Response<AddHistoryResponse>> {
        return traktApi.addToHistory(tokenHolder.httpAuth(), request)
                .subscribeOn(rxSchedulers.subscribe)
                .observeOn(rxSchedulers.observe)
                .doOnError({ Timber.e(it, "addToHistory error") })
    }



    private fun loadShowSeasonsInner(showId: Long): Single<List<Season>> {
        return traktApi.showSeasons(tokenHolder.httpAuth(), showId, TraktApi.ExtendedInfo.full)
            .subscribeOn(rxSchedulers.subscribe)
            .doOnError({ Timber.e(it, "loadShowSeasonsInner error") })
            .map { response ->
                Timber.d("loadShowSeasonsWithEpisodes - result %s", response.code())
                if (!response.isSuccessful) {
                    throw ApiException("Response is not successful ${response.code()} ${response.message()}", response.code(), response.message())
                }
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
