package cz.josefadamcik.trackontrakt.detail

import cz.josefadamcik.trackontrakt.data.api.model.*
import cz.josefadamcik.trackontrakt.util.CurrentTimeProviderImpl
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.*
import org.junit.Test

class MediaDetailModelTest {

    @Test
    fun getNextShowEpisodeToWatch() {
        val epCount = 5
        val completedEpCount = 0
        val seasonNumber = 1
        val (episodes, seasonWithProg) = createSeasonWithProg(seasonNumber, epCount, completedEpCount)
        val model = MediaDetailModel(
              MediaDetailModel.MediaDetailInfo(),
              seasons = listOf(seasonWithProg),
              showProgress = ShowWatchedProgress(
                      aired = epCount,
                      completed = completedEpCount,
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

    @Test
    fun copyWithAppliedCheckinMiddleOfSeason() {
        val epCount = 5
        val completedEpCount = 3
        val (episodes, seasonWithProg) = createSeasonWithProg(1, epCount, completedEpCount)
        val (episodes2, season2Withprog) = createSeasonWithProg(2, epCount, 0)
        val nextEpisode = episodes[completedEpCount]
        var checkedInEpisodeIndex = completedEpCount
        val model = MediaDetailModel(
                MediaDetailModel.MediaDetailInfo(),
                seasons = listOf(seasonWithProg, season2Withprog),
                showProgress = ShowWatchedProgress(
                        aired = epCount * 2,
                        completed = completedEpCount,
                        seasons = listOf(seasonWithProg.progress, season2Withprog.progress),
                        last_episode = null,
                        next_episode = nextEpisode

                )
        )

        val currentTimeProvider = CurrentTimeProviderImpl()

        val checkinWithTime = CheckinWithTime(CheckinSubject.EpisodeCheckin(nextEpisode), CheckinTime.Now)

        val modelAfterCheckin = model.copyWithAppliedCheckin(checkinWithTime, currentTimeProvider)



        assertThat("completed count for model is incremented", modelAfterCheckin.showProgress.completed, equalTo(completedEpCount + 1))
        assertThat("completed count for season is incremented", modelAfterCheckin.seasons[0].progress.completed, equalTo(completedEpCount + 1))

        val checkedInEpisode =  modelAfterCheckin.seasons[0].episodes[checkedInEpisodeIndex]
        assertTrue("checked-in episode is completed", checkedInEpisode.progress.completed)

        assertThat("Next episode season is still the same", modelAfterCheckin.showProgress.next_episode?.season , equalTo(checkedInEpisode.episode.season))
        assertThat("Next episode number is correctly updated", modelAfterCheckin.showProgress.next_episode?.number , equalTo(checkedInEpisode.episode.number + 1))
    }


    @Test
    fun copyWithAppliedCheckinEndOfSeason() {
        val epCount = 5
        val completedEpCount = epCount - 1
        val (episodes, seasonWithProg) = createSeasonWithProg(1, epCount, completedEpCount)
        val (episodes2, season2Withprog) = createSeasonWithProg(2, epCount, 0)
        val nextEpisode = episodes[completedEpCount]
        var checkedInEpisodeIndex = completedEpCount
        val model = MediaDetailModel(
                MediaDetailModel.MediaDetailInfo(),
                seasons = listOf(seasonWithProg, season2Withprog),
                showProgress = ShowWatchedProgress(
                        aired = epCount * 2,
                        completed = completedEpCount,
                        seasons = listOf(seasonWithProg.progress, season2Withprog.progress),
                        last_episode = null,
                        next_episode = nextEpisode

                )
        )

        val currentTimeProvider = CurrentTimeProviderImpl()

        val checkinWithTime = CheckinWithTime(CheckinSubject.EpisodeCheckin(nextEpisode), CheckinTime.Now)

        val modelAfterCheckin = model.copyWithAppliedCheckin(checkinWithTime, currentTimeProvider)



        val checkedInEpisode =  modelAfterCheckin.seasons[0].episodes[checkedInEpisodeIndex]
        assertTrue("checked-in episode is completed", checkedInEpisode.progress.completed)

        assertThat("Next episode season is correctly incremented", modelAfterCheckin.showProgress.next_episode?.season , equalTo(2))
        assertThat("Next episode number is correctly updated", modelAfterCheckin.showProgress.next_episode?.number , equalTo(1))
    }


    private fun createSeasonWithProg(seasonNumber: Int, epCount: Int, completedEpCount: Int): Pair<List<Episode>, SeasonWithProgress> {
        val episodes = (1..epCount).map { Episode(seasonNumber, it, "ep $it", MediaIds(it.toLong())) }
        val season = Season(seasonNumber, MediaIds(1), title = "test", episodes = episodes)
        val episodesWithProg = mapEpisodesToEpisodesWProg(episodes, completedEpCount)
        val seasonWithProg = SeasonWithProgress(
                season = season,
                episodes = episodesWithProg,
                progress = ShowWatchedProgress.SeasonWatchedProgress(seasonNumber, epCount, completedEpCount, episodesWithProg.map { it.progress })
        )
        return Pair(episodes, seasonWithProg)
    }

    private fun mapEpisodesToEpisodesWProg(episodes: List<Episode>, completedEpCount: Int) =
            episodes.map { EpisodeWithProgress(it, ShowWatchedProgress.EpisodeWatchedProgress(it.number, completedEpCount >= it.number)) }



}