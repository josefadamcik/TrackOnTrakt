package cz.josefadamcik.trackontrakt.detail

import cz.josefadamcik.trackontrakt.data.api.model.*
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThat
import org.junit.Test

class MediaDetailModelTest {

    @Test
    fun getNextShowEpisodeToWatch() {
        val seasonNumber = 1
        val epCount = 5
        val episodes = (1..epCount).map { Episode(seasonNumber, it, "ep $it", MediaIds(it.toLong())) }
        val season = Season(seasonNumber, MediaIds(1), title = "test", episodes = episodes)
        val episodesWithProg = episodes.map { EpisodeWithProgress(it, ShowWatchedProgress.EpisodeWatchedProgress(it.number)) }
        val seasonWithProg = SeasonWithProgress(season = season,
                episodes = episodesWithProg,
                progress = ShowWatchedProgress.SeasonWatchedProgress(seasonNumber, epCount, 0, episodesWithProg.map { it.progress })
        )
        val model = MediaDetailModel(
              MediaDetailModel.MediaDetailInfo(),
              seasons = listOf(seasonWithProg),
              showProgress = ShowWatchedProgress(
                      aired = epCount,
                      completed = 0,
                      seasons = listOf(seasonWithProg.progress),
                      last_episode = null,
                      next_episode = episodes.first()

              )
        )

        val nextToWatch = model.nextShowEpisodeToWatch
        assertNotNull(nextToWatch)
        assertThat("nextShowEpisodeToWatch is the from season 0", nextToWatch?.first?.season?.number, equalTo(seasonNumber))
        assertThat("nextShowEpisodeToWatch is the first one episode", nextToWatch?.second?.episode?.number, equalTo(1))

    }

}