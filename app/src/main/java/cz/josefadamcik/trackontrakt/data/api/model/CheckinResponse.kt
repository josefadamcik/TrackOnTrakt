
package cz.josefadamcik.trackontrakt.data.api.model

import org.threeten.bp.LocalDateTime

class CheckinResponse(
    val id: Long,
    val watched_ad: LocalDateTime,
    val sharing: CheckinRequest.Sharing?,
    val movie: Movie? = null,
    val episode: Episode? = null,
    val show: Show? = null
)