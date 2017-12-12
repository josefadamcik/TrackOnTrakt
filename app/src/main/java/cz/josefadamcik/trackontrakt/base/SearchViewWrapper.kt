package cz.josefadamcik.trackontrakt.base

import com.mancj.materialsearchbar.MaterialSearchBar
import cz.josefadamcik.trackontrakt.search.TraktFilter
import timber.log.Timber


public class SearchViewWrapper (
    private val searchView: MaterialSearchBar,
    private val searchCallback: SearchCallback
) : MaterialSearchBar.OnSearchActionListener {

    override fun onButtonClicked(buttonCode: Int) {
        Timber.d("onButtonClicked btn: %s", buttonCode)
        when (buttonCode) {
//            MaterialSearchBar.BUTTON_SPEECH ->
            MaterialSearchBar.BUTTON_BACK -> searchCallback.onBackClicked()
            MaterialSearchBar.BUTTON_NAVIGATION -> searchCallback.onNavigationClicked()
        }
    }

    override fun onSearchStateChanged(enabled: Boolean) {
        Timber.d("onSearchStateChanged enabled %s", enabled)
    }

    override fun onSearchConfirmed(query: CharSequence?) {
        Timber.d("oQueryTextSubmit $query")
        if (query != null && query.isNotBlank()) {
            searchCallback.doSearchForQuery(query.toString(), TraktFilter(true, true))
        }
    }

    public var query: String
        get() = searchView.text
        set(value) {
            searchView.text = value
        }

    interface SearchCallback {
        fun doSearchForQuery(query: String,  filter: TraktFilter)
        fun onNavigationClicked()
        fun onBackClicked()
    }


    init {
        searchView.setOnSearchActionListener(this)
    }


}
