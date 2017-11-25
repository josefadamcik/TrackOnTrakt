
package cz.josefadamcik.trackontrakt.search

import paperparcel.PaperParcel
import paperparcel.PaperParcelable

@PaperParcel
data class TraktFilter(
    val movies: Boolean,
    val shows: Boolean
): PaperParcelable {
    companion object {
        @JvmField val CREATOR = PaperParcelTraktFilter.CREATOR
    }

    fun forApiQuery(): String {
        if (!movies && !shows) {
            throw IllegalArgumentException("at least on of movies, shows should be true")
        }
        val types = mutableListOf<String>()
        if (movies) types.add("movie")
        if (shows) types.add("show")
        return types.joinToString(",")
    }
}