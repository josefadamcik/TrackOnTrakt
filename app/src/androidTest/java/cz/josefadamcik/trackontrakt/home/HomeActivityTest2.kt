package cz.josefadamcik.trackontrakt.home


import android.support.test.InstrumentationRegistry.getInstrumentation
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.test.suitebuilder.annotation.LargeTest
import android.view.View
import android.view.ViewGroup
import cz.josefadamcik.trackontrakt.R
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsInstanceOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class HomeActivityTest2 {

    @get:Rule
    var mActivityTestRule = ActivityTestRule(HomeActivity::class.java)

    @Test
    fun homeActivityTest2() {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)

        val appCompatTextView = onView(
                allOf(withId(R.id.title), withText("Settings"), isDisplayed()))
        appCompatTextView.perform(click())

        val floatingActionButton = onView(
                allOf(withId(R.id.fab), isDisplayed()))
        floatingActionButton.perform(click())

        val textView = onView(
                allOf(withId(R.id.textView), withText("Hello, World!"),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf<View>(android.view.ViewGroup::class.java),
                                        1),
                                0),
                        isDisplayed()))
        textView.check(matches(withText("Hello, World!")))

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
