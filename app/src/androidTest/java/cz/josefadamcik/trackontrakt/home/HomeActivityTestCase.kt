package cz.josefadamcik.trackontrakt.home


import android.content.Context
import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import cz.josefadamcik.trackontrakt.ApplicationModule
import cz.josefadamcik.trackontrakt.BuildConfig
import cz.josefadamcik.trackontrakt.DaggerTestApplicationComponent
import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.data.api.TestApiModule
import cz.josefadamcik.trackontrakt.testutil.AssetReaderUtil.asset
import cz.josefadamcik.trackontrakt.testutil.ComponentActivityTestRule
import cz.josefadamcik.trackontrakt.testutil.RecyclerViewRowMatcher.atPosition
import cz.josefadamcik.trackontrakt.testutil.WiremockTimberNotifier
import org.junit.Rule
import org.junit.Test
import timber.log.Timber


class HomeActivityTestCase {

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
    fun homeActivityTestShowHistory() {
        Timber.d("homeActivityTestShowHistory %s", wireMockRule.isRunning)
        val appContext = InstrumentationRegistry.getTargetContext()
        // API call
        withApiStubForHistory(appContext)

        //launch activity
        val activity = activityTestRule.launchActivity(Intent(appContext, HomeActivity::class.java))

        onView(withId(R.id.list))
            .check(matches(atPosition(0, hasDescendant(withText("Black Books - S 2, Ep 1")))))

    }

    private fun withApiStubForHistory(appContext: Context) {
        wireMockRule.stubFor(
            get(urlMatching("/users/me/history.*"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(asset(appContext, "history.json"))
                )
        )
    }


}
