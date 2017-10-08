package cz.josefadamcik.trackontrakt.data.api.model

import java.util.*

data class Season(
    val number: Int,
    val ids: MediaIds,
    val rating: Float?,
    val votes: Int?,
    val aired_episodes: Int?,
    val title: String?,
    val overview: String?,
    val first_aired: Date?,
    val network: String?,
    val episodes: List<Episode>?
)