
package cz.josefadamcik.trackontrakt.data.api.model

import java.util.*

data class ShowWatchedProgress(
    val aired: Int = 0,
    val completed: Int = 0,
    val last_watched_at: Date? = null,
    val seasons: List<SeasonWatchedProgress> = emptyList(),
    val next_episode: Episode? = null,
    val last_episode: Episode? = null
) {

    data class SeasonWatchedProgress(
        val number: Int = 0,
        val aired: Int = 0,
        val completed: Int = 0,
        val episodes: List<EpisodeWatchedProgress> = emptyList()
    )

    data class EpisodeWatchedProgress(
        val number: Int = 0,
        val completed: Boolean = false,
        val last_watched_at: Date? = null
    )
}
