
package cz.josefadamcik.trackontrakt.home

import com.nhaarman.mockito_kotlin.*
import cz.josefadamcik.trackontrakt.data.api.model.*
import io.reactivex.Single
import org.hamcrest.Matchers
import org.hamcrest.Matchers.equalTo
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Test
import org.threeten.bp.LocalDateTime


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
        argumentCaptor<HistoryModel>().apply {
            verify(view, times(1)).showHistory(capture())
            assertThat("Model has flag that there are more pages", firstValue.hasNextPage, equalTo(true))
            assertThat("Model has flag that its not loading next page ATM.", firstValue.loadingNextPage, equalTo(false))
        }


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
        presenter.loadNextPage()

        //then
        verify(view, times(1)).showLoading()
        verify(view, atLeastOnce()).hideLoading()

        argumentCaptor<HistoryModel>().apply {
            //first invocation is initial load, than 2 calls for next loadHomeStreet
            verify(view, times(3)).showHistory(capture())

            assertThat("got three model updates", allValues.size, equalTo(3))

            //the first model update is initial load
            assertThat("First model with flag that there are more pages", firstValue.hasNextPage, equalTo(true))
            assertThat("First model without flag that it's loading more pages", firstValue.loadingNextPage, equalTo(false))

            //then second model update is just loader stat update
            assertThat("First and second model has same amount of items", firstValue.items.size, Matchers.equalTo(secondValue.items.size))
            assertThat("Second model update with flag that its loading next page ATM.", secondValue.loadingNextPage, equalTo(true))

            //and the third comes with new items and "loading" state reset
            assertThat("Final model update has more items", thirdValue.items.size, Matchers.greaterThan(secondValue.items.size))
            assertThat("Final model update with reset of loading flag", thirdValue.loadingNextPage, equalTo(false))
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
                    watched_at = LocalDateTime.now().minusDays(it.toLong()),
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