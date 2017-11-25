
package cz.josefadamcik.trackontrakt.data.api.model


import java.util.*

data class HistoryItem(
    val id: Long,
    val watched_at: Date,
    val action: Action,
    override val type: MediaType,
    override val movie: Movie? = null,
    override val episode: Episode? = null,
    override val show: Show? = null
) : MediaItem
