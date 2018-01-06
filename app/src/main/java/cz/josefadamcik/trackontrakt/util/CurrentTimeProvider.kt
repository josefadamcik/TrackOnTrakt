package cz.josefadamcik.trackontrakt.util

import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

interface CurrentTimeProvider {
    val dateTime: LocalDateTime
    val date: LocalDate
    val time: LocalTime
}