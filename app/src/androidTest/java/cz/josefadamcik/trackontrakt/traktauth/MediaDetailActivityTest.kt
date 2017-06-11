package cz.josefadamcik.trackontrakt.traktauth


import android.support.design.widget.CollapsingToolbarLayout
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.BoundedMatcher
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
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class MediaDetailActivityTest {

    @get:Rule
    var wireMockRule = WireMockRule(WireMockConfiguration.options()
        .port(BuildConfig.MOCKSERVER_PORT)
        .notifier(WiremockTimberNotifier)
    )

    @get:Rule
    var activityTestRule = activityTestRule<MediaDetailActivity>()

    @Test
    fun mediaDetailActivityTest() {
        val appContext = InstrumentationRegistry.getTargetContext()
        // API calls
        wireMockRule.stubFor(
            get(urlPathEqualTo("/shows/69829"))
                .withQueryParam("extended", equalTo("full"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(asset(appContext, "shows_69829_extendedfull.json"))
                )

        )
        wireMockRule.stubFor(
            get(urlPathEqualTo("/shows/69829/last_episode"))
                .withQueryParam("extended", equalTo("full"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(asset(appContext, "shows_69829_last_episode_extended_full.json"))
                )
        )
        wireMockRule.stubFor(
            get(urlPathEqualTo("/shows/69829/seasons"))
                .withQueryParam("extended", equalTo("episodes"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(asset(appContext, "shows_69829_seasons_extendedepisodes.json"))
                )
        )

        //launch activity

        activityTestRule.launchActivity(
            MediaDetailActivity.createIntent(appContext, MediaIdentifier(MediaType.show, 69829), "Rick and Morty")
        )

        //assert info rendered

        onView(isAssignableFrom(CollapsingToolbarLayout::class.java))
            .check(matches(withCollapsingToolbarTitle(`is`("Rick and Morty"))))


        val showDescription = "A sociopathic scientist drags his unintelligent grandson on insanely dangerous adventures across the universe."
        val textView = onView(
            allOf(
                withId(R.id.txt_description),
                isDisplayed()
            )
        )
        textView.check(matches(withText(showDescription)))

        val textView2 = onView(
            allOf(withText("Latest aired episode"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.list),
                        1),
                    0),
                isDisplayed()))
        textView2.check(matches(withText("Latest aired episode")))

    }

    private fun withCollapsingToolbarTitle(
        textMatcher: Matcher<CharSequence>): Matcher<Any> {
        return object : BoundedMatcher<Any, CollapsingToolbarLayout>(CollapsingToolbarLayout::class.java) {
            public override fun matchesSafely(toolbar: CollapsingToolbarLayout): Boolean {
                return textMatcher.matches(toolbar.title)
            }

            override fun describeTo(description: Description) {
                description.appendText("with collapsing toolbar title: ")
                textMatcher.describeTo(description)
            }
        }
    }
}
