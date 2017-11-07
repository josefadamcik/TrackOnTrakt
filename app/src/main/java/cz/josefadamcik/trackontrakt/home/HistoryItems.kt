package cz.josefadamcik.trackontrakt.home

import cz.josefadamcik.trackontrakt.data.api.model.HistoryItem

data class HistoryItems(
    val items: List<HistoryItem>,
    val pageCount: Int,
    val page: Int,
    val itemCount: Int

)