
package cz.josefadamcik.trackontrakt.data.api.model

import java.util.*

data class MovieDetail(
    val title: String,
    val ids: MediaIds,
    val year: Int?,
    val tagline: String?,
    val overview: String?,
    val released: Date?,
    val runtime: Int?,
    val updated_at: Date?,
    val trailer: String?,
    val homepage: String?,
    val rating: Double?,
    val votes: Long?,
    val language: String?,
    val genres: List<String>,
    val certification: String?

)