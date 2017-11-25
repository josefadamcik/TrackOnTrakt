
package cz.josefadamcik.trackontrakt.data.api.model

import java.util.*

data class Watching(
    val expires_at: Date,
    val started_at: Date,
    val action: String,
    override val type: MediaType,
    override val movie: Movie?,
    override val show: Show?,
    override val episode: Episode?
) : MediaItem