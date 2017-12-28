package cz.josefadamcik.trackontrakt.data.api

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import java.util.*

class LocalDateTimeJsonAdapter(private val dateAdapter: JsonAdapter<Date>) : JsonAdapter<LocalDateTime>() {
    override fun fromJson(reader: JsonReader?): LocalDateTime {
        val date = dateAdapter.fromJson(reader)
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(date?.time ?: 0), ZoneOffset.systemDefault())
    }

    override fun toJson(writer: JsonWriter?, value: LocalDateTime?) {
        val instant = value?.toInstant(ZoneOffset.UTC)
        if (instant != null) {
            dateAdapter.toJson(writer, Date(instant.toEpochMilli()))
        } else {
            writer?.nullValue()
        }
    }
}

class LocalDateJsonAdapter(private val dateAdapter: JsonAdapter<Date>) : JsonAdapter<LocalDate>() {
    override fun fromJson(reader: JsonReader?): LocalDate {
        val date = dateAdapter.fromJson(reader)
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(date?.time ?: 0), ZoneOffset.systemDefault()).toLocalDate()
    }

    override fun toJson(writer: JsonWriter?, value: LocalDate?) {
        val instant = value?.atStartOfDay()?.toInstant(ZoneOffset.UTC)
        if (instant != null) {
            dateAdapter.toJson(writer, Date(instant.toEpochMilli()))
        } else {
            writer?.nullValue()
        }
    }
}
