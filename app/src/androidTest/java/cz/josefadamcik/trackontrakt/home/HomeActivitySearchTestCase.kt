package cz.josefadamcik.trackontrakt.home


import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.pressImeActionButton
import android.support.test.espresso.action.ViewActions.replaceText
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.runner.AndroidJUnit4
import android.view.View
import android.view.ViewGroup
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import cz.josefadamcik.trackontrakt.ApplicationModule
import cz.josefadamcik.trackontrakt.BuildConfig
import cz.josefadamcik.trackontrakt.DaggerTestApplicationComponent
import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.data.api.TestApiModule
import cz.josefadamcik.trackontrakt.testutil.AssetReaderUtil
import cz.josefadamcik.trackontrakt.testutil.ComponentActivityTestRule
import cz.josefadamcik.trackontrakt.testutil.WiremockTimberNotifier
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsInstanceOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeActivitySearchTestCase {

    @get:Rule
    var wireMockRule = WireMockRule(WireMockConfiguration.options()
        .port(BuildConfig.MOCKSERVER_PORT)
        .notifier(WiremockTimberNotifier)
    )

    @get:Rule
    val activityTestRule = ComponentActivityTestRule<HomeActivity>(HomeActivity::class.java, { app ->
        app.component = DaggerTestApplicationComponent.builder()
            .applicationModule(ApplicationModule(app))
            .testApiModule(TestApiModule(app))
            .build()
    })


    @Test
    fun homeActivitySearchTest() {
        val appContext = InstrumentationRegistry.getTargetContext()

        wireMockRule.stubFor(
            get(urlMatching("/users/me/history.*"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(AssetReaderUtil.asset(appContext, "history.json"))
                )
        )

        wireMockRule.stubFor(
            get(urlMatching("/search/.*"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(AssetReaderUtil.asset(appContext, "search_ricknmorty.json"))
                )
        )


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
        searchEditText.perform(replaceText("rick and morty"), pressImeActionButton())

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(500)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

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
                            IsInstanceOf.instanceOf<View>(android.widget.LinearLayout::class.java),
                            0)),
                    1),
                isDisplayed()))
        editText.check(matches(withText("rick and morty")))

    }

    private fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                    && view == parent.getChildAt(position)
            }
        }
    }
}
