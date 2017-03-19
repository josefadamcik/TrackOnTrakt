package cz.josefadamcik.trackontrakt.home

import android.content.SharedPreferences
import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.FloatingActionButton
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
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.bumptech.glide.Glide
import com.hannesdorfmann.mosby3.mvp.lce.MvpLceActivity
import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.TrackOnTraktApplication
import cz.josefadamcik.trackontrakt.data.UserAccountManager
import cz.josefadamcik.trackontrakt.data.api.model.HistoryItem
import timber.log.Timber
import javax.inject.Inject


class HomeActivity : MvpLceActivity<SwipeRefreshLayout, List<HistoryItem>, HomeView, HomePresenter>(), SwipeRefreshLayout.OnRefreshListener, HomeView {
    @Inject lateinit var preferences: SharedPreferences
    @Inject lateinit var userAccountManager: UserAccountManager
    @Inject lateinit var homePresenter: HomePresenter

    @BindView(R.id.toolbar) lateinit var toolbar: Toolbar
    @BindView(R.id.toolbar_layout) lateinit var toolbarLayout: CollapsingToolbarLayout
    @BindView(R.id.fab) lateinit var fab: FloatingActionButton
    @BindView(R.id.toolbar_image) lateinit var toolbarImage: ImageView
    @BindView(R.id.recyclerView) lateinit var recyclerView: RecyclerView
    private lateinit var unbinder: Unbinder


    private lateinit var adapter: HistoryAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        (application as TrackOnTraktApplication).graph.inject(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        unbinder = ButterKnife.bind(this)
        setSupportActionBar(toolbar)



        contentView.setOnRefreshListener(this)

        adapter = HistoryAdapter(LayoutInflater.from(this))
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter

        loadData(false)

    }

    override fun createPresenter(): HomePresenter {
        return homePresenter
    }

    override fun loadData(pullToRefresh: Boolean) {
        presenter.loadData(pullToRefresh);
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

        loadUserProfile()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.action_settings) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun hidePullToRefreshRefreshing() {
        if (contentView.isRefreshing) {
            contentView.isRefreshing = false
        }
    }

    private fun loadUserProfile() {
        userAccountManager.obtainUserSettings()
            .subscribe(
                { settings ->
                    Timber.d("%s", settings)
                    supportActionBar?.title = settings.user.name
                    toolbarLayout.title = settings.user.name
                    //toolbar.title = settings.user.name
                    settings.account.cover_image?.apply {
                        Glide.with(this@HomeActivity).load(this).into(toolbarImage)
                    }
                },
                { t ->
                    Timber.e(t, "unable to load user profile")
                    val snackbar = Snackbar.make(findViewById(R.id.webview), R.string.err_data_retrieval_failed, Snackbar.LENGTH_INDEFINITE)
                    snackbar.setAction(R.string.action_retry, View.OnClickListener { loadUserProfile() })
                    snackbar.show()
                }

            )
    }
}
