package cz.josefadamcik.trackontrakt.data.api.model

data class AddHistoryResponseStats(
        val movies: Int?,
        val episodes: Int?
)
data class AddHistoryNotFound(
    val movies: List<MediaIds>,
    val shows: List<MediaIds>,
    val episodes: List<MediaIds>
)

data class AddHistoryResponse(
        val added : AddHistoryResponseStats,
        val not_found: AddHistoryNotFound
)