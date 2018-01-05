package cz.josefadamcik.trackontrakt.data.api.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDateTime

class MediaItemTest {

    @Test
    fun isSameMediaItemTest() {
        val movieA = Movie("movie_a", 1997, MediaIds(1))
        val movieB = Movie("movie_b", 1992, MediaIds(2))
        val historyItemA = HistoryItem(1L, LocalDateTime.now(), Action.checkin, MediaType.movie, movieA)
        val historyItemB = HistoryItem(1L, LocalDateTime.now(), Action.checkin, MediaType.movie, movieB)
        val historyItemC = HistoryItem(1L, LocalDateTime.now(), Action.checkin, MediaType.movie, movieB)

        val showA = Show("show a", 2000, MediaIds(1))
        val episodeA = Episode(1, 1, "episode a", MediaIds(1))
        val episodeB = Episode(1, 2, "episode b", MediaIds(2))

        val showHistoryItemA = HistoryItem(1L, LocalDateTime.now(), Action.checkin, MediaType.episode, episode = episodeA, show = showA)
        val showHistoryItemB = HistoryItem(1L, LocalDateTime.now(), Action.checkin, MediaType.episode, episode = episodeB, show = showA)

        assertFalse("different movies are not the same media", historyItemA.isSameMediaItem(historyItemB))
        assertTrue("the same movies are the same media", historyItemB.isSameMediaItem(historyItemC))

        assertFalse("movie is not the same media as show/episode", showHistoryItemA.isSameMediaItem(historyItemA))
        assertFalse("different episodes of one show are not the same media", showHistoryItemB.isSameMediaItem(showHistoryItemA))
    }
}