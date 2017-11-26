package cz.josefadamcik.trackontrakt.home

import org.threeten.bp.LocalDateTime


/**
 * It will add separators between history records with time information so
 * the user can easily orientate in his watch history.
 * It expects that the list is ordered by time from the newest to the oldest.
 */
class HistoryListTimeSeparatorAugmenter {
    fun augmentList(list: List<HistoryAdapter.RowItem>): List<HistoryAdapter.RowItem> {
        val now = LocalDateTime.now()
        var currentRelativeTimeHeader: RelativeWatchTime = RelativeWatchTime.Today
        var currentHeaderAddToOutput = false
        val augmentedList = mutableListOf<HistoryAdapter.RowItem>()

        fun tryToConsumeItemUnderCurrentHeader(item: HistoryAdapter.RowItem.HistoryRowItem): Boolean {
            if (currentRelativeTimeHeader.isDateInRelativeRange(time = item.historyItem.watched_at, relativeToOrigin = now)) {
                if (!currentHeaderAddToOutput) {
                    augmentedList.add(HistoryAdapter.RowItem.HeaderRowItem(currentRelativeTimeHeader))
                    currentHeaderAddToOutput = true
                }
                augmentedList.add(item)
                return true
            }
            return false
        }


        list.forEach { item ->
            if (item is HistoryAdapter.RowItem.HistoryRowItem) {
                val watched = item.historyItem.watched_at
                while (!tryToConsumeItemUnderCurrentHeader(item)) {
                    val lastTime = currentRelativeTimeHeader
                    currentRelativeTimeHeader = when (lastTime) {
                        RelativeWatchTime.Today -> RelativeWatchTime.Yesterday
                        RelativeWatchTime.Yesterday -> RelativeWatchTime.MonthsInPast(0)
                        is RelativeWatchTime.MonthsInPast -> RelativeWatchTime.MonthsInPast(lastTime.monthCount + 1)
                    }
                    currentHeaderAddToOutput = false
                }
            }
        }
        return augmentedList
    }





}