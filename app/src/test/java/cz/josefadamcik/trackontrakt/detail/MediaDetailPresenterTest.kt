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

import com.nhaarman.mockito_kotlin.mock
import cz.josefadamcik.trackontrakt.data.api.model.*
import khronos.Dates
import khronos.day
import khronos.minus
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Test

class MediaDetailPresenterTest {


    @Test
    fun combineSeasonDataWithProgressTest() {
        //arrange / given
        val presenter = arrangePresenterInstance()
        val seasonNumber = 1
        val episodeCount = 5
        val seasonEpisodes = arrangeEpisodesForSeason(episodeCount, seasonNumber)
        val seasonsWithoutProgress = listOf(arrangeSeasonWithProgress(seasonNumber, seasonEpisodes))

        val watchedEpisodes = 3
        val watchedProgress = ShowWatchedProgress(
            aired = seasonEpisodes.size,
            completed = watchedEpisodes,
            last_episode = seasonEpisodes[watchedEpisodes - 1],
            next_episode = seasonEpisodes[watchedEpisodes],
            last_watched_at = Dates.today - 1.day,
            seasons = listOf(
                ShowWatchedProgress.SeasonWatchedProgress(
                    number = seasonNumber,
                    aired = seasonEpisodes.size,
                    completed = watchedEpisodes,
                    episodes = (1..episodeCount).map {
                        ShowWatchedProgress.EpisodeWatchedProgress(it, it <= watchedEpisodes, last_watched_at = Dates.today - 1.day)
                    }
                )
            )
        )

        //act / when
        val result = presenter.combineSeasonDataWithProgress(seasonsWithoutProgress, watchedProgress)

        //assert / then
        assertThat("result is not null", result, notNullValue())
        assertThat("there is a season in result", result.size, equalTo(1))
        result.first().let { swp ->
            assertThat("there are episodes in the season", swp.episodes.size, equalTo(episodeCount))
            assertThat("season has correct number of completed episodes in its progress", swp.progress.completed, equalTo(watchedEpisodes))
            assertThat("first $watchedEpisodes are watched", swp.episodes.map { it.progress.completed }, equalTo(listOf(true, true, true, false, false)))
        }

    }

    private fun arrangePresenterInstance(): MediaDetailPresenter {
        //        val testShowId: Long = 1
//        val mediaDataSource = mock<MediaDetailDataSource> {
//            on { loadShowWatchedProgress(testShowId) } doReturn Single.just(ShowWatchedProgress())
//        }
        val mediaDataSource = mock<MediaDetailDataSource>()
        val presenter = MediaDetailPresenter(mediaDataSource)
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

    private fun arrangeEpisodesForSeason(episodeCount: Int, seasonNumber: Int) =
        (1..episodeCount).map { Episode(season = seasonNumber, number = it, ids = MediaIds(it.toLong()), title = "test episode $it") }
}