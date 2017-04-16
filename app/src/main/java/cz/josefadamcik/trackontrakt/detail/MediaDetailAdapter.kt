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
package cz.josefadamcik.trackontrakt.detail

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import cz.josefadamcik.trackontrakt.R

class MediaDetailAdapter(
    val inflater: LayoutInflater
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var model: MediaDetailModel? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    companion object {
        const val VIEWTYPE_MEDIA_INFO = 1
        const val VIEWTYPE_EPISODE = 2
        const val VIEWTYPE_SEASON_HEADER = 3
        const val VIEWTYPE_LAST_EPISODE_HEADER = 4
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return VIEWTYPE_MEDIA_INFO;
        } else if (model?.latestEpisode != null) {
            if (position == 1) {
                return VIEWTYPE_LAST_EPISODE_HEADER
            } else if (position == 2) {
                return VIEWTYPE_EPISODE
            }
        }
        return -1
    }


    override fun getItemCount(): Int {
        if (model == null) {
            return 0
        } else {
            var rowCount = 1
            if (model?.latestEpisode != null) {
                rowCount += 2
            }
            return rowCount
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when (holder) {
            is MainInfoViewHolder -> {
                holder.txtTagline.text = model?.basic?.tagline
                holder.txtDescription.text = model?.basic?.description
            }
            is EpisodeInfoViewHolder -> {
                holder.txtTitle.text = model?.latestEpisode?.title
            }
            else -> {

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val holder = when (viewType) {
            VIEWTYPE_MEDIA_INFO -> MainInfoViewHolder(inflater.inflate(R.layout.item_media_info, parent, false))
            VIEWTYPE_EPISODE -> EpisodeInfoViewHolder(inflater.inflate(R.layout.item_media_info_episode, parent, false))
            VIEWTYPE_LAST_EPISODE_HEADER -> ViewHolder(inflater.inflate(R.layout.item_media_info_latest_episode_separator, parent, false))
            else -> ViewHolder(null)
        }

        ButterKnife.bind(holder, holder.itemView)

        return holder
    }

    open class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
    }

    class MainInfoViewHolder(itemView: View?) : ViewHolder(itemView) {
        @BindView(R.id.txt_description) lateinit var txtDescription: TextView
        @BindView(R.id.txt_tagline) lateinit var txtTagline: TextView
    }

    class EpisodeInfoViewHolder(itemView: View?) : ViewHolder(itemView) {
        @BindView(R.id.title) lateinit var txtTitle: TextView
    }
}