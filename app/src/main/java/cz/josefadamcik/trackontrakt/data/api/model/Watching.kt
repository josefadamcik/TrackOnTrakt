
package cz.josefadamcik.trackontrakt.data.api.model

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

        override fun isExpired(): Boolean {
            return expires_at.isBefore(LocalDateTime.now())
        }
    }

    object Nothing : Watching()

    open fun isExpired(): Boolean {
        return false
    }
}

