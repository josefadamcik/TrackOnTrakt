package cz.josefadamcik.trackontrakt.data.api.model

import org.threeten.bp.LocalDateTime

data class HistoryRecords(
    val movies: List<HistoryRecord> = emptyList(),
    val episodes: List<HistoryRecord> = emptyList(),
    val shows: List<HistoryRecord> = emptyList()
)

data class HistoryRecord(
        val watched_at: LocalDateTime,
        val ids: MediaIds
)