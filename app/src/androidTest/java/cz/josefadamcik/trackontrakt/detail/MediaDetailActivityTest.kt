package cz.josefadamcik.trackontrakt.detail


//import com.facebook.testing.screenshot.Screenshot
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
import android.support.test.runner.screenshot.Screenshot
import android.view.View
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.github.tomakehurst.wiremock.matching.StringValuePattern
import com.squareup.spoon.SpoonRule
import cz.josefadamcik.trackontrakt.BuildConfig
import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.data.api.model.MediaType
import cz.josefadamcik.trackontrakt.testutil.*
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneOffset


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
        givenApiStubsForShowDetail(appContext)

        //when: launch activity
        whenActivityForShowDetailIsLaunched(appContext, showName)

        //than
        //assert: info rendered
        assertActionBarTitle(showName)
        assertDescriptionViewValue("A sociopathic scientist drags his unintelligent grandson on insanely dangerous adventures across the universe.")

        whenSwipedUp()
        onView(withId(R.id.list))
            .check(matches(isDisplayed()))
            .perform(RecyclerViewActions.scrollTo<MediaDetailAdapter.ViewHolder>(hasDescendant(withText(R.string.media_detail_next_episode_separator))))


        val textView2 = onView(allOf(
            isDescendantOfA(withId(R.id.list)),
            isDisplayed(),
            withText(R.string.media_detail_next_episode_separator)
        ))

        textView2.check(matches(withText("Next to watch")))


        activityTestRule.screenshot("finished")
    }

    @Test
    fun showEpisodeCheckinTest() {
        //given
        val appContext = InstrumentationRegistry.getTargetContext()
        val showName = "Test Show Name"
        val unwatchedEpisodeName = "Close MainCharacter-Counters of the MainCharacter Kind"
        givenApiStubsForShowDetail(appContext)
        givenApiStubForEpisodeCheckin(appContext)

        //when
        whenActivityForShowDetailIsLaunched(appContext, showName)
        whenSwipedUp()
        whenScrolledToEpisodeItem(unwatchedEpisodeName)
        whenCheckinIconForEpisodeIsClickedOn(unwatchedEpisodeName)
        thanCheckinDialogIsDisplayed()
        whenCheckinIsConfirmed()

        //than
        //assert: checkin request received
        verify(postRequestedFor(urlPathEqualTo("/checkin")))
    }

    @Test
    fun showEpisodeCheckinInThePastTest() {
        //given
        TestTimeProvider.clock = Clock.fixed(Instant.parse("2017-05-07T22:00:00Z"), ZoneOffset.UTC)
        val appContext = InstrumentationRegistry.getTargetContext()
        val showName = "Test Show Name"
        val unwatchedEpisodeName = "Close MainCharacter-Counters of the MainCharacter Kind"
        givenApiStubsForShowDetail(appContext)
        givenApiStubForAddEpisodeToHistory(appContext)

        //when
        whenActivityForShowDetailIsLaunched(appContext, showName)
        whenSwipedUp()
        whenScrolledToEpisodeItem(unwatchedEpisodeName)
        whenCheckinIconForEpisodeIsClickedOn(unwatchedEpisodeName)
        thanCheckinDialogIsDisplayed()
        whenCheckinInThePastIsChosen()
        whenDateIsClicked()
        whenDateDialogIsDisplayedAndClosed()
        whenTimeIsClicked()
        whenDateDialogIsDisplayedAndClosed()
        whenCheckinIsConfirmed()

        //than
        //assert: checkin request received
        verify(postRequestedFor(urlPathEqualTo("/sync/history")))
    }

    @Test
    fun movieDetailTest() {
        //given
        val detailName = "Mad Jack: Fury Road"
        val appContext = InstrumentationRegistry.getTargetContext()
        givenApiStubForMovieDetail(appContext)

        //when: launch activity
        whenActivityForMovieIsLaunched(appContext, detailName)

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

        //when
        whenActivityForMovieIsLaunched(appContext, "name")
        whenCheckinFabIsClicked()
        thanCheckinDialogIsDisplayed()
        whenCheckinIsConfirmed()

        //than
        //assert: checkin request received
        verify(postRequestedFor(urlPathEqualTo("/checkin")))
    }

    @Test
    fun movieCheckinInThePastTest() {
        //arrange
        val appContext = InstrumentationRegistry.getTargetContext()
        givenApiStubForMovieDetail(appContext)
        givenApiStubForAddMovieToHistory(appContext)

        //when
        whenActivityForMovieIsLaunched(appContext, "name")
        whenCheckinFabIsClicked()
        thanCheckinDialogIsDisplayed()
        whenCheckinInThePastIsChosen()
        whenDateIsClicked()
        whenDateDialogIsDisplayedAndClosed()
        whenTimeIsClicked()
        whenDateDialogIsDisplayedAndClosed()
        whenCheckinIsConfirmed()

        //than
        //assert: checkin request received
        verify(postRequestedFor(urlPathEqualTo("/sync/history")))
    }



    private fun whenDateDialogIsDisplayedAndClosed() {
        onView(withId(R.id.mdtp_ok))
                .check(matches(isDisplayed()))
                .perform(click())
    }


    private fun whenDateIsClicked() {
        onView(withId(R.id.txt_date))
                .check(matches(isDisplayed()))
                .perform(click())
    }
    private fun whenTimeIsClicked() {
        onView(withId(R.id.txt_time))
                .check(matches(isDisplayed()))
                .perform(click())
    }

    private fun whenCheckinInThePastIsChosen() {
        onView(withId(R.id.rb_inpast))
                .check(matches(isDisplayed()))
                .perform(click())
    }


    private fun whenCheckinIsConfirmed() {
        onView(withText(R.string.dialog_checkin))
                .perform(click())
    }

    private fun thanCheckinDialogIsDisplayed() {
        onView(withText(R.string.checkin_dialog_question))
                .check(matches(isDisplayed()))
    }

    private fun whenCheckinFabIsClicked() {
        onView(allOf(
                withId(R.id.fab),
                isDisplayed()
        )).perform(click())
    }

    private fun whenActivityForMovieIsLaunched(appContext: Context, detailName: String) {
        val intent = MediaDetailActivity.createIntent(appContext, MediaIdentifier(MediaType.movie, 56360), detailName)
        activityTestRule.launchActivity(intent)
    }

    private fun givenApiStubForMovieCheckin(appContext: Context) {
        givenApiStubForCheckin(appContext, matchingJsonPath("$.movie.ids.trakt"))
    }

    private fun givenApiStubForEpisodeCheckin(appContext: Context) {
        givenApiStubForCheckin(appContext, matchingJsonPath("$.episode.ids.trakt"))
    }

    private fun givenApiStubForAddMovieToHistory(appContext: Context) {
        givenApiStubForAddToHistory(appContext,
                matchingJsonPath("$.movies[0].ids.trakt"),
                "history_add_movie.json")
    }

    private fun givenApiStubForAddEpisodeToHistory(appContext: Context) {
        givenApiStubForAddToHistory(appContext,
                matchingJsonPath("$.episodes[0].ids.trakt"),
                "history_add_episode.json")
    }

    private fun givenApiStubForCheckin(appContext: Context, bodyPattern: StringValuePattern) {
        wireMockRule.stubFor(
            post(urlPathEqualTo("/checkin"))
                .withRequestBody(bodyPattern)
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(asset(appContext, "checkin.json"))
                )
        )
    }
    private fun givenApiStubForAddToHistory(appContext: Context, bodyPattern: StringValuePattern, responseFileName: String) {
        wireMockRule.stubFor(
                post(urlPathEqualTo("/sync/history"))
                        .withRequestBody(bodyPattern)
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withBody(asset(appContext, responseFileName))
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

    private fun whenActivityForShowDetailIsLaunched(appContext: Context, showName: String) {
        val intent = MediaDetailActivity.createIntent(appContext, MediaIdentifier(MediaType.show, 69829), showName)
        activityTestRule.launchActivity(intent)
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

    private fun whenScrolledToEpisodeItem(episodeName: String) {
        //when we scroll to the episode item in the list
        onView(withId(R.id.list))
                .check(matches(isDisplayed()))
                .perform(
                        RecyclerViewActions.scrollTo<MediaDetailAdapter.EpisodeInfoViewHolder>(
                                first(hasDescendant(allOf(withId(R.id.title), withText(episodeName))))
                        )
                )
    }

    private fun whenSwipedUp() {
        //Scroll action  probably doesn't work due the CoordinatorLayout setup we have here. So we do simple swipe up
        //to work around the issue.
        onView(withId(android.R.id.content)).perform(ViewActions.swipeUp())
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

    private fun givenApiStubsForShowDetail(appContext: Context) {
        givenApiStubForShowDetail(appContext)
        givenApiStubForLatestEpisode(appContext)
        givenApiStubForShowSeasons(appContext)
    }

    private fun whenCheckinIconForEpisodeIsClickedOn(episodeName: String) {
        onView(allOf(withId(R.id.btn_checkin), isDescendantOfA(isEpisodeRow(episodeName))))
                .check(matches(isDisplayed()))
                .perform(click())
    }



    private fun isEpisodeRow(episodeName: String): Matcher<View>? {
        return allOf(
                isDisplayed(),
                isDescendantOfA(withId(R.id.list)),
                hasDescendant(allOf(withId(R.id.title), withText(episodeName)))
        )
    }

}
