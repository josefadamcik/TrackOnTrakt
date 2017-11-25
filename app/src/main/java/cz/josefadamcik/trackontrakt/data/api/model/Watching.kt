
package cz.josefadamcik.trackontrakt.data.api.model

import org.threeten.bp.LocalDateTime


data class Watching(
    val expires_at: LocalDateTime,
    val started_at: LocalDateTime,
    val action: String,
    override val type: MediaType,
    override val movie: Movie?,
    override val show: Show?,
    override val episode: Episode?
) : MediaItem