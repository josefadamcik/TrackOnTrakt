package cz.josefadamcik.trackontrakt.detail


import android.content.Context
import android.support.design.widget.CollapsingToolbarLayout
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.matcher.BoundedMatcher
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.runner.AndroidJUnit4
import android.view.View
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.github.tomakehurst.wiremock.matching.StringValuePattern
import com.schibsted.spain.barista.interaction.BaristaSleepInteractions
import cz.josefadamcik.trackontrakt.BuildConfig
import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.data.api.model.MediaType
import cz.josefadamcik.trackontrakt.testutil.WiremockTimberNotifier
import cz.josefadamcik.trackontrakt.testutil.activityTestRule
import cz.josefadamcik.trackontrakt.testutil.asset
import cz.josefadamcik.trackontrakt.testutil.first
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
        //given
        val appContext = InstrumentationRegistry.getTargetContext()
        val showName = "Test Show Name"
        givenApiStubForShowDetail(appContext)
        givenApiStubForLatestEpisode(appContext)
        givenApiStubForShowSeasons(appContext)

        //when: launch activity
        val intent = MediaDetailActivity.createIntent(appContext, MediaIdentifier(MediaType.show, 69829), showName)
        activityTestRule.launchActivity(intent)

        //than
        //assert: info rendered
        assertActionBarTitle(showName)
        assertDescriptionViewValue("A sociopathic scientist drags his unintelligent grandson on insanely dangerous adventures across the universe.")

        //Scrool action below probably doesnt work due the CoordinatorLayout setup we have here. So we do simple swipe up
        //to work around the issue.
        onView(withId(android.R.id.content)).perform(ViewActions.swipeUp())
        onView(withId(R.id.list))
            .check(matches(isDisplayed()))
            .perform(RecyclerViewActions.scrollTo<MediaDetailAdapter.ViewHolder>(hasDescendant(withText(R.string.media_detail_next_episode_separator))))


        val textView2 = onView(allOf(
            isDescendantOfA(withId(R.id.list)),
            isDisplayed(),
            withText(R.string.media_detail_next_episode_separator)
        ))

        textView2.check(matches(withText("Next to watch")))
    }

    @Test
    fun showEpisodeCheckinTest() {
        //given
        val appContext = InstrumentationRegistry.getTargetContext()
        val showName = "Test Show Name"
        givenApiStubForShowDetail(appContext)
        givenApiStubForLatestEpisode(appContext)
        givenApiStubForShowSeasons(appContext)
        givenApiStubForEpisodeCheckin(appContext)

        //when: launch activity

        val intent = MediaDetailActivity.createIntent(appContext, MediaIdentifier(MediaType.show, 69829), showName)
        activityTestRule.launchActivity(intent)

        //Scrool action below probably doesnt work due the CoordinatorLayout setup we have here. So we do simple swipe up
        //to work around the issue.
        onView(withId(android.R.id.content)).perform(ViewActions.swipeUp())

        val unwatchedEpisodeName = "Close MainCharacter-Counters of the MainCharacter Kind"
        //when we scroll to the episode item in the list
        onView(withId(R.id.list))
            .check(matches(isDisplayed()))
            .perform(
                RecyclerViewActions.scrollTo<MediaDetailAdapter.EpisodeInfoViewHolder>(
                    first(hasDescendant(allOf(withId(R.id.title), withText(unwatchedEpisodeName))))
                )
            )


        //and click on it
        onView(allOf(withId(R.id.btn_checkin), isDescendantOfA(isEpisodeRow(unwatchedEpisodeName))))
            .check(matches(isDisplayed()))
            .perform(click())

        BaristaSleepInteractions.sleep(1000)

        //than
        //assert: checkin request received
        verify(postRequestedFor(urlPathEqualTo("/checkin")))
    }

    private fun isEpisodeRow(episodeName: String): Matcher<View>? {
        return allOf(
            isDisplayed(),
            isDescendantOfA(withId(R.id.list)),
            hasDescendant(allOf(withId(R.id.title), withText(episodeName)))
        )
    }

    @Test
    fun movieDetailTest() {
        //given
        val detailName = "Mad Jack: Fury Road"
        val appContext = InstrumentationRegistry.getTargetContext()
        givenApiStubForMovieDetail(appContext)

        //when: launch activity
        val intent = MediaDetailActivity.createIntent(appContext, MediaIdentifier(MediaType.movie, 56360), detailName)
        activityTestRule.launchActivity(intent)

        //than
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
        givenApiStubForMovieDetail(appContext)
        givenApiStubForMovieCheckin(appContext)

        //when: launch activity
        val intent = MediaDetailActivity.createIntent(appContext, MediaIdentifier(MediaType.movie, 56360), "name")
        activityTestRule.launchActivity(intent)

        onView(allOf(
            withId(R.id.fab),
            isDisplayed()
        )).perform(click())


        //than
        //assert: checkin request received
        verify(postRequestedFor(urlPathEqualTo("/checkin")))
    }

    private fun givenApiStubForMovieCheckin(appContext: Context) {
        arranegApiStubForCheckin(appContext, matchingJsonPath("$.movie.ids.trakt"))
    }

    private fun givenApiStubForEpisodeCheckin(appContext: Context) {
        arranegApiStubForCheckin(appContext, matchingJsonPath("$.episode.ids.trakt"))
    }

    private fun arranegApiStubForCheckin(appContext: Context, bodyPattern: StringValuePattern) {
        wireMockRule.stubFor(
            post(urlPathEqualTo("/checkin"))
                .withRequestBody(bodyPattern)
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(asset(appContext, "checkin.json"))
                )
        )
    }


    private fun givenApiStubForMovieDetail(appContext: Context) {
        wireMockRule.stubFor(
            get(urlPathEqualTo("/movies/56360"))
                .withQueryParam("extended", equalTo("full"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(asset(appContext, "movies_56360_extended_full.json"))
                )

        )
    }

    private fun givenApiStubForShowSeasons(appContext: Context) {
        wireMockRule.stubFor(
            get(urlPathEqualTo("/shows/69829/seasons"))
                .withQueryParam("extended", equalTo("full"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(asset(appContext, "shows_69829_seasons_extendedfull.json"))
                )
        )

        (0..3).forEach { seasonIndex ->
            wireMockRule.stubFor(
                get(urlPathEqualTo("/shows/69829/seasons/$seasonIndex"))
                    .withQueryParam("extended", equalTo("full"))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(asset(appContext, "shows_69829_seasons_${seasonIndex}_extendedfull.json"))
                    )
            )
        }

        wireMockRule.stubFor(
            get(urlPathEqualTo("/shows/69829/progress/watched"))
                .withQueryParam("hidden", equalTo("false"))
                .withQueryParam("specials", equalTo("true"))
                .withQueryParam("count_specials", equalTo("false"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(asset(appContext, "shows_69829_progress.json"))
                )
        )
    }

    private fun givenApiStubForLatestEpisode(appContext: Context) {
        wireMockRule.stubFor(
            get(urlPathEqualTo("/shows/69829/last_episode"))
                .withQueryParam("extended", equalTo("full"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(asset(appContext, "shows_69829_last_episode_extended_full.json"))
                )
        )
    }

    private fun givenApiStubForShowDetail(appContext: Context) {
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
                description.appendText("with collapsing toolbar text: ")
                textMatcher.describeTo(description)
            }
        }
    }
}
