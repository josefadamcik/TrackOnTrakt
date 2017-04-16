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

import cz.josefadamcik.trackontrakt.BuildConfig
import cz.josefadamcik.trackontrakt.base.BasePresenter
import cz.josefadamcik.trackontrakt.data.api.TraktApi
import cz.josefadamcik.trackontrakt.data.api.TraktAuthTokenHolder
import cz.josefadamcik.trackontrakt.data.api.model.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class MediaDetailPresenter @Inject constructor(
    val traktApi: TraktApi,
    val tokenHolder: TraktAuthTokenHolder
) : BasePresenter<MediaDetailView>() {

    private var identifier: MediaIdentifier? = null
    private var movieDetail: MovieDetail? = null
    private var showDetail: ShowDetail? = null

    fun load(mediaId: MediaIdentifier?, name: String?) {
        Timber.i("load: %s", mediaId)

        if (mediaId == null) {
            //fixme: error
            return
        }

        if (name != null) {
            view?.showTitle(name)
        }

        view?.itemCheckInactionVisible(false)
        view?.itemCheckInactionEnabled(false)

        view?.showLoading()
        if (mediaId.type == MediaType.movie) {
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
        } else if (mediaId.type == MediaType.show) {
            disposables.add(
                traktApi.show(tokenHolder.httpAuth(), mediaId.id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { show ->
                            view?.hideLoading()
                            if (show != null) {
                                showShow(show)
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


    fun checkinActionClicked() {
        val movieDetail = this.movieDetail
        if (movieDetail != null) {
            val request = CheckinRequest(
                Movie(movieDetail.title, movieDetail.year, movieDetail.ids),
                null,
                BuildConfig.VERSION_NAME,
                BuildConfig.BUILD_DATE,
                null,
                null
            )
            view?.showLoading()
            view?.itemCheckInactionEnabled(false)
            disposables.add(
                traktApi.checkin(tokenHolder.httpAuth(), request)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { result ->
                            Timber.d("checkin complete $result")
                            view?.hideLoading()
                            view?.itemCheckInactionEnabled(true)
                            if (result.isSuccessful && result.code() == 201) {
                                view?.showCheckinSuccess()
                            } else if (result.code() == 409) {
                                view?.showCheckinAlreadyInProgress()
                            } else {
                                Timber.e("Unexpected status code %s", result.code())
                                view?.showError(IllegalStateException("Unexpected status code " + result.code()))
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


    private fun showShow(show: ShowDetail) {
        showDetail = show
        view?.showMedia(MediaDetailModel(MediaDetailModel.MediaDetailInfo(show.network, show.overview)))

        //TODO: start loading info
        //view?.showLoading()

    }

    private fun showMovie(movie: MovieDetail) {
        movieDetail = movie
        view?.itemCheckInactionVisible(true)
        view?.itemCheckInactionEnabled(true)
        view?.showMedia(MediaDetailModel(MediaDetailModel.MediaDetailInfo(movie.tagline, movie.overview)))
    }
}