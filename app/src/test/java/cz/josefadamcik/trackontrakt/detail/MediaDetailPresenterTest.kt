
package cz.josefadamcik.trackontrakt.detail

import com.nhaarman.mockito_kotlin.*
import cz.josefadamcik.trackontrakt.data.api.model.*
import cz.josefadamcik.trackontrakt.util.CurrentTimeProviderImpl
import io.reactivex.Single
import junit.framework.Assert.assertTrue
import kxdate.threeten.bp.ago
import kxdate.threeten.bp.days
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Test

class MediaDetailPresenterTest {

    companion object {
        const val testSeasonNumber = 1
        const val testSeasonEpisodeCount = 5
        const val testSeasonWatchedEpisodes = 3
    }

    private lateinit var mediaManager: MediaDetailManager
    private lateinit var presenter: MediaDetailPresenter


    @Test
    fun checkinEpisodeTest() {
        //given

        val seasonEpisodes = givenEpisodesForSeason(testSeasonEpisodeCount, testSeasonNumber)
        val season = givenSeasonWithProgress(seasonEpisodes)
        val episodeToCheckIn = season.episodes.first();

        val presenter = givenPresenterInstanceWithMockedDataService({
            on { doCheckin(any()) } doReturn Single.just<CheckinResult>(CheckinResult.Success)
        })

        val view = mock<MediaDetailView>()
        presenter.view = view

        // inject model into presenter -> we will observe if the model changed
        presenter.model = givenMediaDetailModel(season, seasonEpisodes)

        //when
        presenter.checkinActionClicked(episodeToCheckIn)
        verify(view).showCheckinDialog(any())

        presenter.checkinConfirmed(CheckinTime.Now)

        //then
        argumentCaptor<CheckinWithTime>().apply {
            verify(mediaManager).doCheckin(capture())
            assertTrue("is checkin for episode", lastValue.subject is CheckinSubject.EpisodeCheckin)
            assertTrue("is checkin for now", lastValue.time is CheckinTime.Now)
        }

        verify(view).showLoading()
        verify(view).hideLoading()

        verify(view).showCheckinSuccess()
        argumentCaptor<List<RowItemModel>>().apply {
            verify(view).showMedia(capture())

            val model = presenter.model
            val modelEpisodeRow = lastValue.find { it is RowItemModel.EpisodeRowItem
                    &&  it.episodeWithProgress.episode.ids.trakt == episodeToCheckIn.episode.ids.trakt}
                    as RowItemModel.EpisodeRowItem?
            assertThat("checked episode exists in model", modelEpisodeRow, notNullValue())
            assertThat("checked episode in model is watched", modelEpisodeRow?.episodeWithProgress?.progress?.completed ?: false, equalTo(true))
            assertThat("number of watched episodes is increased", model?.showProgress?.completed, equalTo(1))
            assertThat("the last watched episode should be changed to next the first (number 1)", model?.showProgress?.last_episode?.number, equalTo(1))
            assertThat("next to watch episode should be changed to number 2", model?.showProgress?.next_episode?.number, equalTo(2))
            assertThat("nextShowEpisodeToWatch episode should return ep number 2", model?.nextShowEpisodeToWatch?.second?.episode?.number, equalTo(2))
        }

    }



    @Test
    fun checkinMovieTest() {
        //given
        val movieToCheckin = MovieDetail("movie", MediaIds(1), 1997)
        val presenter = givenPresenterInstanceWithMockedDataService({
            on { doCheckin(any()) } doReturn  Single.just(CheckinResult.Success as CheckinResult)
        })
        val view = mock<MediaDetailView>()
        presenter.view = view
        // inject model into presenter -> we will observe if the model changed
        presenter.model = MediaDetailModel(MediaDetailModel.MediaDetailInfo())
        presenter.movieDetail = movieToCheckin

        //when
        presenter.checkinActionClicked()
        presenter.checkinConfirmed(CheckinTime.Now)

        // then
        verify(view).showCheckinDialog(any())
        argumentCaptor<CheckinWithTime>().apply {
            verify(mediaManager).doCheckin(capture())
            assertTrue("is checkin for movie", lastValue.subject is CheckinSubject.MovieCheckin)
            assertTrue("is checkin for now", lastValue.time is CheckinTime.Now)
        }

        verify(view).showLoading()
        verify(view).hideLoading()
        verify(view).showCheckinSuccess()
        argumentCaptor<List<RowItemModel>>().apply {
            verify(view).showMedia(capture())
        }

    }

