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
import cz.josefadamcik.trackontrakt.data.api.TraktAuthTokenHolder
import cz.josefadamcik.trackontrakt.data.api.model.MediaType
import cz.josefadamcik.trackontrakt.data.api.model.MovieDetail
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class MediaDetailPresenter @Inject constructor(
    val traktApi: TraktApi,
    val tokenHolder: TraktAuthTokenHolder
) : BasePresenter<MediaDetailView>() {


    fun load(mediaId: MediaIdentifier?, name: String?) {
        Timber.i("load: %s", mediaId)

        if (mediaId == null) {
            //fixme: error
            return
        }

        if (name != null) {
            view?.showTitle(name)
        }

        view?.showItemCheckInActionVisible(mediaId.type == MediaType.movie)

        if (mediaId.type == MediaType.movie) {
            view?.showLoading()
            disposables.add(
                traktApi.movie(tokenHolder.httpAuth(), mediaId.id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { movie ->
                            view?.hideLoading()
                            if (movie != null) {
                                showMovie(movie)
                            }
                        },
                        { t ->
                            view?.hideLoading()
                            view?.showError(t)
                        }

                    )
            )
        }

    }

    private fun showMovie(movie: MovieDetail) {
        view?.showTextInfo(movie.tagline, movie.overview)
    }
}