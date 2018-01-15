package cz.josefadamcik.trackontrakt.data.api.model

import org.threeten.bp.LocalDate


data class Season(
    val number: Int,
    val ids: MediaIds,
    val rating: Float? = 0.0f,
    val votes: Int? = 0,
    val aired_episodes: Int? = 0,
    val title: String? = null,
    val overview: String? = null,
    val first_aired: LocalDate? = null,
    val network: String? = null,
    val episodes: List<Episode>? = emptyList()
) {
    val isSpecials: Boolean get() = number == 0
}


