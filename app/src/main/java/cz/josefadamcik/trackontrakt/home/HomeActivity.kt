package cz.josefadamcik.trackontrakt.home

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.Snackbar
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import butterknife.BindDrawable
import butterknife.BindView
import butterknife.ButterKnife

import com.mancj.materialsearchbar.MaterialSearchBar
import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.TrackOnTraktApplication
import cz.josefadamcik.trackontrakt.base.BaseActivity
import cz.josefadamcik.trackontrakt.base.SearchViewWrapper
import cz.josefadamcik.trackontrakt.data.api.model.HistoryItem
import cz.josefadamcik.trackontrakt.data.api.model.MediaItem
import cz.josefadamcik.trackontrakt.data.api.model.MediaType
import cz.josefadamcik.trackontrakt.data.api.model.Watching
import cz.josefadamcik.trackontrakt.detail.MediaDetailActivity
import cz.josefadamcik.trackontrakt.detail.MediaIdentifier
import cz.josefadamcik.trackontrakt.search.SearchResultsActivity
import cz.josefadamcik.trackontrakt.search.TraktFilter
import cz.josefadamcik.trackontrakt.util.CurrentTimeProvider
import timber.log.Timber
import javax.inject.Inject


class HomeActivity : BaseActivity<HomeView, HomePresenter>(), SwipeRefreshLayout.OnRefreshListener, HomeView, SearchViewWrapper.SearchCallback, HistoryAdapter.ItemInteractionListener {


    enum class Mode {
        History,
        Search
    }

    @Inject lateinit var homePresenter: HomePresenter
    @Inject lateinit var currentTimeProvider: CurrentTimeProvider

    @BindView(R.id.toolbar_layout) lateinit var toolbarLayout: CollapsingToolbarLayout
    @BindView(R.id.list) lateinit var recyclerView: RecyclerView
    @BindView(R.id.progress) lateinit var progress: ProgressBar
    @BindView(R.id.swipe_refresh) lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.search_bar) lateinit var searchBar: MaterialSearchBar
    @BindDrawable(R.drawable.ic_local_movies_black_24dp) lateinit var icoTypeMovieDrawable: Drawable
    @BindDrawable(R.drawable.ic_television_classic) lateinit var icoTypeShowDrawable: Drawable



    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var searchViewWrapper: SearchViewWrapper

    private var currentMode: Mode = Mode.History


    override fun onCreate(savedInstanceState: Bundle?) {
        (application as TrackOnTraktApplication).component.inject(this)
        setContentView(R.layout.activity_home)
        unbinder = ButterKnife.bind(this)

        //setSupportActionBar(toolbar)
        //toolbar.title = getString(R.string.title_history)
        toolbarLayout.title = getString(R.string.title_history)
        toolbarLayout.isTitleEnabled = true

        swipeRefreshLayout.setOnRefreshListener(this)

        initList()
        searchViewWrapper = SearchViewWrapper(searchBar, this)

        super.onCreate(savedInstanceState)
    }

    private fun initList() {
        historyAdapter = HistoryAdapter(LayoutInflater.from(this), this, this, icoTypeMovieDrawable, icoTypeShowDrawable, currentTimeProvider)
        recyclerView.layoutManager = LinearLayoutManager(this)
//        recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        recyclerView.setHasFixedSize(true)

        setAdapterForMode()
    }

    private fun setAdapterForMode() {
        if (recyclerView.adapter != historyAdapter) {
            recyclerView.adapter = historyAdapter
        }
    }


    override fun doSearchForQuery(query: String, filter: TraktFilter) {
        startActivity(SearchResultsActivity.createIntent(this, query, filter))
    }

    override fun onNavigationClicked() {
        //nop
    }

    override fun onBackClicked() {
        //nop
    }

    override fun createPresenter(): HomePresenter {
        return homePresenter
    }


    override fun showHistory(items: HistoryModel) {
        hidePullToRefreshRefreshing()
        currentMode = Mode.History
        historyAdapter.model = items
        setAdapterForMode()
    }


    override fun showLoading() {
        progress.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        progress.visibility = View.GONE
    }

    override fun showError(e: Throwable?) {
        Snackbar.make(progress, e?.message ?: getString(R.string.err_unknown), Snackbar.LENGTH_LONG).show()

        hidePullToRefreshRefreshing()
    }

    override fun onRefresh() {
        Timber.d("onRefresh ")
        presenter.loadHomeStreamData(true)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds model to the action bar if it is present.
        //menuInflater.inflate(R.menu.menu_home, menu)
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_search) {
//            appbar.setExpanded(true, true)
//            searchView.open(true, item)

            return true
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onHistoryItemClicked(item: HistoryItem, position: Int) {
        showDetailForMediaItem(item)
    }

    private fun showDetailForMediaItem(item: MediaItem) {
        val title: String? = when (item.type) {
            MediaType.episode -> item.show?.title
            MediaType.movie -> item.movie?.title
            else -> null
        }
        startActivity(MediaDetailActivity.createIntent(this, MediaIdentifier.fromMediaItemButShowForEpisode(item), title ?: getString(R.string.media_title_placeholder)))
    }

    override fun onWatchingItemClicked(item: Watching.Something, position: Int) {
        showDetailForMediaItem(item)
    }

    override fun onPagerClicked() {
        presenter.loadNextPage()
    }

    private fun hidePullToRefreshRefreshing() {
        if (swipeRefreshLayout.isRefreshing) {
            swipeRefreshLayout.isRefreshing = false
        }
    }


}

