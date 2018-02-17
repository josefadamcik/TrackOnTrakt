package cz.josefadamcik.trackontrakt.detail

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.widget.TextViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import butterknife.*
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.evernote.android.state.State
import com.evernote.android.state.StateSaver
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.TrackOnTraktApplication
import cz.josefadamcik.trackontrakt.base.BaseActivity
import cz.josefadamcik.trackontrakt.data.api.model.EpisodeWithProgress
import cz.josefadamcik.trackontrakt.util.CurrentTimeProvider
import cz.josefadamcik.trackontrakt.util.tint
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import javax.inject.Inject

class MediaDetailActivity : BaseActivity<MediaDetailView, MediaDetailPresenter>(), MediaDetailView, MediaDetailAdapter.InteractionListener {
    @Inject lateinit var myPresenter: MediaDetailPresenter
    @Inject lateinit var currentTimeProvider: CurrentTimeProvider

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

    private var checkinDialog: MaterialDialog? = null

    companion object {
        const val PAR_ID = "id"
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

    override fun onPause() {
        super.onPause()
        checkinDialog?.dismiss()
    }

    private fun initList() {
        adapter = MediaDetailAdapter(LayoutInflater.from(this), resources, this, this)
        list.layoutManager = LinearLayoutManager(this)

        //list.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        list.setHasFixedSize(true)
        list.adapter = adapter
    }


    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        StateSaver.saveInstanceState(this, outState);
    }

    @OnClick(R.id.fab) fun onFabClick() {
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

    override fun itemCheckInactionEnabled(visible: Boolean) {
        fab.isEnabled = visible
    }

    override fun showLoading() {
        progress.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        progress.visibility = View.GONE
    }

    override fun showMedia(rowItems: List<RowItemModel>) {
        adapter.items = rowItems
    }

    override fun showAlreadyWatchedStats(number: Int, last_watched_at: LocalDateTime?) {
        Snackbar.make(progress, getString(R.string.media_detail_show_watched_stats, number, last_watched_at), Snackbar.LENGTH_LONG).show()
    }

    override fun showError(e: Throwable?) {
        Snackbar.make(progress, e?.message ?: getString(R.string.err_unknown), Snackbar.LENGTH_LONG).show()
    }



    override fun showCheckinDialog(checkinItemName: String) {
        val vh = CheckinDialogViewHolder(
                LayoutInflater.from(this).inflate(R.layout.dialog_checkin, null, false),
                currentTimeProvider
        )
        vh.txtTitle.text = checkinItemName
        checkinDialog = MaterialDialog.Builder(this)
                .positiveText(R.string.dialog_checkin)
                .customView(vh.view, false)
                .negativeText(android.R.string.cancel)
                .autoDismiss(true)
                .onPositive(vh)
                .build()
        checkinDialog?.show()
    }

    override fun showCheckinSuccess() {
        Snackbar.make(progress, getString(R.string.info_checkin_successful), Snackbar.LENGTH_LONG).show()
    }

    override fun showCheckinAlreadyInProgress() {
        Snackbar.make(progress, getString(R.string.info_checkin_already_in_progress), Snackbar.LENGTH_LONG).show()
    }

    inner class CheckinDialogViewHolder(val view: View, val currentTimeProvider: CurrentTimeProvider)
        : RadioGroup.OnCheckedChangeListener,
            MaterialDialog.SingleButtonCallback,
            DialogInterface.OnDismissListener,
            DatePickerDialog.OnDateSetListener,
            TimePickerDialog.OnTimeSetListener
    {
        @BindView(R.id.txt_title) lateinit var txtTitle : TextView
        @BindView(R.id.txt_date) lateinit var txtDate : TextView
        @BindView(R.id.txt_time) lateinit var txtTime : TextView
        @BindView(R.id.rg_when) lateinit var radioGroup : RadioGroup
        private var dateDialog: DatePickerDialog? = null
        private var timeDialog: TimePickerDialog? = null
        private val timeFormat: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        private val dateFormat: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

        var dateTime: LocalDateTime  = currentTimeProvider.dateTime
            set(value) {
                txtTime.text = timeFormat.format(value)
                txtDate.text = dateFormat.format(value)
                field = value
            }

        override fun onClick(dialog: MaterialDialog, which: DialogAction) {
            if (which == DialogAction.POSITIVE) {
                when(radioGroup.checkedRadioButtonId) {
                    R.id.rb_now -> presenter.checkinConfirmed(CheckinTime.Now)
                    R.id.rb_inpast -> presenter.checkinConfirmed(CheckinTime.At(dateTime))
                }
            }
        }

        override fun onCheckedChanged(rg: RadioGroup, selectedId: Int) {
            when (selectedId) {
                R.id.rb_now -> {
                    txtDate.visibility = View.GONE
                    txtTime.visibility = View.GONE
                    dismissDateTimeDialogsIfNeeded()
                }
                else -> {
                    txtDate.visibility = View.VISIBLE
                    txtTime.visibility = View.VISIBLE
                }
            }
        }

        override fun onTimeSet(view: TimePickerDialog, hourOfDay: Int, minute: Int, second: Int) {
            dateTime = dateTime.withHour(hourOfDay)
                    .withMinute(minute)
                    .withSecond(second)
        }

        override fun onDateSet(view: DatePickerDialog, year: Int, monthOfYear: Int, dayOfMonth: Int) {
            dateTime = dateTime.withYear(year)
                    .withMonth(monthOfYear + 1)
                    .withDayOfMonth(dayOfMonth)
        }

        override fun onDismiss(p0: DialogInterface?) {
            dismissDateTimeDialogsIfNeeded()
        }

        private fun dismissDateTimeDialogsIfNeeded() {
            if (dateDialog?.isVisible() == true) {
                dateDialog?.dismiss()
            }
            if (timeDialog?.isVisible() == true) {
                timeDialog?.dismiss()
            }
        }

        @OnClick(R.id.txt_date, R.id.txt_time)
        fun onDateTimeClick(view: View) {
            when(view.id) {
                R.id.txt_date -> {
                    dateDialog = DatePickerDialog.newInstance(this, dateTime.year, dateTime.monthValue - 1, dateTime.dayOfMonth )
                    dateDialog?.show(fragmentManager, "datedialog")
                }
                R.id.txt_time -> {
                    timeDialog = TimePickerDialog.newInstance(this, dateTime.hour, dateTime.minute, 0,DateFormat.is24HourFormat(this@MediaDetailActivity))
                    timeDialog?.show(fragmentManager, "timedialog")
                }
            }
        }

        init {
            ButterKnife.bind(this, view)
            radioGroup.setOnCheckedChangeListener(this)
            dateTime = currentTimeProvider.dateTime.withMinute(0)

            txtDate.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(this@MediaDetailActivity, R.drawable.ic_date_range_black_24dp)
                            ?.tint(this@MediaDetailActivity, R.color.colorAccent),
                    null, null, null)
            txtTime.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(this@MediaDetailActivity, R.drawable.ic_access_time_black_24dp)
                            ?.tint(this@MediaDetailActivity, R.color.colorAccent),
                    null, null, null)

        }
    }


}
