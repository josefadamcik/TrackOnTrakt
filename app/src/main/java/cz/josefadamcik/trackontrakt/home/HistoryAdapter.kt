/*
 Copyright 2017 Josef Adamcik <josef.adamcik@gmail.com>

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
package cz.josefadamcik.trackontrakt.home

import android.content.res.Resources
import android.graphics.drawable.Drawable
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
import java.text.DateFormat


class HistoryAdapter(
    private val layoutInflater: LayoutInflater,
    private val resources: Resources,
    private val itemInteractionListener: OnItemInteractionListener,
    private val icoMovieTypeDrawable: Drawable,
    private val icoShowTypeDrawable: Drawable
) : RecyclerView.Adapter<ViewHolder>() {

    private val dateFormat: DateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
    var model: HistoryModel = HistoryModel()
        set(value) {
            field = value
            val newItems = mutableListOf<RowItem>()
            value.items.mapTo(newItems) { RowItem.HistoryRowItem(it) }
            if (value.hasNextPage) {
                newItems.add(RowItem.PagerRowItem)
            }
            items = newItems.toList()
            //TODO: use diffutils to do a propper notify
            notifyDataSetChanged()
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

        when (item) {
            is RowItem.HistoryRowItem -> item.historyItem.let { it ->
                if (holder is HistoryViewHolder) {
                    bindHistoryViewHolder(it, holder)
                }
            }
            is RowItem.PagerRowItem -> {
                //nop
            }
        }


    }

    private fun bindHistoryViewHolder(it: HistoryItem, holder: HistoryViewHolder) {
        val typeIcoDrawable = when (it.type) {
            MediaType.movie -> icoMovieTypeDrawable
            MediaType.episode -> icoShowTypeDrawable
            MediaType.show -> icoShowTypeDrawable
        }


        holder.title.text = it.title
        if (TextUtils.isEmpty(it.subtitle)) {
            holder.subtitle.visibility = View.GONE
        } else {
            holder.subtitle.visibility = View.VISIBLE
            holder.subtitle.text = it.subtitle
        }

        holder.date.text = dateFormat.format(it.watched_at)

        holder.typeInfo.setCompoundDrawablesWithIntrinsicBounds(typeIcoDrawable, null, null, null)
        holder.typeInfo.text = resources.getString(R.string.media_item_type_info, it.type.toString(), it.year.toString())
    }

    override fun getItemCount(): Int {
        return items.size
    }


    sealed class RowItem(val viewType: Int) {
        companion object {
            val VIEW_TYPE_HISTORY = 1
            val VIEW_TYPE_PAGER = 2
        }

        data class HistoryRowItem(val historyItem: HistoryItem) : RowItem(VIEW_TYPE_HISTORY)
        object PagerRowItem : RowItem(VIEW_TYPE_PAGER)
    }


    open class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    inner class HistoryViewHolder(view: View) : ViewHolder(view), View.OnClickListener {
        @BindView(R.id.title) lateinit var title: TextView
        @BindView(R.id.subtitle) lateinit var subtitle: TextView
        @BindView(R.id.date) lateinit var date: TextView
        @BindView(R.id.type_info) lateinit var typeInfo: TextView

        private var unbinder: Unbinder

        init {
//            ButterKnife.setDebug(true)
            unbinder = ButterKnife.bind(this, view)
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            itemInteractionListener.onHistoryItemClicked(model.items[adapterPosition], adapterPosition)
        }
    }

    inner class PagerViewHolder(view: View) : ViewHolder(view), View.OnClickListener {
        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            itemInteractionListener.onPagerClicked()
        }
    }

}
