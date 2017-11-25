
package cz.josefadamcik.trackontrakt.data.api.model

data class EpisodeWithProgress(
    val episode: Episode,
    val progress: ShowWatchedProgress.EpisodeWatchedProgress = ShowWatchedProgress.EpisodeWatchedProgress()
)