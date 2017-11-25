
package cz.josefadamcik.trackontrakt.data.api.model

import java.util.*

class CheckinResponse(
    val id: Long,
    val watched_ad: Date,
    val sharing: CheckinRequest.Sharing?,
    val movie: Movie? = null,
    val episode: Episode? = null,
    val show: Show? = null
)