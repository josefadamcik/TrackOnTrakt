
package cz.josefadamcik.trackontrakt.detail

import cz.josefadamcik.trackontrakt.data.api.model.EpisodeWithProgress
import cz.josefadamcik.trackontrakt.data.api.model.SeasonWithProgress
import cz.josefadamcik.trackontrakt.data.api.model.ShowWatchedProgress
import cz.josefadamcik.trackontrakt.util.CurrentTimeProvider
import org.threeten.bp.LocalDate
import java.text.SimpleDateFormat
import java.util.*

data class MediaDetailModel(
    val basic: MediaDetailInfo,
    val seasons: List<SeasonWithProgress> = emptyList(),
    val showProgress: ShowWatchedProgress = ShowWatchedProgress()
) {

    val nextShowEpisodeToWatch: Pair<SeasonWithProgress, EpisodeWithProgress>?
        get() {
            if (showProgress.next_episode != null) {
                val episode = seasons.flatMap { it.episodes }.find { ep -> ep.episode.ids.trakt == showProgress.next_episode.ids.trakt }
                if (episode != null) {
                    val season = seasons.find { it.season.number == episode.episode.season }
                    if (season != null) {
                        return Pair(season, episode)
                    }
                }
            }

            return null
        }




    data class MediaDetailInfo(
        val tagline: String? = null,
        val description: String? = null,
        val homepage: String? = null,
        val traktPage: String? = null,
        val rating: Double = 0.0,
        val certification: String? = null,
        val votes: Long = 0,
        val date: LocalDate? = null,
        val network: String? = null,
        val genres: List<String> = emptyList(),
        val trailer: String? = null,
        val status: String? = null,
        val language: String? = null
    ) {


        companion object {
            val YEAR_FORMAT = SimpleDateFormat("yyyy", Locale.getDefault())
        }

        val year: CharSequence get() = if (date == null) "" else YEAR_FORMAT.format(date)

    }

    /**
     * Create new MediaDetailModel instance with it's data modified according to checking request.
     */
    fun copyWithAppliedCheckin(request: CheckinWithTime, currentTimeProvider: CurrentTimeProvider): MediaDetailModel {
        var episodeFoundAndCompletedStatusChanged = false
        if (request.subject is CheckinSubject.EpisodeCheckin ) {
            val requestedEpisode = request.subject.episode
            val modifiedSeasons = seasons.map { season ->
                if (season.season.number == requestedEpisode.season) {
                    val modifiedEpisodes = season.episodes.map { e ->
                        if (e.episode.number == requestedEpisode.number) {
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
                    val completedEpisodes = modifiedEpisodes.filter { e -> e.progress.completed }.count()
                    season.copy(
                            episodes = modifiedEpisodes,
                            progress = season.progress.copy(
                                    completed = completedEpisodes
                            ))
                } else {
                    season
                }
            }



            //get next episode candidate for each season but ignore specials
            val nextEpisodeCandidates = modifiedSeasons
                    .filter { s -> !s.season.isSpecials }
                    .mapNotNull { s -> s.episodes.find { ep -> !ep.progress.completed }?.episode }

            val nextEpisode = nextEpisodeCandidates.firstOrNull()

            val completedInAllSeasons = modifiedSeasons.sumBy { s ->
                s.episodes.sumBy { e -> if (e.progress.completed) 1 else 0 }
            }
            return copy(
                    seasons = modifiedSeasons,
                    showProgress = showProgress.copy(
                            completed = completedInAllSeasons,
                            last_watched_at = currentTimeProvider.dateTime,
                            last_episode = requestedEpisode,
                            next_episode = nextEpisode
                    )
            )
        } else {
            return copy()
        }
    }
}