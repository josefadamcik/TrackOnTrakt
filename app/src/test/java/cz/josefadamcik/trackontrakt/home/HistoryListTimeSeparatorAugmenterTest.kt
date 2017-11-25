package cz.josefadamcik.trackontrakt.home

import cz.josefadamcik.trackontrakt.data.api.model.*
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.greaterThan
import org.junit.Assert.assertThat
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.Period

class HistoryListTimeSeparatorAugmenterTest {
    @Test
    fun augmentListWithDailyRecords() {
        //given some history list with a record for each day for 90 days back
        val today = LocalDate.now().atStartOfDay()
        val list = (1..90).map {
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
        assertThat("first item is 'today' separator",
            resultList.first(),
            allOf(
                isHeaderRowWithDate(Period.ofDays(1))
            )
        )
    }

    private fun isHeaderRowWithDate(date: Period): Matcher<in HistoryAdapter.RowItem> {
        return object : BaseMatcher<HistoryAdapter.RowItem>() {
            override fun matches(item: Any?): Boolean {
                return item is HistoryAdapter.RowItem.HeaderRowItem && item.time == date
            }

            override fun describeTo(description: Description?) {
                description?.appendText("is header row with day")?.appendValue(date)
            }
        }
    }

}