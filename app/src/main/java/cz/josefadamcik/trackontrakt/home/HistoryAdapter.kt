package cz.josefadamcik.trackontrakt.home

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.data.api.model.HistoryItem
import cz.josefadamcik.trackontrakt.data.api.model.MediaType
import cz.josefadamcik.trackontrakt.data.api.model.Watching
import cz.josefadamcik.trackontrakt.home.HistoryAdapter.ViewHolder
import cz.josefadamcik.trackontrakt.util.CurrentTimeProvider
import cz.josefadamcik.trackontrakt.util.tint
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import org.threeten.bp.format.DateTimeFormatter
import timber.log.Timber

class HistoryAdapter(
    private val layoutInflater: LayoutInflater,
    private val context: Context,
    private val itemInteractionListener: ItemInteractionListener,
    private val icoMovieTypeDrawable: Drawable,
    private val icoShowTypeDrawable: Drawable,
    private val currentTimeProvider: CurrentTimeProvider
) : RecyclerView.Adapter<ViewHolder>() {
    private val historyListTimeSeparatorAugmenter = HistoryListTimeSeparatorAugmenter(currentTimeProvider)
    private val monthNameFormatter = DateTimeFormatter.ofPattern("MMMMM")
    private val resources = context.resources

    interface ItemInteractionListener {
        fun onHistoryItemClicked(item: HistoryItem, position: Int)
        fun onWatchingItemClicked(item: Watching.Something, position: Int)
        fun onPagerClicked()
    }

    var model: HistoryModel = HistoryModel()
        set(value) {
            field = value
            val newItems = historyListTimeSeparatorAugmenter.augmentList(
                value.items.map { RowItemModel.HistoryRowItem(it) }
            )

            if (value.watching is Watching.Something) {
                newItems.add(0, RowItemModel.HeaderRowItem(RelativeWatchTime.Now))
                newItems.add(1, RowItemModel.WatchingRowItem(value.watching))
            }

            if (value.hasNextPage) {
                newItems.add(RowItemModel.PagerRowItem(value.loadingNextPage))
            }

            //use DiffUtil to do a proper change propagation
            val duCallback = RowItemModel.DiffUtilCallback(items, newItems)
            val diffResult = DiffUtil.calculateDiff(duCallback, true)
            items = newItems.toList()
            diffResult.dispatchUpdatesTo(this)

        }

    private var items: List<RowItemModel> = emptyList()

    override fun getItemViewType(position: Int): Int = items[position].viewType

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        return when (viewType) {
            RowItemModel.VIEW_TYPE_PAGER -> PagerViewHolder(layoutInflater.inflate(R.layout.item_history_pager, parent, false))
            RowItemModel.VIEW_TYPE_HEADER -> HeaderViewHolder(layoutInflater.inflate(R.layout.item_history_header, parent, false))
            else -> HistoryViewHolder(layoutInflater.inflate(R.layout.item_history, parent, false))
        }
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.let { items[position].bindViewHolder(it) }
    }

    override fun getItemCount(): Int {
        return items.size
    }


    open class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    inner class HistoryViewHolder(view: View) : ViewHolder(view), View.OnClickListener {
        @BindView(R.id.title) lateinit var title: TextView
        @BindView(R.id.subtitle) lateinit var subtitle: TextView
        @BindView(R.id.date) lateinit var date: TextView
        @BindView(R.id.type_info) lateinit var typeInfo: TextView

        private var unbinder: Unbinder = ButterKnife.bind(this, view)

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            onItemClicked(adapterPosition)
        }

        fun chooseTypeInfoIconAndText(type: MediaType, year: Int?) {
            val typeIcoDrawable = when (type) {
                MediaType.movie -> icoMovieTypeDrawable
                MediaType.episode -> icoShowTypeDrawable
                MediaType.show -> icoShowTypeDrawable
            }

            title.setCompoundDrawablesWithIntrinsicBounds(typeIcoDrawable.tint(context, R.color.colorAccent), null, null, null)
            typeInfo.text = resources.getString(R.string.media_item_type_info, type.toString(), year?.toString() ?: "")
        }
    }

    private fun onItemClicked(position: Int) {
        val item = items[position]
        when (item) {
            is RowItemModel.HistoryRowItem ->
                itemInteractionListener.onHistoryItemClicked(item.historyItem, position)
            is RowItemModel.PagerRowItem ->
                itemInteractionListener.onPagerClicked()
            is RowItemModel.HeaderRowItem -> Timber.d("Clicked on header $position")
            is RowItemModel.WatchingRowItem ->
                itemInteractionListener.onWatchingItemClicked(item.watching, position)
        }
    }


    inner class PagerViewHolder(view: View) : ViewHolder(view), View.OnClickListener {
        @BindView(R.id.text) lateinit var text: TextView
        @BindView(R.id.pager_progress) lateinit var pagerProgress: MaterialProgressBar
        private var unbinder: Unbinder = ButterKnife.bind(this, view)

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            onItemClicked(adapterPosition)
        }

        fun chooseText(isLoading: Boolean) {
            text.text = resources.getString(if (isLoading) R.string.pager_loading else R.string.pager_load_more)
        }
    }

    inner class HeaderViewHolder(view: View) : ViewHolder(view) {
        @BindView(R.id.text) lateinit var text: TextView
        private var unbinder: Unbinder = ButterKnife.bind(this, view)


        fun formatRelativeWatchTime(time: RelativeWatchTime) {
            text.text = when (time) {
                RelativeWatchTime.Now -> resources.getString(R.string.now_watching)
                RelativeWatchTime.Today -> resources.getString(R.string.today)
                RelativeWatchTime.Yesterday -> resources.getString(R.string.yesterday)
                is RelativeWatchTime.MonthsInPast -> if (time.monthCount == 0) {
                    resources.getString(R.string.this_month)
                } else if (time.monthCount == 1) {
                    resources.getString(R.string.last_month)
                } else {
                    monthNameFormatter.format(time.toMonth(currentTimeProvider.dateTime))
                }

            }
        }
    }



}


