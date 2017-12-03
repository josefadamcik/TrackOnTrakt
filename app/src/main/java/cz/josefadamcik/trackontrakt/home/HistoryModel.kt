package cz.josefadamcik.trackontrakt.home

import cz.josefadamcik.trackontrakt.data.api.model.HistoryItem
import cz.josefadamcik.trackontrakt.data.api.model.Watching

data class HistoryModel(
    val items: List<HistoryItem> = emptyList(),
    val watching: Watching = Watching.Nothing,
    val hasNextPage: Boolean = false,
    val loadingNextPage: Boolean = false
) {

}