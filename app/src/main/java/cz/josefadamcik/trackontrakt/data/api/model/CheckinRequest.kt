
package cz.josefadamcik.trackontrakt.data.api.model

import cz.josefadamcik.trackontrakt.BuildConfig

data class CheckinRequest(
    val movie: Movie? = null,
    val episode: Episode? = null,
    val message: String? = null,
    val sharing: Sharing? = null,
    val app_version: String = BuildConfig.VERSION_NAME,
    val app_date: String = BuildConfig.BUILD_DATE
) {
    data class Sharing(val facebook: Boolean, val twitter: Boolean, val thumblr: Boolean)
}