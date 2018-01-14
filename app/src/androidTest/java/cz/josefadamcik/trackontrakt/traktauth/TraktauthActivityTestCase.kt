package cz.josefadamcik.trackontrakt.traktauth


import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.runner.AndroidJUnit4
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import cz.josefadamcik.trackontrakt.BuildConfig
import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.data.api.TestTraktAuthTokenHolder
import cz.josefadamcik.trackontrakt.testutil.WiremockTimberNotifier
import cz.josefadamcik.trackontrakt.testutil.activityTestRule
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TraktauthActivityTestCase {

    @get:Rule
    val activityTestRule = activityTestRule<TraktAuthActivity>()

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

        wireMockRule.stubFor(
            get(urlPathEqualTo("/loginform"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/html; charset=utf-8")
                        .withBody("<form method='get' action='oauth://test'><submit value='SUBMIT' ></form>")
                )

        )
        wireMockRule.stubFor(
            get(urlPathEqualTo("/favicon.ico"))
                .willReturn(aResponse().withStatus(404)
                )
        )

        //when about is launched
        val activity = activityTestRule.launchActivity(Intent(appContext, TraktAuthActivity::class.java))

        assertNotNull(activity)

        onView(withId(R.id.webview))
                .check(matches(isDisplayed()))

        verify(getRequestedFor(urlPathEqualTo("/loginform")))
    }

    @Test
    fun showActivityAuthorized() {
        //given
        TestTraktAuthTokenHolder.instance.authorized = true
        val appContext = InstrumentationRegistry.getTargetContext()


        //when about is launched
        val activity = activityTestRule.launchActivity(Intent(appContext, TraktAuthActivity::class.java))

        //than app should go directly do home activity
        //doesn't work, I am not sure why
        //Intents.intended(Matchers.allOf(IntentMatchers.hasComponent(HomeActivity::class.java.name)))
    }



}
