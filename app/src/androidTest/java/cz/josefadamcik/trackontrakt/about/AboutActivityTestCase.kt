package cz.josefadamcik.trackontrakt.about


import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.runner.AndroidJUnit4
import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.testutil.activityTestRule
import junit.framework.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AboutActivityTestCase {

    @get:Rule
    val activityTestRule = activityTestRule<AboutActivity>()


    @Test
    fun showActivity() {
        //given
        val appContext = InstrumentationRegistry.getTargetContext()

        //when about is launched
        val activity = activityTestRule.launchActivity(Intent(appContext, AboutActivity::class.java))

        onView(withContentDescription(R.string.abc_action_bar_up_description))
                .check(matches(isDisplayed()))
        onView(withText(R.string.about))
                .check(matches(isDisplayed()))
    }

    @Test
    fun navUpClicked() {
        //given
        val appContext = InstrumentationRegistry.getTargetContext()

        //when about is launched
        val activity = activityTestRule.launchActivity(Intent(appContext, AboutActivity::class.java))

        onView(withContentDescription(R.string.abc_action_bar_up_description))
                .check(matches(isDisplayed()))
                .perform(click())

        assertTrue(activityTestRule.activity.isFinishing)
    }

}
