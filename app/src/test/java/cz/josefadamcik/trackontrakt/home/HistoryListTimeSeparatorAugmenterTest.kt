package cz.josefadamcik.trackontrakt.home

import cz.josefadamcik.trackontrakt.data.api.model.*
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.greaterThan
import org.junit.Assert.assertThat
import org.junit.Test
import org.threeten.bp.LocalDate

class HistoryListTimeSeparatorAugmenterTest {
    @Test
    fun augmentListWithDailyRecords() {
        //given some history list with a record for each day for 90 days back
        val today = LocalDate.now().atStartOfDay()
        val list = (0..89).map {
            HistoryItem(
                id = it.toLong(),
                watched_at = today.minusDays(it.toLong()),
                action = Action.watch,
                type = MediaType.movie,
                movie = Movie("movie $it", 2000, MediaIds(it.toLong())))
        }.map { HistoryAdapter.RowItem.HistoryRowItem(it) }

        val augmenter = HistoryListTimeSeparatorAugmenter()

        //act -> augment list
        val resultList = augmenter.augmentList(list)

        //assert
        assertThat("new list is longer than original", resultList.size, greaterThan(list.size))

        //there should be separators in particular places in the list
        assertThat("first item is 'today' separator", resultList.first(), isHeaderRowWithRelativeTime(RelativeWatchTime.Today))
        assertThat("second item is a history record", resultList[1], isHistoryRow())
        assertThat("next item is 'yesterday' separator", resultList[2], isHeaderRowWithRelativeTime(RelativeWatchTime.Yesterday))
        assertThat("next item is a history record", resultList[3], isHistoryRow())
        assertThat("next item is 'this month' separator", resultList[4], isHeaderRowWithRelativeTime(RelativeWatchTime.MonthsInPast(0)))


    }

    private fun isHistoryRow(): Matcher<in HistoryAdapter.RowItem>? {
        return object : BaseMatcher<HistoryAdapter.RowItem>() {
            override fun matches(item: Any?): Boolean {
                return item is HistoryAdapter.RowItem.HistoryRowItem
            }

            override fun describeTo(description: Description?) {
                description?.appendText("is history row")
            }
        }
    }

    private fun isHeaderRowWithRelativeTime(time: RelativeWatchTime): Matcher<in HistoryAdapter.RowItem> {
        return object : BaseMatcher<HistoryAdapter.RowItem>() {
            override fun matches(item: Any?): Boolean {
                return item is HistoryAdapter.RowItem.HeaderRowItem && item.time == time
            }

            override fun describeTo(description: Description?) {
                description?.appendText("is header row with relative time")?.appendValue(time)
            }
        }
    }

}