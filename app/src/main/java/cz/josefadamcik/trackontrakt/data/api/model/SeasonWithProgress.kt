
package cz.josefadamcik.trackontrakt.data.api.model


data class SeasonWithProgress(
    val season: Season,
    val episodes: List<EpisodeWithProgress> = emptyList(),
    val progress: ShowWatchedProgress.SeasonWatchedProgress = ShowWatchedProgress.SeasonWatchedProgress()
)