    @Test
    fun combineSeasonDataWithProgressTest() {
        //arrange / given
        val presenter = givenPresenterInstanceWithMockedDataService({ })
        val seasonEpisodes = givenEpisodesForSeason(testSeasonEpisodeCount, testSeasonNumber)
        val seasonsWithoutProgress = listOf(arrangeSeasonWithProgress(testSeasonNumber, seasonEpisodes))
        val watchedProgress = givenShowWatchedProgress(seasonEpisodes, testSeasonWatchedEpisodes, testSeasonNumber, testSeasonEpisodeCount)

        //act / when
        val result = presenter.combineSeasonDataWithProgress(seasonsWithoutProgress, watchedProgress)

        //assert / then
        assertThat("result is not null", result, notNullValue())
        assertThat("there is a season in result", result.size, equalTo(1))
        result.first().let { swp ->
            assertThat("there are episodes in the season", swp.episodes.size, equalTo(testSeasonEpisodeCount))
            assertThat("season has correct number of completed episodes in its progress", swp.progress.completed, equalTo(testSeasonWatchedEpisodes))
            assertThat("first $testSeasonWatchedEpisodes are watched", swp.episodes.map { it.progress.completed }, equalTo(listOf(true, true, true, false, false)))
        }

    }

    private fun givenMediaDetailModel(season: SeasonWithProgress, seasonEpisodes: List<Episode>): MediaDetailModel {
        return MediaDetailModel(
                MediaDetailModel.MediaDetailInfo(),
                seasons = listOf(season),
                showProgress = givenShowWatchedProgress(seasonEpisodes, 0, testSeasonNumber, testSeasonEpisodeCount)
        )
    }

    private fun givenSeasonWithProgress(seasonEpisodes: List<Episode>): SeasonWithProgress {
        return SeasonWithProgress(
                givenTestSeason(seasonEpisodes),
                episodes = seasonEpisodes.map { EpisodeWithProgress(it, ShowWatchedProgress.EpisodeWatchedProgress(it.number)) },
                progress = arrangeSeasonWatchedProgress(testSeasonNumber, seasonEpisodes, 0, testSeasonEpisodeCount)
        )
    }


    private fun givenTestSeason(seasonEpisodes: List<Episode>): Season {
        return Season(
                number = testSeasonNumber,
                ids = MediaIds(testSeasonNumber.toLong()),
                episodes = seasonEpisodes,
                title = "Test season"
        )
    }

    private fun givenShowWatchedProgress(seasonEpisodes: List<Episode>, watchedEpisodes: Int, seasonNumber: Int, episodeCount: Int): ShowWatchedProgress {
        return ShowWatchedProgress(
            aired = seasonEpisodes.size,
            completed = watchedEpisodes,
            last_episode = if (watchedEpisodes > 0) seasonEpisodes[watchedEpisodes - 1] else null,
            next_episode = seasonEpisodes[watchedEpisodes],
            last_watched_at = 1.days.ago.atStartOfDay(),
            seasons = listOf(arrangeSeasonWatchedProgress(seasonNumber, seasonEpisodes, watchedEpisodes, episodeCount))
        )
    }

    private fun arrangeSeasonWatchedProgress(seasonNumber: Int, seasonEpisodes: List<Episode>, watchedEpisodes: Int, episodeCount: Int): ShowWatchedProgress.SeasonWatchedProgress {
        return ShowWatchedProgress.SeasonWatchedProgress(
            number = seasonNumber,
            aired = seasonEpisodes.size,
            completed = watchedEpisodes,
            episodes = (1..episodeCount).map {
                ShowWatchedProgress.EpisodeWatchedProgress(it, it <= watchedEpisodes, last_watched_at = 1.days.ago.atStartOfDay())
            }
        )
    }


    private fun givenPresenterInstanceWithMockedDataService(dataServiceStubbing: KStubbing<MediaDetailManager>.(MediaDetailManager) -> Unit): MediaDetailPresenter {
        mediaManager = mock<MediaDetailManager>(stubbing = dataServiceStubbing)
        presenter = MediaDetailPresenter(mediaManager, CurrentTimeProviderImpl())

        return presenter
    }

    private fun arrangeSeasonWithProgress(seasonNumber: Int, seasonEpisodes: List<Episode>): SeasonWithProgress {
        return SeasonWithProgress(
            Season(
                number = seasonNumber,
                ids = MediaIds(seasonNumber.toLong()),
                title = "test season",
                episodes = seasonEpisodes
            ),
            episodes = seasonEpisodes.map { EpisodeWithProgress(it) }
        )
    }

    private fun givenEpisodesForSeason(episodeCount: Int, seasonNumber: Int) =
        (1..episodeCount).map { Episode(season = seasonNumber, number = it, ids = MediaIds(it.toLong()), title = "test episode $it") }
}