
package cz.josefadamcik.trackontrakt.data.api.model

import java.util.*

data class ShowDetail(
    val title: String,
    val ids: MediaIds,
    val year: Int?,
    val first_aired: Date?,
    val overview: String?,
    //val airs:
    val runtime: Int?,
    val network: String?,
    val country: String?,
    val updated_at: Date?,
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

