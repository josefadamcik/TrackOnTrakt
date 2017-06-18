package cz.josefadamcik.trackontrakt.detail


import android.content.Context
import android.support.design.widget.CollapsingToolbarLayout
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
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
    fun showDetailTest() {
        //arrange
        val appContext = InstrumentationRegistry.getTargetContext()
        arrangeApiStubForShowDetail(appContext)
        arrangeApiStubForLatestEpisode(appContext)
        arrangeApiStubForShowSeasons(appContext)

        //act: launch activity

        val intent = MediaDetailActivity.createIntent(appContext, MediaIdentifier(MediaType.show, 69829), "Rick and Morty")
        activityTestRule.launchActivity(intent)

        //assert: info rendered

        assertActionBarTitle("Rick and Morty")
        assertDescriptionViewValue("A sociopathic scientist drags his unintelligent grandson on insanely dangerous adventures across the universe.")

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

    @Test
    fun movieDetailTest() {
        val detailName = "Mad Max: Fury Road"

        //arrange
        val appContext = InstrumentationRegistry.getTargetContext()
        arrangeApiStubForMovieDetail(appContext)

        //act: launch activity
        val intent = MediaDetailActivity.createIntent(appContext, MediaIdentifier(MediaType.movie, 56360), detailName)
        activityTestRule.launchActivity(intent)

        //assert info rendered
        assertActionBarTitle(detailName)
        assertDescriptionViewValue("An apocalyptic story set in the furthest reaches of our planet, in a stark desert landscape where humanity is broken, and most everyone is crazed fighting for the necessities of life. Within this world exist two rebels on the run who just might be able to restore order. There's Max, a man of action and a man of few words, who seeks peace of mind following the loss of his wife and child in the aftermath of the chaos. And Furiosa, a woman of action and a woman who believes her path to survival may be achieved if she can make it across the desert back to her childhood homeland.")
        //and checkin button displayed
        onView(withId(R.id.fab)).check(matches(isDisplayed()))
    }

    @Test
    fun movieCheckinTest() {
        //arrange
        val appContext = InstrumentationRegistry.getTargetContext()
        arrangeApiStubForMovieDetail(appContext)
        arrangeApiStubForCheckin(appContext)

        //act: launch activity
        val intent = MediaDetailActivity.createIntent(appContext, MediaIdentifier(MediaType.movie, 56360), "name")
        activityTestRule.launchActivity(intent)

        onView(allOf(
            withId(R.id.fab),
            isDisplayed()
        )).perform(click())


        //assert: checkin request received
        verify(postRequestedFor(urlPathEqualTo("/checkin")))
    }

    private fun arrangeApiStubForCheckin(appContext: Context) {
        wireMockRule.stubFor(
            post(urlPathEqualTo("/checkin"))
                .withRequestBody(matchingJsonPath("$.movie.ids.trakt"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(asset(appContext, "checkin.json"))
                )
        )
    }

    private fun arrangeApiStubForMovieDetail(appContext: Context) {
        wireMockRule.stubFor(
            get(urlPathEqualTo("/movies/56360"))
                .withQueryParam("extended", equalTo("full"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(asset(appContext, "movies_56360_extended_full.json"))
                )

        )
    }

    private fun arrangeApiStubForShowSeasons(appContext: Context) {
        wireMockRule.stubFor(
            get(urlPathEqualTo("/shows/69829/seasons"))
                .withQueryParam("extended", equalTo("episodes"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(asset(appContext, "shows_69829_seasons_extendedepisodes.json"))
                )
        )
    }

    private fun arrangeApiStubForLatestEpisode(appContext: Context) {
        wireMockRule.stubFor(
            get(urlPathEqualTo("/shows/69829/last_episode"))
                .withQueryParam("extended", equalTo("full"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(asset(appContext, "shows_69829_last_episode_extended_full.json"))
                )
        )
    }

    private fun arrangeApiStubForShowDetail(appContext: Context) {
        wireMockRule.stubFor(
            get(urlPathEqualTo("/shows/69829"))
                .withQueryParam("extended", equalTo("full"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(asset(appContext, "shows_69829_extendedfull.json"))
                )

        )
    }

    private fun assertActionBarTitle(title: String) {
        onView(isAssignableFrom(CollapsingToolbarLayout::class.java))
            .check(matches(withCollapsingToolbarTitle(`is`(title))))
    }


    private fun assertDescriptionViewValue(showDescription: String) {
        val textView = onView(
            allOf(
                withId(R.id.txt_description),
                isDisplayed()
            )
        )
        textView.check(matches(withText(showDescription)))
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
