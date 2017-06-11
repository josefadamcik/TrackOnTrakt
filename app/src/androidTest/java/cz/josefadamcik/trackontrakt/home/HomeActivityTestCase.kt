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
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import cz.josefadamcik.trackontrakt.BuildConfig
import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.search.SearchResultsActivity
import cz.josefadamcik.trackontrakt.testutil.RecyclerViewRowMatcher.atPosition
import cz.josefadamcik.trackontrakt.testutil.WiremockTimberNotifier
import cz.josefadamcik.trackontrakt.testutil.activityTestRule
import cz.josefadamcik.trackontrakt.testutil.asset
import cz.josefadamcik.trackontrakt.testutil.childAtPosition
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test


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
    }


    @Test
    fun showHistoryTest() {
        val appContext = InstrumentationRegistry.getTargetContext()
        //launch activity
        val activity = activityTestRule.launchActivity(Intent(appContext, HomeActivity::class.java))

        //should render results in list
        onView(withId(R.id.list))
            .check(matches(atPosition(0, hasDescendant(withText("Black Books - S 2, Ep 1")))))

        //should open detail when clickeed

    }

    @Test
    fun searchTest() {
        val searchQuery = "rick and morty"
        val appContext = InstrumentationRegistry.getTargetContext()

        //launch activity
        val activity = activityTestRule.launchActivity(Intent(appContext, HomeActivity::class.java))

        val searchEditText = onView(
            allOf(withId(R.id.searchEditText_input),
                childAtPosition(
                    allOf(withId(R.id.linearLayout),
                        childAtPosition(
                            withClassName(`is`("android.widget.LinearLayout")),
                            0)),
                    1)
            ))

        searchEditText.perform(ViewActions.replaceText(searchQuery), ViewActions.pressImeActionButton())

        intended(
            allOf(
                hasAction(Intent.ACTION_SEARCH),
                hasComponent(SearchResultsActivity::class.java.name),
                hasExtra(SearchManager.QUERY, searchQuery)
            )
        )
    }

}
