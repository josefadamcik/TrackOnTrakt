
package cz.josefadamcik.trackontrakt.traktauth

import com.hannesdorfmann.mosby3.mvp.MvpView

interface TraktAuthView : MvpView {
    fun requestLoginToTraktInBrowser(url: String);
    fun showErrorMessageWithRetry(messageId: Int)
    /**
     * Goto next activity.
     */
    fun continueNavigation()

    fun showErrorView()
    fun showProgress()
    fun hideProgress()
}