package cz.josefadamcik.trackontrakt.home

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.Snackbar
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import butterknife.BindDrawable
import butterknife.BindView
import butterknife.ButterKnife
import com.lapism.searchview.SearchView
import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.TrackOnTraktApplication
import cz.josefadamcik.trackontrakt.base.BaseActivity
import cz.josefadamcik.trackontrakt.base.SearchViewWrapper
import cz.josefadamcik.trackontrakt.data.api.model.HistoryItem
import cz.josefadamcik.trackontrakt.data.api.model.MediaType
import cz.josefadamcik.trackontrakt.detail.MediaDetailActivity
import cz.josefadamcik.trackontrakt.detail.MediaIdentifier
import cz.josefadamcik.trackontrakt.search.SearchResultsActivity
import cz.josefadamcik.trackontrakt.search.TraktFilter
import timber.log.Timber
import javax.inject.Inject


class HomeActivity : BaseActivity<HomeView, HomePresenter>(), SwipeRefreshLayout.OnRefreshListener, HomeView, SearchViewWrapper.SearchCallback, HistoryAdapter.OnItemInteractionListener {

    enum class Mode {
        History,
        Search
    }

    @Inject lateinit var homePresenter: HomePresenter

    @BindView(R.id.app_bar) lateinit var appbar: AppBarLayout
    @BindView(R.id.toolbar) lateinit var toolbar: Toolbar
    @BindView(R.id.toolbar_layout) lateinit var toolbarLayout: CollapsingToolbarLayout
    @BindView(R.id.toolbar_image) lateinit var toolbarImage: ImageView
    @BindView(R.id.list) lateinit var recyclerView: RecyclerView
    @BindView(R.id.progress) lateinit var progress: ProgressBar
    @BindView(R.id.swipe_refresh) lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.search_view) lateinit var searchView: SearchView
    @BindDrawable(R.drawable.ic_local_movies_gray_24dp) lateinit var icoTypeMovieDrawable: Drawable
    @BindDrawable(R.drawable.ic_tv_gray_24dp) lateinit var icoTypeShowDrawable: Drawable



    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var searchViewWrapper: SearchViewWrapper

    private var currentMode: Mode = Mode.History


    override fun onCreate(savedInstanceState: Bundle?) {
        (application as TrackOnTraktApplication).component.inject(this)
        setContentView(R.layout.activity_home)
        unbinder = ButterKnife.bind(this)

        setSupportActionBar(toolbar)
        toolbar.title = getString(R.string.title_history)
        toolbarLayout.title = getString(R.string.title_history)

        swipeRefreshLayout.setOnRefreshListener(this)

        initList()
        searchViewWrapper = SearchViewWrapper(this, searchView, this)

        super.onCreate(savedInstanceState)
    }

    private fun initList() {
        historyAdapter = HistoryAdapter(LayoutInflater.from(this), resources, this, icoTypeMovieDrawable, icoTypeShowDrawable)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        recyclerView.setHasFixedSize(true)

        setAdapterForMode()
    }

    private fun setAdapterForMode() {
        recyclerView.adapter = historyAdapter
    }


    override fun doSearchForQuery(query: String, filter: TraktFilter) {
        searchView.close(true)

        startActivity(SearchResultsActivity.createIntent(this, query, filter))
    }


    override fun createPresenter(): HomePresenter {
        return homePresenter
    }


    override fun showHistory(items: List<HistoryItem>) {
        hidePullToRefreshRefreshing()
        currentMode = Mode.History
        historyAdapter.items = items
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


    override fun onHistoryItemClicked(item: HistoryItem, position: Int) {
        val title: String? = when (item.type) {
            MediaType.episode -> item.show?.title
            MediaType.movie -> item.movie?.title
            else -> null
        }
        startActivity(MediaDetailActivity.createIntent(this, MediaIdentifier.fromMediaItemButShowForEpisode(item), title ?: getString(R.string.media_title_placeholder)))
    }



    private fun hidePullToRefreshRefreshing() {
        if (swipeRefreshLayout.isRefreshing) {
            swipeRefreshLayout.isRefreshing = false
        }
    }


}
