package cz.josefadamcik.trackontrakt.home


import android.app.SearchManager
import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.matcher.IntentMatchers.*
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.runner.AndroidJUnit4
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import cz.josefadamcik.trackontrakt.BuildConfig
import cz.josefadamcik.trackontrakt.R
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
        wireMockRule.stubFor(
            get(urlMatching("/users/me/history.*"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(asset(appContext, "history.json"))
                )
        )
        wireMockRule.stubFor(
            get(urlMatching("/users/me/watching.*"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(asset(appContext, "watching.json"))
                )
        )
    }


    @Test
    fun showHistoryTest() {
        //given
        givenSystemTimeDuringWatchingPeriodInData()
        val appContext = InstrumentationRegistry.getTargetContext()

        //when activity is launched
        val activity = activityTestRule.launchActivity(Intent(appContext, HomeActivity::class.java))

        //than
        onView(withId(R.id.list))
            .check(matches(isDisplayed()))

        //than there's watching header.
        onView(childAtPosition(withId(R.id.list), 0))
            .check(matches(allOf(
                isDisplayed(),
                hasDescendant(withText(R.string.now_watching))
            )))

        //thatn there's undergoing show
        onView(childAtPosition(withId(R.id.list), 1))
            .check(matches(allOf(
                isDisplayed(),
                hasDescendant(withText("Breaking Bread - S 0, Ep 2"))
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
                hasDescendant(withText("Black Books - S 2, Ep 1"))
            )))

    }


    @Test
    fun clickOhHistoryTest() {
        //given
        givenSystemTimeDuringWatchingPeriodInData()
        val appContext = InstrumentationRegistry.getTargetContext()
        //whan activity is launched
        val activity = activityTestRule.launchActivity(Intent(appContext, HomeActivity::class.java))
        //than

        //should open detail when clicked
        onView(childAtPosition(withId(R.id.list), 3))
            .check(matches(isDisplayed()))
            .perform(ViewActions.click())
        //assert intent
        intended(allOf(
            hasComponent(MediaDetailActivity::class.java.name),
            hasExtra(MediaDetailActivity.PAR_ID, MediaIdentifier(MediaType.show, 898)),
            hasExtra(MediaDetailActivity.PAR_NAME, "Black Books")
        ))
    }

    @Test
    fun searchTest() {
        val searchQuery = "Test Show Name"
        val appContext = InstrumentationRegistry.getTargetContext()

        wireMockRule.stubFor(
            get(urlMatching("/search/.*"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(asset(appContext, "search_showname.json"))
                )
        )

        //launch activity
        val activity = activityTestRule.launchActivity(Intent(appContext, HomeActivity::class.java))

        //click on placeholder - that will activate edit text for search
        val searchPlaceholder = onView(withId(R.id.mt_placeholder))
        searchPlaceholder.perform(ViewActions.click())

        val searchEditText = onView(allOf(withId(R.id.mt_editText)))

        searchEditText.perform(
            ViewActions.replaceText(searchQuery),
            ViewActions.pressImeActionButton()
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


    private fun givenSystemTimeDuringWatchingPeriodInData() {
        TestTimeProvider.clock = Clock.fixed(Instant.parse("2017-05-07T22:00:31Z"), ZoneOffset.UTC)
    }

}
