package cz.josefadamcik.trackontrakt.search

import android.app.SearchManager
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import butterknife.BindDrawable
import butterknife.BindView
import butterknife.ButterKnife
import com.evernote.android.state.State
import com.evernote.android.state.StateSaver
import com.lapism.searchview.SearchView
import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.TrackOnTraktApplication
import cz.josefadamcik.trackontrakt.base.BaseActivity
import cz.josefadamcik.trackontrakt.base.SearchViewWrapper
import cz.josefadamcik.trackontrakt.data.api.model.SearchResultItem
import cz.josefadamcik.trackontrakt.detail.MediaDetailActivity
import cz.josefadamcik.trackontrakt.detail.MediaIdentifier
import javax.inject.Inject


class SearchResultsActivity : BaseActivity<SearchResultsView, SearchResultPresenter>(), SearchResultsView, SearchViewWrapper.SearchCallback, SearchResultAdapter.OnItemInteractionListener {
    @Inject lateinit var myPresenter: SearchResultPresenter
    private lateinit var searchViewWrapper: SearchViewWrapper
    protected lateinit var searchAdapter: SearchResultAdapter

    @BindView(R.id.progress) lateinit var progress: ProgressBar
    @BindView(R.id.toolbar) lateinit var toolbar: Toolbar
    @BindView(R.id.search_view) lateinit var searchView: SearchView
    @BindView(R.id.list) lateinit var list: RecyclerView

    @BindDrawable(R.drawable.ic_local_movies_gray_24dp) lateinit var icoTypeMovieDrawable: Drawable
    @BindDrawable(R.drawable.ic_tv_gray_24dp) lateinit var icoTypeShowDrawable: Drawable

    @State var query: String? = null
    @State var filter: TraktFilter = TraktFilter(true, true)

    companion object {
        public const val PAR_FILTER: String = "filter"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as TrackOnTraktApplication).graph.inject(this)
        setContentView(R.layout.activity_search_results)
        unbinder = ButterKnife.bind(this)

        searchView.setArrowOnly(true)
        searchView.setOnMenuClickListener(SearchView.OnMenuClickListener { finish() })
        searchView.setVersionMargins(SearchView.VERSION_MARGINS_TOOLBAR_SMALL)

        searchViewWrapper = SearchViewWrapper(this, searchView, this)

        initList()

        super.onCreate(savedInstanceState)

        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            this.query = query
            searchViewWrapper.query = query
            val filter = intent.getParcelableExtra<TraktFilter>(PAR_FILTER)
            this.filter = filter
            searchViewWrapper.filters = filter
        }

        StateSaver.restoreInstanceState(this, savedInstanceState)

        presenter.search(query, filter)
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        StateSaver.saveInstanceState(this, outState);
    }

    override fun doSearchForQuery(query: String, filter: TraktFilter) {
        this.query = query
        this.filter = filter
        presenter.search(query, filter)
    }

    override fun createPresenter(): SearchResultPresenter {
        return myPresenter
    }

    override fun showSearchResults(items: List<SearchResultItem>) {
        hideLoading()
        searchAdapter.items = items

    }

    override fun showLoading() {
        progress.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        progress.visibility = View.GONE
    }

    override fun showError(e: Throwable?) {
        //TODO: show error
        Snackbar.make(progress, e?.message ?: getString(R.string.err_unknown), Snackbar.LENGTH_LONG).show()
    }

    override fun showEmptyResult() {
        //todo
    }

    override fun onSearchResultClicked(item: SearchResultItem, position: Int) {
        val intent = Intent(this, MediaDetailActivity::class.java)
        intent.putExtra(MediaDetailActivity.PAR_ID, MediaIdentifier.fromSearchResult(item))
        intent.putExtra(MediaDetailActivity.PAR_NAME, item.title)
        startActivity(intent)
    }

    private fun initList() {
        searchAdapter = SearchResultAdapter(LayoutInflater.from(this), resources, icoTypeMovieDrawable, icoTypeShowDrawable, this)
        list.layoutManager = LinearLayoutManager(this)
        list.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        list.setHasFixedSize(true)
        list.adapter = searchAdapter
    }

}
