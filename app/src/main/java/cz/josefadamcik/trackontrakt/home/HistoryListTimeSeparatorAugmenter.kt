package cz.josefadamcik.trackontrakt.home

import kxdate.threeten.bp.days
import org.threeten.bp.LocalDate


/**
 * It will add separators between history records with time information so
 * the user can easily orientate in his watch history.
 */
class HistoryListTimeSeparatorAugmenter {
    fun augmentList(list: List<HistoryAdapter.RowItem>): List<HistoryAdapter.RowItem> {

        val today = LocalDate.now()
        var until = today.atStartOfDay()
        var period = 1.days
        val augmentedList = mutableListOf<HistoryAdapter.RowItem>()
        list.forEach { item ->
            if (item is HistoryAdapter.RowItem.HistoryRowItem) {
                val watched = item.historyItem.watched_at
                if (watched.isBefore(until) && watched.isAfter(until - period)) {
                    augmentedList.add(HistoryAdapter.RowItem.HeaderRowItem(period))
                }


            }
            augmentedList.add(item)
        }
        return list
    }


}