package cz.josefadamcik.trackontrakt.welcome


import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.intent.Intents
import android.support.test.espresso.intent.matcher.IntentMatchers
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.runner.AndroidJUnit4
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import cz.josefadamcik.trackontrakt.BuildConfig
import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.data.api.TestTraktAuthTokenHolder
import cz.josefadamcik.trackontrakt.home.HomeActivity
import cz.josefadamcik.trackontrakt.testutil.WiremockTimberNotifier
import cz.josefadamcik.trackontrakt.testutil.activityTestRule
import cz.josefadamcik.trackontrakt.traktauth.TraktAuthActivity
import org.hamcrest.Matchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WelcomeActivityTestCase {

    @get:Rule
    val activityTestRule = activityTestRule<WelcomeActivity>()

    @get:Rule
    var wireMockRule = WireMockRule(WireMockConfiguration.options()
            .port(BuildConfig.MOCKSERVER_PORT)
            .notifier(WiremockTimberNotifier)
    )

    @Test
    fun showActivityUnauthorized() {
        //given
        TestTraktAuthTokenHolder.instance.authorized = false
        val appContext = InstrumentationRegistry.getTargetContext()

        //when about is launched
        val activity = activityTestRule.launchActivity(Intent(appContext, WelcomeActivity::class.java))


        onView(withId(R.id.login_button))
                .check(matches(isDisplayed()))
                .perform(ViewActions.click())



        Intents.intended(Matchers.allOf(IntentMatchers.hasComponent(TraktAuthActivity::class.java.name)))
    }

    @Test
    fun showActivityAuthorized() {
        //given
        TestTraktAuthTokenHolder.instance.authorized = true
        val appContext = InstrumentationRegistry.getTargetContext()


        //when about is launched
        val activity = activityTestRule.launchActivity(Intent(appContext, WelcomeActivity::class.java))


        //than app should go directly do home activity
        Intents.intended(Matchers.allOf(IntentMatchers.hasComponent(HomeActivity::class.java.name)))
    }
}
