

package cz.josefadamcik.trackontrakt.data.api.model

data class SearchResultItem(
    val score: Double?,
    override val type: MediaType,
    override val movie: Movie?,
    override val episode: Episode?,
    override val show: Show?
) : MediaItem
