
package cz.josefadamcik.trackontrakt.detail

import cz.josefadamcik.trackontrakt.data.api.model.EpisodeWithProgress
import cz.josefadamcik.trackontrakt.data.api.model.SeasonWithProgress
import cz.josefadamcik.trackontrakt.data.api.model.ShowWatchedProgress
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
        val date: Date? = null,
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
}