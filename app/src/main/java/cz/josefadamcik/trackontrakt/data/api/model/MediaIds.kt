
package cz.josefadamcik.trackontrakt.data.api.model

data class MediaIds(
    val trakt: Long,
    val slug: String? = null,
    val imdb: String? = null,
    val tvdb: String? = null,
    val tmdb: String? = null
)