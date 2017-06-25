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

import android.content.res.Resources
import android.graphics.Typeface
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindColor
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.data.api.model.Episode
import cz.josefadamcik.trackontrakt.data.api.model.Season
import cz.josefadamcik.trackontrakt.util.spannable
import java.text.DateFormat


class MediaDetailAdapter(
    val inflater: LayoutInflater,
    val resources: Resources,
    val listener: InteractionListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val VIEWTYPE_MEDIA_INFO = 1
        const val VIEWTYPE_EPISODE = 2
        const val VIEWTYPE_SEASON_HEADER = 3
        const val VIEWTYPE_LAST_EPISODE_HEADER = 4
        val dateFormat: DateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
    }

    interface InteractionListener {
        fun onEpisodeCheckInClick(episode: Episode)
        fun onOpenWebPageClick(uri: Uri)
    }

    private var items = listOf<Item>()

    var model: MediaDetailModel? = null
        set(value) {
            field = value
            items = buildItems()
            notifyDataSetChanged()
        }


    override fun getItemViewType(position: Int): Int {
        return items[position].viewType
    }


    override fun getItemCount(): Int {
        return items.size
    }

    private fun buildItems(): List<Item> {
        val list = mutableListOf<Item>()
        val model = this.model
        if (model != null) {
            list.add(Item(VIEWTYPE_MEDIA_INFO))
            if (model.latestEpisode != null) {
                list.add(Item(VIEWTYPE_LAST_EPISODE_HEADER))
                list.add(Item(VIEWTYPE_EPISODE, episode = model.latestEpisode))
            }
            if (model.seasons.isNotEmpty()) {
                model.seasons.forEach { season ->
                    list.add(Item(VIEWTYPE_SEASON_HEADER, season = season))
                    season.episodes?.mapTo(list, transform = { ep -> Item(VIEWTYPE_EPISODE, season, ep) })
                }
            }
        }
        return list
    }

    private fun hasLatestEpisode() = model?.latestEpisode != null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when (holder) {
            is MainInfoViewHolder -> model?.basic?.let {
                if (it.tagline == null) {
                    holder.txtTagline.visibility = View.GONE
                } else {
                    holder.txtTagline.visibility = View.VISIBLE
                    holder.txtTagline.text = it.tagline
                }
                holder.txtDescription.text = it.description
                holder.txtYear.text = it.year
                holder.txtRating.text = resources.getString(R.string.media_detail_votes, it.rating * 10, it.votes)
                holder.txtOther.movementMethod = LinkMovementMethod.getInstance()

                val otherSpan = spannable {
                    if (it.genres.isNotEmpty()) {
//                        roundedBg(holder.otherBgColor, holder.otherColor) {
                        typeface(Typeface.BOLD) {
                            +resources.getString(R.string.label_genres)
                        }
                        +":"
                        +it.genres.joinToString(", ")
//                        }
                        +" "
                    }

                    if (it.network != null) {
//                        roundedBg(holder.otherBgColor, holder.otherColor) {
                        typeface(Typeface.BOLD) {
                            +resources.getString(R.string.label_network)
                        }
                        +":"
                        +it.network
//                        }
                        +" "

                    }

                    if (it.homepage != null) {
//                        roundedBg(holder.otherBgColor, holder.otherColor) {
                        holder.homepageUri = Uri.parse(it.homepage)
                        typeface(Typeface.BOLD) {
                            clickable({ holder.onClickHomepage() }) {
                                +resources.getString(R.string.label_homepage)
                            }
                        }
//                        }
                        +" "
                    }


                }

                holder.txtOther.text = otherSpan.toCharSequence()
            }
            is EpisodeInfoViewHolder -> {
                items[position].episode?.let {
                    holder.txtEpisodeInfo.text = resources.getString(R.string.episode_item_number_and_season_info, it.season, it.number)
                    holder.txtTitle.text = it.title
                }
            }
            is HeaderInfoViewHolder -> {
                items[position].season?.let {
                    holder.txtTitle.text = resources.getString(R.string.season_info, it.number)
                }
            }
            else -> {

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val holder = when (viewType) {
            VIEWTYPE_MEDIA_INFO -> MainInfoViewHolder(inflater.inflate(R.layout.item_media_info, parent, false), listener)
            VIEWTYPE_EPISODE -> EpisodeInfoViewHolder(inflater.inflate(R.layout.item_media_info_episode, parent, false))
            VIEWTYPE_SEASON_HEADER -> HeaderInfoViewHolder(inflater.inflate(R.layout.item_media_info_season_header, parent, false))
            VIEWTYPE_LAST_EPISODE_HEADER -> ViewHolder(inflater.inflate(R.layout.item_media_info_latest_episode_separator, parent, false))
            else -> ViewHolder(null)
        }

        ButterKnife.bind(holder, holder.itemView)

        return holder
    }

    open class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView)

    class MainInfoViewHolder(itemView: View?, val listener: InteractionListener) : ViewHolder(itemView) {
        @BindView(R.id.txt_description) lateinit var txtDescription: TextView
        @BindView(R.id.txt_tagline) lateinit var txtTagline: TextView
        @BindView(R.id.txt_year) lateinit var txtYear: TextView
        @BindView(R.id.txt_rating) lateinit var txtRating: TextView
        @BindView(R.id.txt_other) lateinit var txtOther: TextView
        @JvmField @BindColor(R.color.material_color_blue_grey_500) var otherBgColor: Int = 0
        @JvmField @BindColor(android.R.color.white) var otherColor: Int = 0
        var homepageUri: Uri? = null

        fun onClickHomepage() {
            if (homepageUri != null) {
                listener.onOpenWebPageClick(homepageUri as Uri)
            }
        }
    }

    inner class EpisodeInfoViewHolder(itemView: View?) : ViewHolder(itemView) {

        @BindView(R.id.title) lateinit var txtTitle: TextView
        @BindView(R.id.episode_info) lateinit var txtEpisodeInfo: TextView
        @OnClick(R.id.btn_checkin) fun onCheckinClick() {
            items[adapterPosition].episode?.let { listener.onEpisodeCheckInClick(it) }
        }

    }

    class HeaderInfoViewHolder(itemView: View?) : ViewHolder(itemView) {
        @BindView(R.id.title) lateinit var txtTitle: TextView
    }

    data class Item(val viewType: Int, val season: Season? = null, val episode: Episode? = null)
}