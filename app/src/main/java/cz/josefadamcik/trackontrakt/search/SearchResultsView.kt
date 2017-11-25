
package cz.josefadamcik.trackontrakt.search

import com.hannesdorfmann.mosby3.mvp.MvpView
import cz.josefadamcik.trackontrakt.data.api.model.SearchResultItem

interface SearchResultsView : MvpView {

    fun showSearchResults(items: List<SearchResultItem>)
    fun showLoading()
    fun hideLoading()
    fun showError(e: Throwable?)
    fun showEmptyResult()
}