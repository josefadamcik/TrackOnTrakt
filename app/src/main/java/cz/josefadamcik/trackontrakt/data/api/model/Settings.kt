
package cz.josefadamcik.trackontrakt.data.api.model

/**
 * Returned by @ http://docs.trakt.apiary.io/#reference/users/settings/retrieve-settings.
 * Incomplete.
 *
 * TODO: add sharing related stuff, avatar and so on
 *
 */

data class Settings(
    val user: User,
    val account: Account
)

