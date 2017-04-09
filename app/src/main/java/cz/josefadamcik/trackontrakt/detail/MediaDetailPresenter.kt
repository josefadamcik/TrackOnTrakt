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

import cz.josefadamcik.trackontrakt.base.BasePresenter
import cz.josefadamcik.trackontrakt.data.api.TraktApi
import cz.josefadamcik.trackontrakt.data.api.model.MediaType
import timber.log.Timber
import javax.inject.Inject

class MediaDetailPresenter @Inject constructor(
    val traktApi: TraktApi
) : BasePresenter<MediaDetailView>() {


    fun load(mediaId: MediaIdentifier?, name: String?) {
        Timber.i("load: %s", mediaId)

        if (name != null) {
            view?.showTitle(name)
        }

        view?.showItemCheckInActionVisible(mediaId?.type == MediaType.movie)

        //disposables.
    }
}