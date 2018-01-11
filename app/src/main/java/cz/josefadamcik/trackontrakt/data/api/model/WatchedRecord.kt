
package cz.josefadamcik.trackontrakt.data.api.model

import org.threeten.bp.LocalDateTime

data class WatchedRecord(
    val watched_at: LocalDateTime,
    val ids: MediaIds
)