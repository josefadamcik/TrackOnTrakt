/*
 Copyright 2017 Josef Adamcik <josef.adamcik@gmail.com>

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
package cz.josefadamcik.trackontrakt.data.api.model

/**
 * Returned by @ http://docs.trakt.apiary.io/#reference/users/settings/retrieve-settings.
 * Incomplete.
 *
 * TODO: add sharing related stuff, avatar and so on
 *
 */
data class UserSettings(
    val username: String,
    val private: Boolean,
    val vip: Boolean,
    val vip_ep: Boolean,
    val ids: UserSettingsIds,
    val images: Map<String, Image>,
    val account: UserAccount
) {
    companion object {
        const val IMAGES_KEY_AVATAR = "avatar"
    }
}

data class UserSettingsIds(
    val slug: String
)

data class Image(
    val full: String
)

data class UserAccount(
    val cover_image: String
)
