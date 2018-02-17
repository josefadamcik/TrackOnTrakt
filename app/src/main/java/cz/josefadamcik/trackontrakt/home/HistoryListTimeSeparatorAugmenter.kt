package cz.josefadamcik.trackontrakt.home

import cz.josefadamcik.trackontrakt.util.CurrentTimeProvider


/**
 * It will add separators between history records with time information so
 * the user can easily orientate in his watch history.
 * It expects that the list is ordered by time from the newest to the oldest.
 */
class HistoryListTimeSeparatorAugmenter(private val currentTimeProvider: CurrentTimeProvider) {
    fun augmentList(list: List<RowItemModel>): MutableList<RowItemModel> {
        val now = currentTimeProvider.dateTime
        var currentRelativeTimeHeader: RelativeWatchTime = RelativeWatchTime.Today
        var currentHeaderAddToOutput = false
        val augmentedList = mutableListOf<RowItemModel>()

        fun tryToConsumeItemUnderCurrentHeader(item: RowItemModel.HistoryRowItem): Boolean {
            if (currentRelativeTimeHeader.isDateInRelativeRange(time = item.historyItem.watched_at, relativeToOrigin = now)) {
                if (!currentHeaderAddToOutput) {
                    augmentedList.add(RowItemModel.HeaderRowItem(currentRelativeTimeHeader))
                    currentHeaderAddToOutput = true
                }
                augmentedList.add(item)
                return true
            }
            return false
        }


        list.forEach { item ->
            if (item is RowItemModel.HistoryRowItem) {
                while (!tryToConsumeItemUnderCurrentHeader(item)) {
                    val lastTime = currentRelativeTimeHeader
                    currentRelativeTimeHeader = when (lastTime) {
                        RelativeWatchTime.Now -> RelativeWatchTime.Today //not really used
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