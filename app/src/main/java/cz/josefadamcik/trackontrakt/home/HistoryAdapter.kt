package cz.josefadamcik.trackontrakt.home

import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
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
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import timber.log.Timber

class HistoryAdapter(
    private val layoutInflater: LayoutInflater,
    private val resources: Resources,
    private val itemInteractionListener: ItemInteractionListener,
    private val icoMovieTypeDrawable: Drawable,
    private val icoShowTypeDrawable: Drawable
) : RecyclerView.Adapter<ViewHolder>() {
    private val historyListTimeSeparatorAugmenter = HistoryListTimeSeparatorAugmenter()
    private val monthNameFormatter = DateTimeFormatter.ofPattern("MMMMM")

    interface ItemInteractionListener {
        fun onHistoryItemClicked(item: HistoryItem, position: Int)
        fun onWatchingItemClicked(item: Watching.Something, position: Int)
        fun onPagerClicked()
    }

    var model: HistoryModel = HistoryModel()
        set(value) {
            field = value
            val newItems = historyListTimeSeparatorAugmenter.augmentList(
                value.items.map { RowItem.HistoryRowItem(it) }
            )

            if (value.watching is Watching.Something) {
                newItems.add(0, RowItem.HeaderRowItem(RelativeWatchTime.Now))
                newItems.add(1, RowItem.WatchingRowItem(value.watching))
            }

            if (value.hasNextPage) {
                newItems.add(RowItem.PagerRowItem(value.loadingNextPage))
            }

            //use DiffUtil to do a proper change propagation
            val duCallback = DiffUtilCallback(items, newItems)
            val diffResult = DiffUtil.calculateDiff(duCallback, true)
            items = newItems.toList()
            diffResult.dispatchUpdatesTo(this)

        }

    private var items: List<RowItem> = emptyList()

    override fun getItemViewType(position: Int): Int = items[position].viewType

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        return when (viewType) {
            RowItem.VIEW_TYPE_PAGER -> PagerViewHolder(layoutInflater.inflate(R.layout.item_history_pager, parent, false))
            RowItem.VIEW_TYPE_HEADER -> HeaderViewHolder(layoutInflater.inflate(R.layout.item_history_header, parent, false))
            else -> HistoryViewHolder(layoutInflater.inflate(R.layout.item_history, parent, false))
        }
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.let { items[position].bindViewHolder(it) }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    sealed class RowItem(val viewType: Int) {
        companion object {
            val VIEW_TYPE_HISTORY = 1
            val VIEW_TYPE_PAGER = 2
            val VIEW_TYPE_HEADER = 3
            val VIEW_TYPE_WATCHING = 4
            val dateFormat: DateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            val timeFormat: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        }

        abstract fun bindViewHolder(holder: ViewHolder)

        class HistoryRowItem(val historyItem: HistoryItem) : RowItem(VIEW_TYPE_HISTORY) {
            override fun bindViewHolder(holder: ViewHolder) {
                if (holder is HistoryViewHolder) {
                    holder.title.text = historyItem.title
                    if (TextUtils.isEmpty(historyItem.subtitle)) {
                        holder.subtitle.visibility = View.GONE
                    } else {
                        holder.subtitle.visibility = View.VISIBLE
                        holder.subtitle.text = historyItem.subtitle
                    }

                    holder.date.text = dateFormat.format(historyItem.watched_at)
                    holder.chooseTypeInfoIconAndText(historyItem.type, historyItem.year)
                }
            }

        }

        class PagerRowItem(val isLoading: Boolean) : RowItem(VIEW_TYPE_PAGER) {
            override fun bindViewHolder(holder: ViewHolder) {
                if (holder is PagerViewHolder) {
                    holder.pagerProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
                    holder.chooseText(isLoading)
                }
            }
        }

        class HeaderRowItem(val time: RelativeWatchTime) : RowItem(VIEW_TYPE_HEADER) {
            override fun bindViewHolder(holder: ViewHolder) {
                if (holder is HeaderViewHolder) {
                    holder.formatRelativeWatchTime(time)
                }
            }
        }

        class WatchingRowItem(val watching: Watching.Something) : HistoryAdapter.RowItem(VIEW_TYPE_WATCHING) {
            override fun bindViewHolder(holder: ViewHolder) {
                if (holder is HistoryViewHolder) {
                    holder.title.text = watching.title
                    if (TextUtils.isEmpty(watching.subtitle)) {
                        holder.subtitle.visibility = View.GONE
                    } else {
                        holder.subtitle.visibility = View.VISIBLE
                        holder.subtitle.text = watching.subtitle
                    }


                    holder.date.text = String.format("%s (%s - %s)",
                        watching.action,
                        timeFormat.format(watching.started_at),
                        timeFormat.format(watching.expires_at)
                    )
                    holder.chooseTypeInfoIconAndText(watching.type, watching.year)
                }
            }

        }
    }


    open class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    }

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

            typeInfo.setCompoundDrawablesWithIntrinsicBounds(typeIcoDrawable, null, null, null)
            typeInfo.text = resources.getString(R.string.media_item_type_info, type.toString(), year?.toString() ?: "")
        }
    }

    private fun onItemClicked(position: Int) {
        val item = items[position]
        when (item) {
            is HistoryAdapter.RowItem.HistoryRowItem ->
                itemInteractionListener.onHistoryItemClicked(item.historyItem, position)
            is HistoryAdapter.RowItem.PagerRowItem ->
                itemInteractionListener.onPagerClicked()
            is HistoryAdapter.RowItem.HeaderRowItem -> Timber.d("Clicked on header $position")
            is HistoryAdapter.RowItem.WatchingRowItem ->
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
                    monthNameFormatter.format(time.toMonth(LocalDateTime.now()))
                }

            }
        }
    }


    class DiffUtilCallback(
        private val oldList: List<RowItem>,
        private val newList: List<RowItem>) : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]

            return (oldItem is RowItem.PagerRowItem && newItem is RowItem.PagerRowItem)
                || (oldItem is RowItem.HistoryRowItem && newItem is RowItem.HistoryRowItem && oldItem.historyItem.id == newItem.historyItem.id)
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]

            return oldItem == newItem //data classes have implemented equals...
        }

    }
}


