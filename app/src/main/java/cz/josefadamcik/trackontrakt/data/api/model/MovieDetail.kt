
package cz.josefadamcik.trackontrakt.data.api.model

import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

data class MovieDetail(
    val title: String,
    val ids: MediaIds,
    val year: Int?,
    val tagline: String?,
    val overview: String?,
    val released: LocalDate?,
    val runtime: Int?,
    val updated_at: LocalDateTime?,
    val trailer: String?,
    val homepage: String?,
    val rating: Double?,
    val votes: Long?,
    val language: String?,
    val genres: List<String>,
    val certification: String?

)