package cz.josefadamcik.trackontrakt.home

import org.threeten.bp.LocalDateTime
import org.threeten.bp.Month

/**
 * Represents time in history in relative manner.
 * We are starting with simple today/yesterday and than months.
 */
sealed class RelativeWatchTime {
    /**
     * @param time - the time is question
     * @param relativeToOrigin - origin for relative time computation (current time in most cases).
     * @return true when time parameter lies in a time interval specified by this value in realation to the relativeToOrigin
     */
    abstract fun isDateInRelativeRange(time: LocalDateTime, relativeToOrigin: LocalDateTime): Boolean

    protected fun isInHalfOpenInterval(time: LocalDateTime, start: LocalDateTime, endExclusive: LocalDateTime) =
        time.isEqual(start) || (time.isAfter(start) && time.isBefore(endExclusive))

    object Today : RelativeWatchTime() {
        override fun isDateInRelativeRange(time: LocalDateTime, relativeToOrigin: LocalDateTime): Boolean {
            val start = relativeToOrigin.toLocalDate().atStartOfDay()
            val endExclusive = start.plusDays(1)
            return isInHalfOpenInterval(time, start, endExclusive)
        }

        override fun toString(): String = "Today"
    }

    object Yesterday : RelativeWatchTime() {
        override fun isDateInRelativeRange(time: LocalDateTime, relativeToOrigin: LocalDateTime): Boolean {
            val start = relativeToOrigin.toLocalDate().minusDays(1).atStartOfDay()
            val endExclusive = start.plusDays(1)
            return isInHalfOpenInterval(time, start, endExclusive)
        }

        override fun toString(): String = "Yesterday"
    }

    class MonthsInPast(val monthCount: Int) : RelativeWatchTime() {
        override fun isDateInRelativeRange(time: LocalDateTime, relativeToOrigin: LocalDateTime): Boolean {
            val start = relativeToOrigin.toLocalDate().withDayOfMonth(1).minusMonths(monthCount.toLong())
            val endExclusive = start.plusMonths(1)
            val date = time.toLocalDate()
            return date.isEqual(start) || (date.isAfter(start) && date.isBefore(endExclusive))
        }

        override fun toString(): String = "MonthsInPast($monthCount)"
        override fun equals(other: Any?): Boolean {
            return other is MonthsInPast && monthCount == other.monthCount
        }

        override fun hashCode(): Int = monthCount

        fun toMonth(relativeToOrigin: LocalDateTime): Month {
            return relativeToOrigin.minusMonths(monthCount.toLong()).month
        }

    }

    object Now : RelativeWatchTime() {
        override fun isDateInRelativeRange(time: LocalDateTime, relativeToOrigin: LocalDateTime): Boolean {
            return false
        }

    }


}