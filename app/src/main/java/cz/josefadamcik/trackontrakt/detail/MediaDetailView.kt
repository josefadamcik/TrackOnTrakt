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

import com.hannesdorfmann.mosby3.mvp.MvpView

interface MediaDetailView : MvpView {
    fun showTitle(name: String)
    fun itemCheckInactionVisible(visible: Boolean)
    fun itemCheckInactionEnabled(visible: Boolean)
    fun showLoading()
    fun hideLoading()
    fun showError(e: Throwable?)
    fun showTextInfo(tagline: String?, overview: String?)
    fun showCheckinSuccess()
    fun showCheckinAlreadyInProgress()
}