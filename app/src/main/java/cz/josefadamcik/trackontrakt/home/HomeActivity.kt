package cz.josefadamcik.trackontrakt.home

import android.content.SharedPreferences
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.hannesdorfmann.mosby3.mvp.lce.MvpLceActivity
import com.lapism.searchview.SearchAdapter
import com.lapism.searchview.SearchFilter
import com.lapism.searchview.SearchItem
import com.lapism.searchview.SearchView
import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.TrackOnTraktApplication
import cz.josefadamcik.trackontrakt.data.UserAccountManager
import cz.josefadamcik.trackontrakt.data.api.model.HistoryItem
import timber.log.Timber
import javax.inject.Inject


class HomeActivity : MvpLceActivity<SwipeRefreshLayout, List<HistoryItem>, HomeView, HomePresenter>(), SwipeRefreshLayout.OnRefreshListener, HomeView, SearchView.OnQueryTextListener, SearchView.OnOpenCloseListener, SearchView.OnVoiceClickListener {
    @Inject lateinit var preferences: SharedPreferences
    @Inject lateinit var userAccountManager: UserAccountManager
    @Inject lateinit var homePresenter: HomePresenter

    @BindView(R.id.app_bar) lateinit var appbar: AppBarLayout
    @BindView(R.id.toolbar) lateinit var toolbar: Toolbar
    @BindView(R.id.toolbar_layout) lateinit var toolbarLayout: CollapsingToolbarLayout
    @BindView(R.id.toolbar_image) lateinit var toolbarImage: ImageView
    @BindView(R.id.recyclerView) lateinit var recyclerView: RecyclerView
    @BindView(R.id.search_view) lateinit var searchView: SearchView

    private lateinit var unbinder: Unbinder
    private lateinit var adapter: HistoryAdapter
    private lateinit var suggestionAdapter: SearchAdapter

    private val suggestionList: MutableList<SearchItem> = mutableListOf()
    var searchSuggestions: List<String> = emptyList()
        set(items: List<String>) {
            suggestionList.clear()
            items.mapTo(suggestionList, ::SearchItem)
            suggestionAdapter.suggestionsList = suggestionList
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as TrackOnTraktApplication).graph.inject(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        unbinder = ButterKnife.bind(this)
        toolbar.navigationContentDescription = getString(R.string.app_name)
        setSupportActionBar(toolbar)

        contentView.setOnRefreshListener(this)

        initList()
        initSearchView()

        loadData(false)

    }

    private fun initList() {
        adapter = HistoryAdapter(LayoutInflater.from(this))
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
    }


    private fun initSearchView() {
        searchView.hint = getString(R.string.search)
        //searchView.setArrowOnly(true)
        searchView.setOnQueryTextListener(this)
        searchView.setOnOpenCloseListener(this)
        searchView.setOnVoiceClickListener(this)


        suggestionAdapter = SearchAdapter(this, suggestionList)

        suggestionAdapter.addOnItemClickListener { view, position ->
            val textView = view.findViewById(R.id.textView_item_text) as TextView
            val query = textView.text.toString()
            doSearchForQuery(query)
            searchView.close(false)
        }
        searchView.adapter = suggestionAdapter

        val filter = listOf(
            SearchFilter(getString(R.string.filter_movies), true),
            SearchFilter(getString(R.string.filter_shows), true)
        )
        searchView.setFilters(filter)
    }

    private fun doSearchForQuery(query: String) {
        searchView.close(true)
        presenter.search(query, searchView.filtersStates[0], searchView.filtersStates[1])
    }


    override fun createPresenter(): HomePresenter {
        return homePresenter
    }

    override fun loadData(pullToRefresh: Boolean) {
        presenter.loadHomeStreamData(pullToRefresh)
    }

    override fun getErrorMessage(e: Throwable?, pullToRefresh: Boolean): String {
        return e?.message ?: getString(R.string.err_uknown)
    }

    override fun setData(data: List<HistoryItem>?) {
        if (data != null) {
            adapter.items = data
        }
        hidePullToRefreshRefreshing()
    }

    override fun showError(e: Throwable?, pullToRefresh: Boolean) {
        super.showError(e, pullToRefresh)
        hidePullToRefreshRefreshing()
    }

    override fun onRefresh() {
        loadData(true)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_search) {
            appbar.setExpanded(true, true)
            searchView.open(true, item)

            return true
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onQueryTextSubmit(query: String?): Boolean {
        Timber.d("oQueryTextSubmit $query")
        if (query != null && query.isNotBlank()) {
            doSearchForQuery(query)
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

    private fun hidePullToRefreshRefreshing() {
        if (contentView.isRefreshing) {
            contentView.isRefreshing = false
        }
    }


}
