
package cz.josefadamcik.trackontrakt.data.api.model

interface MediaItem {
    val type: MediaType
    val movie: Movie?
    val episode: Episode?
    val show: Show?

    val title: String
        get() {
            return when (type) {
                MediaType.episode -> String.format("%s - S %s, Ep %s", show?.title, episode?.season, episode?.number)
                MediaType.show -> show?.title ?: ""
                MediaType.movie -> movie?.title ?: ""
            }
        }

    val subtitle: String
        get() {
            return when (type) {
                MediaType.episode -> episode?.title ?: ""
                else -> ""
            }
        }

    val year: Int?
        get() {
            return when (type) {
                MediaType.episode -> show?.year
                MediaType.show -> show?.year
                MediaType.movie -> movie?.year
            }
        }
    val traktId: Long?
        get() {
            return when (type) {
                MediaType.episode -> episode?.ids?.trakt
                MediaType.show -> show?.ids?.trakt
                MediaType.movie -> movie?.ids?.trakt
            }
        }
}
