
package cz.josefadamcik.trackontrakt.home

import com.hannesdorfmann.mosby3.mvp.MvpView

interface HomeView : MvpView {
    fun showHistory(items: HistoryModel)
    fun showLoading()
    fun hideLoading()
    fun showError(e: Throwable?)
}