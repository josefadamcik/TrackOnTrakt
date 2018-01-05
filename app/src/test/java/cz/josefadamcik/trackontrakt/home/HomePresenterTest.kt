
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
            assertThat("Model has flag that there are more pages", lastValue.hasNextPage, equalTo(true))
            assertThat("Model has flag that its not loading next page ATM.", lastValue.loadingNextPage, equalTo(false))

            assertThat("there is a 'just watching' item in result", lastValue.watching, Matchers.allOf(
                Matchers.notNullValue(),
                Matchers.isA(Watching.Nothing::class.java as Class<Watching>)
            ))
        }
    }

    @Test
    fun showErrorWhenUnableToLoad() {
        //given
        val userHistoryManager = mock<UserHistoryManager> {
            on { loadUserHistory(any()) } doReturn Single.error(Exception("Test error"))
            on { loadWatching() } doReturn Single.error(Exception("Test error"))
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

    @Test
    fun loadWatching() {
        //given
        val userHistoryManager = givenHistoryManagerReturningList(
            watchingSomething = true,
            firstItemWatchedAt = LocalDateTime.now().plusMinutes(15)
        )
        val view = givenMockView()
        val presenter = givenPresenter(userHistoryManager)

        //when

        presenter.attachView(view)

        //then
        argumentCaptor<HistoryModel>().apply {
            //first invocation is initial load
            verify(view, times(1)).showHistory(capture())
            //we will check if the model contains our "just watching" record

            assertThat("there is a 'just watching' item in result", lastValue.watching, Matchers.allOf(
                Matchers.notNullValue(),
                Matchers.isA(Watching.Something::class.java as Class<Watching>)
            ))

            assertThat("First (duplicated) item from history records was removed", lastValue.items.first().id, Matchers.not(1L))

        }
    }

    @Test
    fun loadWatchingWithOverlappingFirstItem() {
        //If a user does a checkin there is a duplicity until the checkin expires ("watching" item is the same as the first item of history list).
        //But another checkin is permitted before the "watched_at" time of the corresponding history item is reached.
        //If user performs another checkin in this period the value of "watching" will change and new item will be added to the history list
        // and there will be two history list items with "watched_at" value in future.
        //We don't want the duplicity (see test #loadWatching) bud we want to cover this edge case.

        //given
        val userHistoryManager = givenHistoryManagerReturningList(
            watchingSomething = true,
            firstItemWatchedAt = LocalDateTime.now().plusMinutes(45) // so the first two items will be in future
        )
        val view = givenMockView()
        val presenter = givenPresenter(userHistoryManager)

        //when

        presenter.attachView(view)

        //then
        argumentCaptor<HistoryModel>().apply {
            //first invocation is initial load
            verify(view, times(1)).showHistory(capture())
            //we will check if the model contains our "just watching" record

            assertThat("there is a 'just watching' item in result", lastValue.watching, Matchers.allOf(
                Matchers.notNullValue(),
                Matchers.isA(Watching.Something::class.java as Class<Watching>)
            ))

            assertThat("First (duplicated) item from history records was removed", lastValue.items.first().id, Matchers.not(1L))
            assertThat("Second (not duplicated) item was not removed and is now the first one", lastValue.items.first().id, Matchers.equalTo(2L))

        }
    }


    private fun givenHistoryManagerReturningList(watchingSomething: Boolean = false, firstItemWatchedAt: LocalDateTime = LocalDateTime.now().minusMinutes(1)): UserHistoryManager {
        return mock<UserHistoryManager> {
            on { loadUserHistory(1) } doReturn Single.just(givenListOfHistoryItems(1..10, firstItemWatchedAt))
            on { loadUserHistory(2) } doReturn Single.just(givenListOfHistoryItems(11..21, firstItemWatchedAt.minusDays(1)))
            on { loadWatching() } doReturn Single.just(
                if (watchingSomething)
                    Watching.Something(
                        expires_at = LocalDateTime.now().plusMinutes(30),
                        started_at = LocalDateTime.now().minusMinutes(30),
                        type = MediaType.movie,
                        action = "checkin",
                        movie = testMovie()
                    ) as Watching
                else Watching.Nothing as Watching
            )
        }
    }

    private fun givenPresenter(userHistoryManager: UserHistoryManager) =
        HomePresenter(userHistoryManager)

    private fun givenMockView(): HomeView {
        return mock<HomeView> {}
    }

    private fun givenListOfHistoryItems(idRange: IntRange, firstItemWatchedAt: LocalDateTime): HistoryItems {
        val movie = testMovie()
        return HistoryItems(
            items = idRange.map {
                HistoryItem(
                    it.toLong(),
                    watched_at = firstItemWatchedAt.minusMinutes((it - 1).toLong() * 30),
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

    private fun testMovie() = Movie("test movie", 2012, MediaIds(1))

}