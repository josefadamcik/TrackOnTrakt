
package cz.josefadamcik.trackontrakt.testutil

import android.view.View
import android.view.ViewGroup
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher


public fun childAtPosition(
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


public fun <T> first(matcher: Matcher<T>): Matcher<T> {
    return object : BaseMatcher<T>() {
        internal var isFirst = true

        override fun matches(item: Any): Boolean {
            if (isFirst && matcher.matches(item)) {
                isFirst = false
                return true
            }

            return false
        }

        override fun describeTo(description: Description) {
            description.appendText("the first matching item of a: (")
            description.appendDescriptionOf(matcher)
            description.appendText(")")
        }
    }
}