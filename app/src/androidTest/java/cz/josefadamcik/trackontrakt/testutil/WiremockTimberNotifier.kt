

package cz.josefadamcik.trackontrakt.testutil

import com.github.tomakehurst.wiremock.common.Notifier
import timber.log.Timber

object WiremockTimberNotifier : Notifier {
    override fun info(s: String) {
        Timber.i(s)
    }

    override fun error(s: String) {
        Timber.e(s)
    }

    override fun error(s: String, throwable: Throwable) {
        Timber.e(throwable, s)
    }
}
