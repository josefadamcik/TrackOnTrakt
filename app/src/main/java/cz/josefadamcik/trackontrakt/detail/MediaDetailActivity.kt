package cz.josefadamcik.trackontrakt.detail

import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.widget.Toolbar
import android.view.View
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.evernote.android.state.State
import com.evernote.android.state.StateSaver
import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.TrackOnTraktApplication
import cz.josefadamcik.trackontrakt.base.BaseActivity
import javax.inject.Inject

class MediaDetailActivity : BaseActivity<MediaDetailView, MediaDetailPresenter>(), MediaDetailView {
    @Inject lateinit var myPresenter: MediaDetailPresenter

    //    @BindView(R.id.progress) lateinit var progress: ProgressBar
    @BindView(R.id.toolbar) lateinit var toolbar: Toolbar
    @BindView(R.id.toolbar_layout) lateinit var toolbarLayout: CollapsingToolbarLayout
    @BindView(R.id.fab) lateinit var fab: FloatingActionButton

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

        presenter.load(mediaId, mediaName)
    }


    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        StateSaver.saveInstanceState(this, outState);
    }

    @OnClick(R.id.fab) fun onFabClick(view: View) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show()
    }


    override fun showTitle(name: String) {
        toolbar.title = name
        toolbarLayout.title = name
    }

    override fun showItemCheckInActionVisible(visible: Boolean) {
        fab.visibility = if (visible) View.VISIBLE else View.GONE
    }
}
