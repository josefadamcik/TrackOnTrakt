package cz.josefadamcik.trackontrakt.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.widget.TextViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import butterknife.*
import com.evernote.android.state.State
import com.evernote.android.state.StateSaver
import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.TrackOnTraktApplication
import cz.josefadamcik.trackontrakt.base.BaseActivity
import cz.josefadamcik.trackontrakt.data.api.model.EpisodeWithProgress
import cz.josefadamcik.trackontrakt.util.RoundedBackgroundSpan
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import org.threeten.bp.LocalDateTime
import javax.inject.Inject

class MediaDetailActivity : BaseActivity<MediaDetailView, MediaDetailPresenter>(), MediaDetailView, MediaDetailAdapter.InteractionListener {



    @Inject lateinit var myPresenter: MediaDetailPresenter

    @BindView(R.id.progress) lateinit var progress: MaterialProgressBar
    @BindView(R.id.toolbar) lateinit var toolbar: Toolbar
    @BindView(R.id.toolbar_layout) lateinit var toolbarLayout: CollapsingToolbarLayout
    @BindView(R.id.toolbar_title) lateinit var toolbarTitle: TextView
    @BindView(R.id.toolbar_year) lateinit var toolbarYear: TextView
    @BindView(R.id.toolbar_certification) lateinit var toolbarCertification: TextView
    @BindView(R.id.toolbar_rating) lateinit var toolbarRating: TextView
    @BindView(R.id.fab) lateinit var fab: FloatingActionButton
    @BindView(R.id.list) lateinit var list: RecyclerView

    @JvmField @BindColor(R.color.material_color_blue_grey_500) var otherBgColor: Int = 0
    @JvmField @BindColor(android.R.color.white) var otherColor: Int = 0
    @JvmField @BindDimen(R.dimen.material_baseline_grid_0_5x) var halfGridStep: Int = 0


    lateinit var adapter: MediaDetailAdapter

    @State var mediaId: MediaIdentifier? = null
    @State var mediaName: String? = null

    companion object {
        public const val PAR_ID = "id"
        public const val PAR_NAME = "name"
        public fun createIntent(context: Context, id: MediaIdentifier, title: String): Intent {
            val intent = Intent(context, MediaDetailActivity::class.java)
            intent.putExtra(MediaDetailActivity.PAR_ID, id)
            intent.putExtra(MediaDetailActivity.PAR_NAME, title)
            return intent
        }
    }

    override fun createPresenter(): MediaDetailPresenter {
        return myPresenter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as TrackOnTraktApplication).component.inject(this)
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
        val roundedBackgroundSpanConfig = RoundedBackgroundSpan.Config(
            backgroundColor = otherBgColor,
            textColor = otherColor,
            cornerRadius = halfGridStep,
            horizontalInnerPad = 2 + halfGridStep,
            verticalInnerPad = halfGridStep,
            verticalOuterPad = halfGridStep

        )
        adapter = MediaDetailAdapter(LayoutInflater.from(this), resources, this, this, roundedBackgroundSpanConfig)
        list.layoutManager = LinearLayoutManager(this)

        //list.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
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

    override fun onEpisodeCheckInClick(episode: EpisodeWithProgress) {
        presenter.checkinActionClicked(episode)
    }

    override fun onOpenWebPageClick(uri: Uri) {
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }

    override fun showTitle(name: String) {
        toolbar.title = name
        toolbarLayout.title = name
        toolbarTitle.text = name

        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
            toolbarTitle,
            8,
            32,
            1,
            TypedValue.COMPLEX_UNIT_DIP
        )

    }


    override fun showBasicInfo(year: Int?, certification: String?, rating: Double, votes: Long) {
        if (year == null) {
            toolbarYear.visibility = View.GONE
        } else {
            toolbarYear.visibility = View.VISIBLE
            toolbarYear.text = String.format("%d", year)
        }
        if (certification == null) {
            toolbarCertification.visibility = View.GONE
        } else {
            toolbarCertification.visibility = View.VISIBLE
            toolbarCertification.text = certification
        }

        toolbarRating.text = resources.getString(R.string.media_detail_votes, rating * 10, votes)
        toolbarRating.visibility = View.VISIBLE
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

    override fun showAlreadyWatchedStats(number: Int, last_watched_at: LocalDateTime?) {
        Snackbar.make(progress, getString(R.string.media_detail_show_watched_stats, number, last_watched_at), Snackbar.LENGTH_LONG).show()
    }

    override fun showError(e: Throwable?) {
        Snackbar.make(progress, e?.message ?: getString(R.string.err_unknown), Snackbar.LENGTH_LONG).show()
    }

    override fun showCheckinDialog() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showCheckinSuccess() {
        Snackbar.make(progress, getString(R.string.info_checkin_successful), Snackbar.LENGTH_LONG).show()
    }

    override fun showCheckinAlreadyInProgress() {
        Snackbar.make(progress, getString(R.string.info_checkin_already_in_progress), Snackbar.LENGTH_LONG).show()
    }
}
