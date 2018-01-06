
package cz.josefadamcik.trackontrakt.data.api.model

import cz.josefadamcik.trackontrakt.util.CurrentTimeProvider
import org.threeten.bp.LocalDateTime

sealed class Watching {
    data class Something(
        val expires_at: LocalDateTime,
        val started_at: LocalDateTime,
        val action: String,
        override val type: MediaType,
        override val movie: Movie? = null,
        override val show: Show? = null,
        override val episode: Episode? = null
    ) : Watching(), MediaItem {

        override fun isExpired(currentTimeProvider: CurrentTimeProvider): Boolean {
            return expires_at.isBefore(currentTimeProvider.dateTime)
        }
    }

    object Nothing : Watching()

    open fun isExpired(currentTimeProvider: CurrentTimeProvider): Boolean {
        return false
    }
}

