package cz.josefadamcik.trackontrakt.search


import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.runner.AndroidJUnit4
import android.view.View
import android.widget.LinearLayout
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import cz.josefadamcik.trackontrakt.BuildConfig
import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.testutil.WiremockTimberNotifier
import cz.josefadamcik.trackontrakt.testutil.activityTestRule
import cz.josefadamcik.trackontrakt.testutil.asset
import cz.josefadamcik.trackontrakt.testutil.childAtPosition
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.hamcrest.core.IsInstanceOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchResultsActivityTestCase {

    @get:Rule
    var wireMockRule = WireMockRule(WireMockConfiguration.options()
        .port(BuildConfig.MOCKSERVER_PORT)
        .notifier(WiremockTimberNotifier)
    )

    @get:Rule
    val activityTestRule = activityTestRule<SearchResultsActivity>()

    @Test
    fun homeActivitySearchTest() {
        val appContext = InstrumentationRegistry.getTargetContext()

        wireMockRule.stubFor(
            get(urlMatching("/search/.*"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(asset(appContext, "search_ricknmorty.json"))
                )
        )

        //launch activity
        val activity = activityTestRule.launchActivity(SearchResultsActivity.createIntent(appContext, "rick and morty", TraktFilter(movies = true, shows = true)))

        //assert

        val textView = onView(
            allOf(withId(R.id.title), withText("Rick and Morty"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.list),
                        0),
                    0),
                isDisplayed()))
        textView.check(matches(withText("Rick and Morty")))

        val textView3 = onView(
            allOf(
                withId(R.id.type_info),
                withText(containsString("show")),
                isDescendantOfA(
                    childAtPosition(withId(R.id.list), 0)
                ),
                isDisplayed()))
        textView3.check(matches(withText("show ( 2013 )")))

        val editText = onView(
            allOf(withId(R.id.searchEditText_input), withText("rick and morty"),
                childAtPosition(
                    allOf(withId(R.id.linearLayout),
                        childAtPosition(
                            IsInstanceOf.instanceOf<View>(LinearLayout::class.java),
                            0)),
                    1),
                isDisplayed()))
        editText.check(matches(withText("rick and morty")))

    }


}
