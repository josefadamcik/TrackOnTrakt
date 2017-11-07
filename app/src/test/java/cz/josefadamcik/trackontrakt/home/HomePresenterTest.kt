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
package cz.josefadamcik.trackontrakt.home

import com.nhaarman.mockito_kotlin.*
import cz.josefadamcik.trackontrakt.data.api.model.*
import io.reactivex.Single
import khronos.Dates
import khronos.days
import khronos.minus
import org.hamcrest.Matchers
import org.hamcrest.Matchers.equalTo
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Test


class HomePresenterTest {


    @After
    fun tearDown() {
    }

    @Test
    fun loadHomeStreamDataOnAttachView() {
        //given
        val userHistoryManager = givenHistoryManagerReturningList()
        val view = givenMockView()
        val presenter = givenPresenter(userHistoryManager)


        //when

        presenter.attachView(view)

        //then

        verify(view).showLoading()
        verify(view).hideLoading()
        verify(view).showHistory(any())
    }

    @Test
    fun showErrorWhenUnableToLoad() {
        //given
        val userHistoryManager = mock<UserHistoryManager> {
            on { loadUserHistory(any()) } doReturn Single.error(Exception("Test error"))
        }
        val view = givenMockView()
        val presenter = givenPresenter(userHistoryManager)
        //when

        presenter.attachView(view)

        //then
        verify(view).showLoading()
        verify(view).hideLoading()
        verify(view).showError(any())
    }

    @Test
    fun loadHomeStreamDataSecondCall() {
        //given
        val userHistoryManager = givenHistoryManagerReturningList()
        val view = givenMockView()
        val presenter = givenPresenter(userHistoryManager)


        //when

        presenter.attachView(view)
        presenter.loadHomeStreamData(false)

        //then


        verify(view, times(2)).showLoading()
        verify(view, times(2)).hideLoading()
        verify(view, times(2)).showHistory(any())
    }

    @Test
    fun loadMoreData() {
        //given
        val userHistoryManager = givenHistoryManagerReturningList()
        val view = givenMockView()
        val presenter = givenPresenter(userHistoryManager)


        //when

        presenter.attachView(view)
        presenter.loadNextPage()

        //then

        verify(view, times(2)).showLoading()
        verify(view, times(2)).hideLoading()
        argumentCaptor<HistoryModel>().apply {
            verify(view, times(2)).showHistory(capture())
            assertThat("got two results", allValues.size, equalTo(2))
            assertThat("second result has more model", secondValue.items.size, Matchers.greaterThan(firstValue.items.size))
        }

    }

    private fun givenHistoryManagerReturningList(): UserHistoryManager {
        return mock<UserHistoryManager> {
            on { loadUserHistory(1) } doReturn Single.just(givenListOfHistoryItems(1..10))
            on { loadUserHistory(2) } doReturn Single.just(givenListOfHistoryItems(11..21))
        }
    }

    private fun givenPresenter(userHistoryManager: UserHistoryManager) =
        HomePresenter(userHistoryManager)

    private fun givenMockView(): HomeView {
        return mock<HomeView> {}
    }

    private fun givenListOfHistoryItems(idRange: IntRange): HistoryItems {
        val movie = Movie("test movie", 2012, MediaIds(1))
        return HistoryItems(
            items = idRange.map {
                HistoryItem(
                    it.toLong(),
                    watched_at = Dates.today - it.days,
                    action = Action.checkin,
                    type = MediaType.movie,
                    movie = movie
                )
            },
            itemCount = 100,
            pageCount = 10,
            page = if (idRange.first == 1) 1 else 2
        )
    }

}