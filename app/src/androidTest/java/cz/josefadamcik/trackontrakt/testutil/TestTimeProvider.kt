package cz.josefadamcik.trackontrakt.testutil

import cz.josefadamcik.trackontrakt.util.CurrentTimeProvider
import org.threeten.bp.Clock
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime


object TestTimeProvider : CurrentTimeProvider {
    public var clock: Clock = Clock.systemDefaultZone()
    override val dateTime: LocalDateTime get() = LocalDateTime.now(clock)
    override val date: LocalDate get() = LocalDate.now(clock)
    override val time: LocalTime get() = LocalTime.now(clock)
}