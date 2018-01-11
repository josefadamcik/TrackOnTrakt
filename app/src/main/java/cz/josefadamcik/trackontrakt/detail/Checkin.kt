package cz.josefadamcik.trackontrakt.detail

import cz.josefadamcik.trackontrakt.data.api.model.Episode
import cz.josefadamcik.trackontrakt.data.api.model.Movie
import org.threeten.bp.LocalDateTime


data class CheckinWithTime(val subject: CheckinSubject, val time: CheckinTime)

sealed class CheckinSubject {
    data class EpisodeCheckin(val episode: Episode) : CheckinSubject()
    data class MovieCheckin(val movie: Movie) : CheckinSubject()
}
/**
 */
sealed class CheckinTime {
    object Now: CheckinTime()
    data class At(val dateTime: LocalDateTime) : CheckinTime()
}

sealed class CheckinResult() {
    object Success: CheckinResult()
    object Failed: CheckinResult()
    object InProgress: CheckinResult()
}
