package cz.josefadamcik.trackontrakt.util

import org.threeten.bp.Clock
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class CurrentTimeProviderImpl(private val clock: Clock = Clock.systemDefaultZone()) : CurrentTimeProvider {
    override val dateTime: LocalDateTime get() = LocalDateTime.now(clock)
    override val date: LocalDate get() = LocalDate.now(clock)
    override val time: LocalTime get() = LocalTime.now(clock)
}




