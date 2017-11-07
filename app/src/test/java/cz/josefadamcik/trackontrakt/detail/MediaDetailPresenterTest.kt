/*
 Copyright 2017 Josef Adamcik <josef.adamcik@gmail.com>

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
package cz.josefadamcik.trackontrakt.detail

import com.nhaarman.mockito_kotlin.*
import cz.josefadamcik.trackontrakt.data.api.model.*
import io.reactivex.Single
import khronos.Dates
import khronos.day
import khronos.minus
import okhttp3.Protocol
import okhttp3.Request
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Test
import retrofit2.Response

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
        //arrange / given

        val seasonEpisodes = arrangeEpisodesForSeason(testSeasonEpisodeCount, testSeasonNumber)
        val season = SeasonWithProgress(
            arrangeTestSeason(seasonEpisodes),
            episodes = seasonEpisodes.map { EpisodeWithProgress(it, ShowWatchedProgress.EpisodeWatchedProgress(it.number)) },
            progress = arrangeSeasonWathchedProgress(testSeasonNumber, seasonEpisodes, 0, testSeasonEpisodeCount)
        )
        val episodeToCheckIn = season.episodes.first();

        val presenter = arrangePresenterInstanceWithMockedDataService({
            on { doCheckin(any()) } doReturn Single.just(
                Response.success(
                    CheckinResponse(1, Dates.today, null),
                    arrangeOkhttpResponse201()
                )
            )
        })

        val view = mock<MediaDetailView>()
        presenter.view = view

        // inject model into presenter -> we will observe if the model changed
        presenter.model = MediaDetailModel(
            MediaDetailModel.MediaDetailInfo(),
            seasons = listOf(season),
            showProgress = arrangeShowWatchedProgress(seasonEpisodes, 0, testSeasonNumber, testSeasonEpisodeCount)
        )

        // act / when
        presenter.checkinActionClicked(episodeToCheckIn)

        // assert / then
        verify(mediaManager).doCheckin(any())

        verify(view).showLoading()
        verify(view).hideLoading()
        verify(view).showCheckinSuccess()
        argumentCaptor<MediaDetailModel>().apply {
            verify(view).showMedia(capture())

            val model = lastValue
            val modelEpisode = model.seasons.firstOrNull()?.episodes?.firstOrNull()
            assertThat("checked episode exists in model", modelEpisode, notNullValue())
            assertThat("checked episode in model is watched", modelEpisode?.progress?.completed ?: false, equalTo(true))
            assertThat("number of watched episodes is increased", model.showProgress.completed, equalTo(1))
        }

    }

    @Test
    fun checkinEpisodeAlreadyWatchedTest() {
        //arrange / given

        val seasonEpisodes = arrangeEpisodesForSeason(testSeasonEpisodeCount, testSeasonNumber)
        val season = SeasonWithProgress(
            arrangeTestSeason(seasonEpisodes),
            episodes = seasonEpisodes.map { EpisodeWithProgress(it, ShowWatchedProgress.EpisodeWatchedProgress(it.number, it.number == 1)) },
            progress = arrangeSeasonWathchedProgress(testSeasonNumber, seasonEpisodes, 1, testSeasonEpisodeCount)
        )
        val episodeToCheckIn = season.episodes.first()

        val presenter = arrangePresenterInstanceWithMockedDataService({ })

        val view = mock<MediaDetailView>()
        presenter.view = view

        // act / when
        presenter.checkinActionClicked(episodeToCheckIn)

        // assert / then
        verify(view).showAlreadyWatchedStats(eq(1), anyOrNull())
    }

    private fun arrangeTestSeason(seasonEpisodes: List<Episode>): Season {
        return Season(
            number = testSeasonNumber,
            ids = MediaIds(testSeasonNumber.toLong()),
            episodes = seasonEpisodes,
            title = "Test season"
        )
    }


    @Test
    fun combineSeasonDataWithProgressTest() {
        //arrange / given
        val presenter = arrangePresenterInstanceWithMockedDataService({ })
        val seasonEpisodes = arrangeEpisodesForSeason(testSeasonEpisodeCount, testSeasonNumber)
        val seasonsWithoutProgress = listOf(arrangeSeasonWithProgress(testSeasonNumber, seasonEpisodes))
        val watchedProgress = arrangeShowWatchedProgress(seasonEpisodes, testSeasonWatchedEpisodes, testSeasonNumber, testSeasonEpisodeCount)

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

    private fun arrangeShowWatchedProgress(seasonEpisodes: List<Episode>, watchedEpisodes: Int, seasonNumber: Int, episodeCount: Int): ShowWatchedProgress {
        return ShowWatchedProgress(
            aired = seasonEpisodes.size,
            completed = watchedEpisodes,
            last_episode = if (watchedEpisodes > 0) seasonEpisodes[watchedEpisodes - 1] else null,
            next_episode = seasonEpisodes[watchedEpisodes],
            last_watched_at = Dates.today - 1.day,
            seasons = listOf(arrangeSeasonWathchedProgress(seasonNumber, seasonEpisodes, watchedEpisodes, episodeCount))
        )
    }

    private fun arrangeSeasonWathchedProgress(seasonNumber: Int, seasonEpisodes: List<Episode>, watchedEpisodes: Int, episodeCount: Int): ShowWatchedProgress.SeasonWatchedProgress {
        return ShowWatchedProgress.SeasonWatchedProgress(
            number = seasonNumber,
            aired = seasonEpisodes.size,
            completed = watchedEpisodes,
            episodes = (1..episodeCount).map {
                ShowWatchedProgress.EpisodeWatchedProgress(it, it <= watchedEpisodes, last_watched_at = Dates.today - 1.day)
            }
        )
    }


    private fun arrangePresenterInstanceWithMockedDataService(dataServiceStubbing: KStubbing<MediaDetailManager>.(MediaDetailManager) -> Unit): MediaDetailPresenter {
        mediaManager = mock<MediaDetailManager>(stubbing = dataServiceStubbing)
        presenter = MediaDetailPresenter(mediaManager)

        return presenter
    }

    private fun arrangeOkhttpResponse201(): okhttp3.Response? {
        return okhttp3.Response.Builder()
            .code(201)
            .message("OK")
            .request(Request.Builder().url("http://localhost/").build())
            .protocol(Protocol.HTTP_1_1)
            .build()
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

    private fun arrangeEpisodesForSeason(episodeCount: Int, seasonNumber: Int) =
        (1..episodeCount).map { Episode(season = seasonNumber, number = it, ids = MediaIds(it.toLong()), title = "test episode $it") }
}