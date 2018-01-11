
package cz.josefadamcik.trackontrakt.data.api.model

import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

data class MovieDetail(
    val title: String,
    val ids: MediaIds,
    val year: Int?,
    val tagline: String? = null,
    val overview: String? = null,
    val released: LocalDate? = null,
    val runtime: Int? = null,
    val updated_at: LocalDateTime? = null,
    val trailer: String? = null,
    val homepage: String? = null,
    val rating: Double? = null,
    val votes: Long? = null,
    val language: String? = null,
    val genres: List<String> = emptyList(),
    val certification: String? = null

)