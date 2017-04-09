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

package cz.josefadamcik.trackontrakt.base

import android.content.Context
import android.widget.TextView
import com.lapism.searchview.SearchAdapter
import com.lapism.searchview.SearchFilter
import com.lapism.searchview.SearchItem
import com.lapism.searchview.SearchView
import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.search.TraktFilter
import timber.log.Timber

public class SearchViewWrapper (
    private val context: Context,
    private val searchView: SearchView,
    private val searchCallback: SearchCallback
) : SearchView.OnVoiceClickListener, SearchView.OnOpenCloseListener, SearchView.OnQueryTextListener {

    private val suggestionList: MutableList<SearchItem> = mutableListOf()
    public var query: String
        get() = searchView.query?.toString() ?: ""
        set(value) {
            searchView.setQuery(value, false)
        }
    public var filters: TraktFilter
        get() = TraktFilter(searchView.filtersStates[0], searchView.filtersStates[1])
        set(value) {
            val viewFilter = convertTraktFilterToViewFilter(value)
            searchView.setFilters(viewFilter)
        }

    var searchSuggestions: List<String> = emptyList()
        set(items: List<String>) {
            suggestionList.clear()
            items.mapTo(suggestionList, ::SearchItem)
            suggestionAdapter.suggestionsList = suggestionList
        }

    private lateinit var suggestionAdapter: SearchAdapter

    interface SearchCallback {
        fun doSearchForQuery(query: String,  filter: TraktFilter)
    }


    init {
        searchView.hint = context.getString(R.string.search)
        //searchView.setArrowOnly(true)
        searchView.setOnQueryTextListener(this)
        searchView.setOnOpenCloseListener(this)
        searchView.setOnVoiceClickListener(this)


        suggestionAdapter = SearchAdapter(context, suggestionList)

        suggestionAdapter.addOnItemClickListener { view, position ->
            val textView = view.findViewById(R.id.textView_item_text) as TextView
            val query = textView.text.toString()
            searchCallback.doSearchForQuery(query, filters)
            searchView.close(false)
        }
        searchView.adapter = suggestionAdapter
        filters = TraktFilter(movies = true, shows = true)
    }

    private fun convertTraktFilterToViewFilter(filter: TraktFilter): List<SearchFilter> {
        val viewFilter = listOf(
            SearchFilter(context.getString(R.string.filter_movies), filter.movies),
            SearchFilter(context.getString(R.string.filter_shows), filter.shows)
        )
        return viewFilter
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        Timber.d("oQueryTextSubmit $query")
        searchView.close(true)
        if (query != null && query.isNotBlank()) {
            searchCallback.doSearchForQuery(query, filters)
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        Timber.d("onQueryTextChange $newText")
        return false
    }

    /**
     * searchView open
     */
    override fun onOpen(): Boolean {
        Timber.d("onOpen searchView")
        return true
    }

    /**
     * searchView close
     */
    override fun onClose(): Boolean {
        Timber.d("onClose searchView")
        return true
    }

    override fun onVoiceClick() {
        Timber.d("onVoiceClick todo: permission on 6+")
    }

}
