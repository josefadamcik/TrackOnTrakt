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
import cz.josefadamcik.trackontrakt.home.HistoryAdapter.ViewHolder
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import java.text.DateFormat


class HistoryAdapter(
    private val layoutInflater: LayoutInflater,
    private val resources: Resources,
    private val itemInteractionListener: OnItemInteractionListener,
    private val icoMovieTypeDrawable: Drawable,
    private val icoShowTypeDrawable: Drawable
) : RecyclerView.Adapter<ViewHolder>() {

    var model: HistoryModel = HistoryModel()
        set(value) {
            field = value
            val newItems = mutableListOf<RowItem>()
            value.items.mapTo(newItems) { RowItem.HistoryRowItem(it) }
            if (value.hasNextPage) {
                newItems.add(RowItem.PagerRowItem(value.loadingNextPage))
            }

            //use DiffUtil to do a proper change propagation
            val duCallback = DiffUtilCallback(items, newItems)
            val diffResult = DiffUtil.calculateDiff(duCallback, true)
            items = newItems.toList()
            diffResult.dispatchUpdatesTo(this)

        }
    var items: List<RowItem> = emptyList()


    interface OnItemInteractionListener {
        fun onHistoryItemClicked(item: HistoryItem, position: Int)
        fun onPagerClicked()
    }

    override fun getItemViewType(position: Int): Int = items[position].viewType

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        if (viewType == RowItem.VIEW_TYPE_PAGER) {
            return PagerViewHolder(layoutInflater.inflate(R.layout.item_history_pager, parent, false))
        } else {
            return HistoryViewHolder(layoutInflater.inflate(R.layout.item_history, parent, false))
        }
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val item = items[position]

        holder?.let { item.bindViewHolder(it) }



    }

    private fun bindPagerViewHolder(item: RowItem.PagerRowItem, viewHolder: PagerViewHolder) {
        viewHolder.pagerProgress.visibility = if (item.isLoading) View.VISIBLE else View.GONE
        viewHolder.text.text = resources.getString(if (item.isLoading) R.string.pager_loading else R.string.pager_load_more)
    }





    override fun getItemCount(): Int {
        return items.size
    }


    sealed class RowItem(val viewType: Int) {
        companion object {
            val VIEW_TYPE_HISTORY = 1
            val VIEW_TYPE_PAGER = 2
        }

        abstract fun bindViewHolder(holder: ViewHolder)

        class HistoryRowItem(val historyItem: HistoryItem) : RowItem(VIEW_TYPE_HISTORY) {
            private val dateFormat: DateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)

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
            itemInteractionListener.onHistoryItemClicked(model.items[adapterPosition], adapterPosition)
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


    inner class PagerViewHolder(view: View) : ViewHolder(view), View.OnClickListener {
        @BindView(R.id.text) lateinit var text: TextView
        @BindView(R.id.pager_progress) lateinit var pagerProgress: MaterialProgressBar
        private var unbinder: Unbinder = ButterKnife.bind(this, view)

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            itemInteractionListener.onPagerClicked()
        }

        fun chooseText(isLoading: Boolean) {
            text.text = resources.getString(if (isLoading) R.string.pager_loading else R.string.pager_load_more)
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
