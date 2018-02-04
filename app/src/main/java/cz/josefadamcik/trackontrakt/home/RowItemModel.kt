package cz.josefadamcik.trackontrakt.home

import android.support.v7.util.DiffUtil
import android.text.TextUtils
import android.view.View
import cz.josefadamcik.trackontrakt.data.api.model.HistoryItem
import cz.josefadamcik.trackontrakt.data.api.model.Watching
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle

sealed class RowItemModel(val viewType: Int) {
    companion object {
        val VIEW_TYPE_HISTORY = 1
        val VIEW_TYPE_PAGER = 2
        val VIEW_TYPE_HEADER = 3
        val VIEW_TYPE_WATCHING = 4
        val dateFormat: DateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
        val timeFormat: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
    }

    abstract fun bindViewHolder(holder: HistoryAdapter.ViewHolder)

    class HistoryRowItem(val historyItem: HistoryItem) : RowItemModel(VIEW_TYPE_HISTORY) {
        override fun bindViewHolder(holder: HistoryAdapter.ViewHolder) {
            if (holder is HistoryAdapter.HistoryViewHolder) {
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

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as HistoryRowItem
            if (historyItem != other.historyItem) return false

            return true
        }

        override fun hashCode(): Int {
            return historyItem.hashCode()
        }


    }

    class PagerRowItem(val isLoading: Boolean) : RowItemModel(VIEW_TYPE_PAGER) {
        override fun bindViewHolder(holder: HistoryAdapter.ViewHolder) {
            if (holder is HistoryAdapter.PagerViewHolder) {
                holder.pagerProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
                holder.chooseText(isLoading)
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as PagerRowItem

            if (isLoading != other.isLoading) return false

            return true
        }

        override fun hashCode(): Int {
            return isLoading.hashCode()
        }


    }

    class HeaderRowItem(val time: RelativeWatchTime) : RowItemModel(VIEW_TYPE_HEADER) {
        override fun bindViewHolder(holder: HistoryAdapter.ViewHolder) {
            if (holder is HistoryAdapter.HeaderViewHolder) {
                holder.formatRelativeWatchTime(time)
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as HeaderRowItem

            if (time != other.time) return false

            return true
        }

        override fun hashCode(): Int {
            return time.hashCode()
        }


    }

    class WatchingRowItem(val watching: Watching.Something) : RowItemModel(VIEW_TYPE_WATCHING) {
        override fun bindViewHolder(holder: HistoryAdapter.ViewHolder) {
            if (holder is HistoryAdapter.HistoryViewHolder) {
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

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as WatchingRowItem

            if (watching != other.watching) return false

            return true
        }

        override fun hashCode(): Int {
            return watching.hashCode()
        }
    }

    class DiffUtilCallback(
            private val oldList: List<RowItemModel>,
            private val newList: List<RowItemModel>) : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]

            return (oldItem is RowItemModel.PagerRowItem && newItem is RowItemModel.PagerRowItem)
                    || (oldItem is RowItemModel.WatchingRowItem && newItem is RowItemModel.WatchingRowItem)
                    || (oldItem is RowItemModel.HeaderRowItem && newItem is RowItemModel.HeaderRowItem)
                    || (oldItem is RowItemModel.HistoryRowItem && newItem is RowItemModel.HistoryRowItem && oldItem.historyItem.id == newItem.historyItem.id)
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