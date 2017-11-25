
package cz.josefadamcik.trackontrakt.data.api.model

import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

data class ShowDetail(
    val title: String,
    val ids: MediaIds,
    val year: Int?,
    val first_aired: LocalDate?,
    val overview: String?,
    //val airs:
    val runtime: Int?,
    val network: String?,
    val country: String?,
    val updated_at: LocalDateTime?,
    val trailer: String?,
    val homepage: String?,
    val status: String?,
    val rating: Double?,
    val votes: Long?,
    val language: String?,
    val genres: List<String>,
    val certification: String?,
    val aired_episodes: Int?
)

