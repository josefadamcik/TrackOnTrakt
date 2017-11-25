package cz.josefadamcik.trackontrakt.home

import cz.josefadamcik.trackontrakt.data.api.model.HistoryItem

data class HistoryModel(
    val items: List<HistoryItem> = emptyList(),
    val hasNextPage: Boolean = false,
    val loadingNextPage: Boolean = false
)