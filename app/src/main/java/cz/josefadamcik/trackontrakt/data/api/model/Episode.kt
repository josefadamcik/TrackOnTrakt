
package cz.josefadamcik.trackontrakt.data.api.model

import java.util.*

data class Episode(
    val season: Int,
    val number: Int,
    val title: String,
    val ids: MediaIds,
    val overview: String? = null,
    val rating: Float? = 0.0f,
    val votes: Int? = 0,
    val first_aired: Date? = null,
    val runtime: Int? = 0
)
