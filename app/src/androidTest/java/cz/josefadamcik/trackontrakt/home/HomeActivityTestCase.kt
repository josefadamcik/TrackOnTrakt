package cz.josefadamcik.trackontrakt.home


import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.matcher.IntentMatchers.*
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.runner.AndroidJUnit4
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import cz.josefadamcik.trackontrakt.BuildConfig
import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.data.api.TraktApi
import cz.josefadamcik.trackontrakt.data.api.model.MediaType
import cz.josefadamcik.trackontrakt.detail.MediaDetailActivity
import cz.josefadamcik.trackontrakt.detail.MediaIdentifier
import cz.josefadamcik.trackontrakt.search.SearchResultsActivity
import cz.josefadamcik.trackontrakt.testutil.*
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneOffset

@RunWith(AndroidJUnit4::class)
class HomeActivityTestCase {

    @get:Rule
    var wireMockRule = WireMockRule(WireMockConfiguration.options()
        .port(BuildConfig.MOCKSERVER_PORT)
        .notifier(WiremockTimberNotifier),
        false
    )

    @get:Rule
    val activityTestRule = activityTestRule<HomeActivity>()

    @Before
    fun prepareApiStub() {
        val appContext = InstrumentationRegistry.getTargetContext()
        // API call
        givenApiStubForHistoryPage("1", appContext)
        givenApiStubForHistoryPage("2", appContext)
        wireMockRule.stubFor(
            get(urlMatching("/users/me/watching.*"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(asset(appContext, "watching.json"))
                )
        )
    }

    private fun givenApiStubForHistoryPage(page: String, appContext: Context) {
        wireMockRule.stubFor(
                get(urlMatching("/users/me/history.*"))
                        .withQueryParam("page", equalTo(page))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader(TraktApi.HEADER_PAGINATION_PAGE, page)
                                .withHeader(TraktApi.HEADER_PAGINATION_PAGE_COUNT, "10")
                                .withHeader(TraktApi.HEADER_PAGINATION_ITEM_COUNT, "100")
                                .withBody(asset(appContext, "history.json"))
                        )
        )
    }


    @Test
    fun showHistoryTest() {
        //given
        givenSystemTimeDuringWatchingPeriodInData()
        val appContext = InstrumentationRegistry.getTargetContext()

        //when activity is launched
        whenHomeActivityIsLaunched(appContext)

        //than
        onView(withId(R.id.list))
            .check(matches(isDisplayed()))

        verify(getRequestedFor(urlPathEqualTo("/users/me/history"))
                .withQueryParam("page", equalTo("1")))

        //than there's watching header.
        onView(childAtPosition(withId(R.id.list), 0))
            .check(matches(allOf(
                isDisplayed(),
                hasDescendant(withText(R.string.now_watching))
            )))

        //than there's undergoing show
        onView(childAtPosition(withId(R.id.list), 1))
            .check(matches(allOf(
                isDisplayed(),
                hasDescendant(withText("Children of apes"))
            )))

        //than there's today header
        onView(childAtPosition(withId(R.id.list), 2))
            .check(matches(allOf(
                isDisplayed(),
                hasDescendant(withText(R.string.today))
            )))

        //than there's another show
        onView(childAtPosition(withId(R.id.list), 3))
            .check(matches(allOf(
                isDisplayed(),
                hasDescendant(withText("Black Books"))
            )))

        //than there's a pager row
        //But start with scroll
        onView(withId(android.R.id.content)).perform(swipeUp())
        onView(withId(R.id.list))
            .perform(RecyclerViewActions.scrollTo<HistoryAdapter.PagerViewHolder>(hasDescendant(withText(R.string.pager_load_more))))

        onView(allOf(isDescendantOfA(withId(R.id.list)), hasDescendant(withText(R.string.pager_load_more))))
            .check(matches(isDisplayed()))
            .perform(click())

        verify(getRequestedFor(urlPathEqualTo("/users/me/history"))
                .withQueryParam("page", equalTo("2")))
    }


    @Test
    fun clickOnHistoryTest() {
        //given
        givenSystemTimeDuringWatchingPeriodInData()
        val appContext = InstrumentationRegistry.getTargetContext()
        //when activity is launched
        whenHomeActivityIsLaunched(appContext)
        //than

        //should open detail when clicked
        onView(childAtPosition(withId(R.id.list), 3))
            .check(matches(isDisplayed()))
            .perform(click())
        //assert intent
        intended(allOf(
            hasComponent(MediaDetailActivity::class.java.name),
            hasExtra(MediaDetailActivity.PAR_ID, MediaIdentifier(MediaType.show, 898)),
            hasExtra(MediaDetailActivity.PAR_NAME, "Black Books")
        ))
    }

    @Test
    fun clickOnNowWatchingTest() {
        //given
        givenSystemTimeDuringWatchingPeriodInData()
        val appContext = InstrumentationRegistry.getTargetContext()
        //when activity is launched
        whenHomeActivityIsLaunched(appContext)
        //than

        //should open detail when clicked
        //than there's undergoing show
        onView(childAtPosition(withId(R.id.list), 1))
                .check(matches(allOf(
                        isDisplayed(),
                        hasDescendant(withText("Children of apes"))
                )))
                .perform(click())
        //assert intent
        intended(allOf(
                hasComponent(MediaDetailActivity::class.java.name),
                hasExtra(MediaDetailActivity.PAR_ID, MediaIdentifier(MediaType.movie, 4965)),
                hasExtra(MediaDetailActivity.PAR_NAME, "Children of apes")
        ))
    }

    @Test
    fun searchTest() {
        val searchQuery = "Test Show Name"
        val appContext = InstrumentationRegistry.getTargetContext()

        givenApiStubForSearch(appContext)

        //launch activity
        whenHomeActivityIsLaunched(appContext)

        //click on placeholder - that will activate edit text for search
        val searchPlaceholder = onView(withId(R.id.mt_placeholder))
        searchPlaceholder.perform(click())

        val searchEditText = onView(allOf(withId(R.id.mt_editText)))

        searchEditText.perform(
            replaceText(searchQuery),
            pressImeActionButton()
        )

        //assert intent
        intended(
            allOf(
                hasAction(Intent.ACTION_SEARCH),
                hasComponent(SearchResultsActivity::class.java.name),
                hasExtra(SearchManager.QUERY, searchQuery)
            )
        )
    }

    private fun whenHomeActivityIsLaunched(appContext: Context?) {
        val activity = activityTestRule.launchActivity(Intent(appContext, HomeActivity::class.java))
    }

    private fun givenApiStubForSearch(appContext: Context) {
        wireMockRule.stubFor(
                get(urlMatching("/search/.*"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withBody(asset(appContext, "search_showname.json"))
                        )
        )
    }


    private fun givenSystemTimeDuringWatchingPeriodInData() {
        TestTimeProvider.clock = Clock.fixed(Instant.parse("2017-05-07T22:00:31Z"), ZoneOffset.UTC)
    }

}
