
package cz.josefadamcik.trackontrakt.data.api

import cz.josefadamcik.trackontrakt.ApplicationScope
import cz.josefadamcik.trackontrakt.data.api.model.Settings
import cz.josefadamcik.trackontrakt.util.RxSchedulers
import io.reactivex.Single
import javax.inject.Inject


@ApplicationScope
class UserAccountManager
@Inject constructor(
    private val traktApi: TraktApi,
    private val authTokenProvider: TraktAuthTokenProvider,
    private val rxSchedulers: RxSchedulers
) {
    /**
     * Produces IllegalStateException when not authorised (missing auth token).
     */
    fun obtainUserSettings(): Single<Settings> {
        if (authTokenProvider.hasToken().not()) {
            return Single.error(IllegalStateException("Missing authorisation token"));
        } else {
            return traktApi.userSettings(authTokenProvider.httpAuth())
                .subscribeOn(rxSchedulers.subscribe)
                .observeOn(rxSchedulers.observe)
        }
    }


}