package cz.josefadamcik.trackontrakt.search


import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.intent.Intents
import android.support.test.espresso.intent.matcher.IntentMatchers
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
import cz.josefadamcik.trackontrakt.testutil.WiremockTimberNotifier
import cz.josefadamcik.trackontrakt.testutil.activityTestRule
import cz.josefadamcik.trackontrakt.testutil.asset
import cz.josefadamcik.trackontrakt.testutil.childAtPosition
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchResultsActivityTestCase {

    @get:Rule
    var wireMockRule = WireMockRule(WireMockConfiguration.options()
        .port(BuildConfig.MOCKSERVER_PORT)
        .notifier(WiremockTimberNotifier),
        false
    )

    @get:Rule
    val activityTestRule = activityTestRule<SearchResultsActivity>()

    @Test
    fun showSearchTest() {
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

        //assert query present in search input
        val editText = onView(
            allOf(withId(R.id.mt_editText), withText("rick and morty"),
                isDisplayed()))
        editText.check(matches(withText("rick and morty")))

        //assert first row -> rick and morty show
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


        //should open detail when clicked
        onView(childAtPosition(withId(R.id.list), 0))
            .perform(ViewActions.click())
        //assert intent
        Intents.intended(allOf(
            IntentMatchers.hasComponent(MediaDetailActivity::class.java.name),
            IntentMatchers.hasExtra(MediaDetailActivity.PAR_ID, MediaIdentifier(MediaType.show, 69829)),
            IntentMatchers.hasExtra(MediaDetailActivity.PAR_NAME, "Rick and Morty")
        ))
    }


}
