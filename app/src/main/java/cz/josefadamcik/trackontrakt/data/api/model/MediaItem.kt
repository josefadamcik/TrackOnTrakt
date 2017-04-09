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

interface MediaItem {
    val type: MediaType
    val movie: Movie?
    val episode: Episode?
    val show: Show?

    val title: String
        get() {
            return when (type) {
                MediaType.episode -> String.format("%s - S %s, Ep %s", show?.title, episode?.season, episode?.number)
                MediaType.show -> show?.title ?: ""
                MediaType.movie -> movie?.title ?: ""
            }
        }

    val subtitle: String
        get() {
            return when (type) {
                MediaType.episode -> episode?.title ?: ""
                else -> ""
            }
        }

    val year: Int?
        get() {
            return when (type) {
                MediaType.episode -> show?.year
                MediaType.show -> show?.year
                MediaType.movie -> movie?.year
            }
        }
    val traktId: Long?
        get() {
            return when (type) {
                MediaType.episode -> episode?.ids?.trakt
                MediaType.show -> show?.ids?.trakt
                MediaType.movie -> movie?.ids?.trakt
            }
        }
}
