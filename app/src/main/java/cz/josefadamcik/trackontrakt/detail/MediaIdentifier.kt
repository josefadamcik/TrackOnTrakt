
package cz.josefadamcik.trackontrakt.detail

import cz.josefadamcik.trackontrakt.data.api.model.MediaItem
import cz.josefadamcik.trackontrakt.data.api.model.MediaType
import paperparcel.PaperParcel
import paperparcel.PaperParcelable


@PaperParcel
data class MediaIdentifier(
    val type: MediaType,
    val id: Long
) : PaperParcelable {
    companion object {
        @JvmField val CREATOR = PaperParcelMediaIdentifier.CREATOR

        public fun fromMediaItem(item: MediaItem): MediaIdentifier {
            val traktId = item.traktId
            if (traktId == null) {
                throw IllegalArgumentException("invalid item $item")
            } else {
                return MediaIdentifier(item.type, traktId)
            }
        }

        public fun fromMediaItemButShowForEpisode(item: MediaItem): MediaIdentifier {
            var traktId = item.traktId
            var type = item.type
            if (item.type == MediaType.episode) {
                traktId = item.show?.ids?.trakt
                type = MediaType.show
            }
            if (traktId == null) {
                throw IllegalArgumentException("invalid item $item")
            } else {
                return MediaIdentifier(type, traktId)
            }
        }


    }


}
