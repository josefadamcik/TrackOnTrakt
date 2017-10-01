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
package cz.josefadamcik.trackontrakt.detail

import cz.josefadamcik.trackontrakt.data.api.model.Episode
import cz.josefadamcik.trackontrakt.data.api.model.Season
import java.text.SimpleDateFormat
import java.util.*

data class MediaDetailModel(
    val basic: MediaDetailInfo,
    val latestEpisode: Episode? = null,
    val seasons: List<Season> = emptyList()
) {


    data class MediaDetailInfo(
        val tagline: String? = null,
        val description: String?,
        val homepage: String?,
        val traktPage: String,
        val rating: Double,
        val certification: String?,
        val votes: Long,
        val date: Date?,
        val network: String? = null,
        val genres: List<String> = emptyList(),
        val trailer: String? = null,
        val status: String? = null,
        val language: String? = null
    ) {

        companion object {
            val YEAR_FORMAT = SimpleDateFormat("YYYY", Locale.getDefault())
        }

        val year: CharSequence get() = if (date == null) "" else YEAR_FORMAT.format(date)
    }
}