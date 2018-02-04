
package cz.josefadamcik.trackontrakt.detail

import com.hannesdorfmann.mosby3.mvp.MvpView
import org.threeten.bp.LocalDateTime

interface MediaDetailView : MvpView {
    fun showTitle(name: String)
    fun showBasicInfo(year: Int?, certification: String?, rating: Double, votes: Long)
    fun itemCheckInactionVisible(visible: Boolean)
    fun itemCheckInactionEnabled(visible: Boolean)
    fun showLoading()
    fun hideLoading()
    fun showError(e: Throwable?)
    fun showMedia(rowItems: List<RowItemModel>)
    fun showCheckinSuccess()
    fun showCheckinAlreadyInProgress()
    fun showAlreadyWatchedStats(number: Int, last_watched_at: LocalDateTime?)
    fun showCheckinDialog(checkinItemName: String)
}