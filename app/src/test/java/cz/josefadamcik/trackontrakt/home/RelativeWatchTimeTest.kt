package cz.josefadamcik.trackontrakt.home

import kxdate.threeten.bp.seconds
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.Month

class RelativeWatchTimeTest {
    private val timeOrigin = LocalDateTime.of(2000, Month.DECEMBER, 1, 12, 30)
    private val timeInSameDayAsOrigin = LocalDateTime.of(2000, Month.DECEMBER, 1, 16, 30)
    private val timeAtStartOfOriginDay = timeOrigin.toLocalDate().atStartOfDay()
    private val timeAtEndOfOriginDay = LocalDateTime.of(2000, Month.DECEMBER, 1, 23, 59, 59)
    private val timeAfterEndOfOriginDay = timeAtEndOfOriginDay + 1.seconds
    private val timeBeforeOriginDay = timeOrigin.toLocalDate().atStartOfDay() - 1.seconds


    @Test
    fun todayIsDateInRelativeRange() {
        //given
        val today = RelativeWatchTime.Today
        //assert
        assertTrue("origin is in today", today.isDateInRelativeRange(time = timeOrigin, relativeToOrigin = timeOrigin))
        assertTrue("other time in today is today", today.isDateInRelativeRange(time = timeInSameDayAsOrigin, relativeToOrigin = timeOrigin))
        assertTrue("begging of day is today", today.isDateInRelativeRange(time = timeAtStartOfOriginDay, relativeToOrigin = timeOrigin))
        assertTrue("end of day is today", today.isDateInRelativeRange(time = timeAtEndOfOriginDay, relativeToOrigin = timeOrigin))
        assertFalse("previous day si not today", today.isDateInRelativeRange(time = timeBeforeOriginDay, relativeToOrigin = timeOrigin))
        assertFalse("next day is not today", today.isDateInRelativeRange(time = timeAfterEndOfOriginDay, relativeToOrigin = timeOrigin))
    }

    @Test
    fun yesterdayIsDateInRelativeRange() {
        //given
        val yesterday = RelativeWatchTime.Yesterday
        //assert
        assertFalse("origin is not in yesterday", yesterday.isDateInRelativeRange(time = timeOrigin, relativeToOrigin = timeOrigin))
        assertFalse("begging of today is nod in yesterday", yesterday.isDateInRelativeRange(time = timeAtStartOfOriginDay, relativeToOrigin = timeOrigin))
        assertFalse("next day is not yesterday", yesterday.isDateInRelativeRange(time = timeAfterEndOfOriginDay, relativeToOrigin = timeOrigin))
        assertTrue("previous day si yesterday", yesterday.isDateInRelativeRange(time = timeBeforeOriginDay, relativeToOrigin = timeOrigin))
    }

    @Test
    fun thisMonthsIsDateInRelativeRange() {
        //given
        val thisMonth = RelativeWatchTime.MonthsInPast(0)

        assertTrue("origin is in thisMonth", thisMonth.isDateInRelativeRange(time = timeOrigin, relativeToOrigin = timeOrigin))
        assertTrue("nextDay is in thisMonth", thisMonth.isDateInRelativeRange(time = timeAfterEndOfOriginDay, relativeToOrigin = timeOrigin))
        assertFalse("previous day is not in thisMonth", thisMonth.isDateInRelativeRange(time = timeBeforeOriginDay, relativeToOrigin = timeOrigin))
    }

    @Test
    fun previousMonthsIsDateInRelativeRange() {
        //given
        val previousMonth = RelativeWatchTime.MonthsInPast(1)

        assertFalse("origin is not in previousMonth", previousMonth.isDateInRelativeRange(time = timeOrigin, relativeToOrigin = timeOrigin))
        assertFalse("nextDay is not in thisMonth", previousMonth.isDateInRelativeRange(time = timeAfterEndOfOriginDay, relativeToOrigin = timeOrigin))
        assertTrue("previous day is in thisMonth", previousMonth.isDateInRelativeRange(time = timeBeforeOriginDay, relativeToOrigin = timeOrigin))
    }


}