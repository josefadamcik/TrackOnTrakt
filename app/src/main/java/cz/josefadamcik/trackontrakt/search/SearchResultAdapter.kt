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
package cz.josefadamcik.trackontrakt.search

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
import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.data.api.model.MediaType
import cz.josefadamcik.trackontrakt.data.api.model.SearchResultItem
import cz.josefadamcik.trackontrakt.search.SearchResultAdapter.ViewHolder
import java.text.DateFormat


class SearchResultAdapter(
    val layoutInflater: LayoutInflater,
    val resources: Resources,
    val icoMovieTypeDrawable: Drawable,
    val icoShowTypeDrawable: Drawable,
    val listener: OnItemInteractionListener
) : RecyclerView.Adapter<ViewHolder>() {

    interface OnItemInteractionListener {
        fun onSearchResultClicked(item: SearchResultItem, position: Int)
    }

    val dateFormat: DateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)

    var items: List<SearchResultItem> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        return ViewHolder(layoutInflater.inflate(R.layout.item_search_result, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val item = items[position]
        holder?.title?.text = item.title
        if (!TextUtils.isEmpty(item.subtitle)) {
            holder?.subtitle?.text = item.subtitle
            holder?.subtitle?.visibility = View.VISIBLE
        } else {
            holder?.subtitle?.visibility = View.GONE
        }


        val typeIcoDrawable = when(item.type) {
            MediaType.movie -> icoMovieTypeDrawable
            MediaType.episode -> icoShowTypeDrawable
            MediaType.show -> icoShowTypeDrawable
        }

        holder?.typeInfo?.setCompoundDrawablesWithIntrinsicBounds(typeIcoDrawable, null, null, null)
        holder?.typeInfo?.text = resources.getString(R.string.media_item_type_info, item.type.toString(), item.year.toString())
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        @BindView(R.id.title) lateinit var title: TextView
        @BindView(R.id.subtitle) lateinit var subtitle: TextView
        @BindView(R.id.type_info) lateinit var typeInfo: TextView

        init {
            ButterKnife.bind(this, view)
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            listener.onSearchResultClicked(items[adapterPosition], adapterPosition)
        }
    }

}