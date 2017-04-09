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
package cz.josefadamcik.trackontrakt.search

import paperparcel.PaperParcel
import paperparcel.PaperParcelable

@PaperParcel
data class TraktFilter(
    val movies: Boolean,
    val shows: Boolean
): PaperParcelable {
    companion object {
        @JvmField val CREATOR = PaperParcelTraktFilter.CREATOR
    }

    fun forApiQuery(): String {
        if (!movies && !shows) {
            throw IllegalArgumentException("at least on of movies, shows should be true")
        }
        val types = mutableListOf<String>()
        if (movies) types.add("movie")
        if (shows) types.add("show")
        return types.joinToString(",")
    }
}