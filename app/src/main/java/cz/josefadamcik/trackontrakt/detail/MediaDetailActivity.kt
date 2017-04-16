package cz.josefadamcik.trackontrakt.detail

import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.evernote.android.state.State
import com.evernote.android.state.StateSaver
import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.TrackOnTraktApplication
import cz.josefadamcik.trackontrakt.base.BaseActivity
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import javax.inject.Inject

class MediaDetailActivity : BaseActivity<MediaDetailView, MediaDetailPresenter>(), MediaDetailView {
    @Inject lateinit var myPresenter: MediaDetailPresenter

    @BindView(R.id.progress) lateinit var progress: MaterialProgressBar
    @BindView(R.id.toolbar) lateinit var toolbar: Toolbar
    @BindView(R.id.toolbar_layout) lateinit var toolbarLayout: CollapsingToolbarLayout
    @BindView(R.id.fab) lateinit var fab: FloatingActionButton
    @BindView(R.id.list) lateinit var list: RecyclerView

    lateinit var adapter: MediaDetailAdapter

    @State var mediaId: MediaIdentifier? = null
    @State var mediaName: String? = null

    companion object {
        public const val PAR_ID = "id"
        public const val PAR_NAME = "name"
    }

    override fun createPresenter(): MediaDetailPresenter {
        return myPresenter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as TrackOnTraktApplication).graph.inject(this)
        setContentView(R.layout.activity_media_detail)
        unbinder = ButterKnife.bind(this)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        super.onCreate(savedInstanceState)

        mediaId = intent?.extras?.getParcelable(PAR_ID)
        mediaName = intent?.extras?.getString(PAR_NAME)
        StateSaver.restoreInstanceState(this, savedInstanceState)

        initList()

        presenter.load(mediaId, mediaName)
    }

    private fun initList() {
        adapter = MediaDetailAdapter(LayoutInflater.from(this), resources)
        list.layoutManager = LinearLayoutManager(this)
        list.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        list.setHasFixedSize(true)
        list.adapter = adapter
    }


    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        StateSaver.saveInstanceState(this, outState);
    }

    @OnClick(R.id.fab) fun onFabClick(view: View) {
        presenter.checkinActionClicked()
    }


    override fun showTitle(name: String) {
        toolbar.title = name
        toolbarLayout.title = name
    }

    override fun itemCheckInactionVisible(visible: Boolean) {
        fab.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun itemCheckInactionEnabled(enabled: Boolean) {
        fab.isEnabled = enabled
    }

    override fun showLoading() {
        progress.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        progress.visibility = View.GONE
    }

    override fun showMedia(model: MediaDetailModel) {
        adapter.model = model
    }

    override fun showError(e: Throwable?) {
        Snackbar.make(progress, e?.message ?: getString(R.string.err_unknown), Snackbar.LENGTH_LONG).show()
    }


    override fun showCheckinSuccess() {
        Snackbar.make(progress, getString(R.string.info_checkin_successful), Snackbar.LENGTH_LONG).show()
    }

    override fun showCheckinAlreadyInProgress() {
        Snackbar.make(progress, getString(R.string.info_checkin_already_in_progress), Snackbar.LENGTH_LONG).show()
    }
}
