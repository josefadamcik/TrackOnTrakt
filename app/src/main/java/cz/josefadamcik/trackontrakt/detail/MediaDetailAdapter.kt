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
import android.net.Uri
import android.support.annotation.StringRes
import android.support.v7.widget.RecyclerView
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.data.api.model.Episode
import cz.josefadamcik.trackontrakt.data.api.model.Season
import cz.josefadamcik.trackontrakt.util.RoundedBackgroundSpan
import java.text.DateFormat


class MediaDetailAdapter(
    private val inflater: LayoutInflater,
    val resources: Resources,
    val listener: InteractionListener,
    private val roundedSpanConfig: RoundedBackgroundSpan.Config
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val VIEWTYPE_MEDIA_INFO = 1
        const val VIEWTYPE_MEDIA_INFO_ROW = 2
        const val VIEWTYPE_EPISODE = 3
        const val VIEWTYPE_SEASON_HEADER = 4
        const val VIEWTYPE_LAST_EPISODE_HEADER = 5
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

            with(model.basic) {
                if (genres.isNotEmpty()) {
                    list.add(itemFormInfoRow(R.string.label_genres, genres.joinToString(", ")))
                }
                network?.let { list.add(itemFormInfoRow(R.string.label_network, it)) }
                language?.let { list.add(itemFormInfoRow(R.string.label_language, it)) }
                status?.let { list.add(itemFormInfoRow(R.string.label_status, it)) }
                trailer?.let { list.add(itemFormInfoRow(R.string.label_trailer, it, it)) }
                homepage?.let { list.add(itemFormInfoRow(R.string.label_homepage, it, it)) }
                list.add(itemFormInfoRow(R.string.label_traktpage, traktPage, traktPage))
            }

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

    private fun itemFormInfoRow(@StringRes labelResource: Int, value: String, link: String? = null): Item {
        return Item(
            VIEWTYPE_MEDIA_INFO_ROW,
            infoItem = InfoItem(resources.getString(labelResource), value, link)
        )
    }
    private fun hasLatestEpisode() = model?.latestEpisode != null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when (holder) {
            is MainInfoViewHolder -> model?.basic?.let {
                if (it.tagline == null || it.tagline.isEmpty()) {
                    holder.txtTagline.visibility = View.GONE
                } else {
                    holder.txtTagline.visibility = View.VISIBLE
                    holder.txtTagline.text = it.tagline
                }
                if (it.description == null || it.description.isEmpty()) {
                    holder.txtDescription.visibility = View.GONE
                } else {
                    holder.txtDescription.visibility = View.VISIBLE
                    holder.txtDescription.text = it.description
                }
            }
            is InfoRowViewHolder -> {
                items[position].infoItem?.let {
                    holder.txtLabel.text = it.label
                    holder.txtValue.movementMethod = LinkMovementMethod.getInstance()
                    holder.txtValue.text = it.value
                    if (it.link != null) {
                        holder.txtValue.tag = it.link
                        holder.txtValue.setSingleLine(true)
                        holder.txtValue.setOnClickListener(holder)
                    } else {
                        holder.txtValue.setSingleLine(false)
                        holder.txtValue.setOnClickListener(null)
                    }
                }
            }
            is EpisodeInfoViewHolder -> {
                items[position].episode?.let {
                    holder.txtEpisodeInfo.text = resources.getString(R.string.episode_item_number_and_season_info, it.season, it.number)
                    holder.txtTitle.text = it.title
                }
            }
            is HeaderInfoViewHolder -> {
                items[position].season?.let {
                    if (it.number == 0) {
                        holder.txtTitle.text = resources.getString(R.string.season_specials_title)
                    } else {
                        holder.txtTitle.text = resources.getString(R.string.season_info, it.number)
                    }
                }
            }
            else -> {

            }
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val holder = when (viewType) {
            VIEWTYPE_MEDIA_INFO -> MainInfoViewHolder(inflater.inflate(R.layout.item_media_info, parent, false), listener)
            VIEWTYPE_MEDIA_INFO_ROW -> InfoRowViewHolder(inflater.inflate(R.layout.item_media_info_row, parent, false), listener)
            VIEWTYPE_EPISODE -> EpisodeInfoViewHolder(inflater.inflate(R.layout.item_media_info_episode, parent, false))
            VIEWTYPE_SEASON_HEADER -> HeaderInfoViewHolder(inflater.inflate(R.layout.item_media_info_season_header, parent, false))
            VIEWTYPE_LAST_EPISODE_HEADER -> ViewHolder(inflater.inflate(R.layout.item_media_info_latest_episode_separator, parent, false))
            else -> ViewHolder(null)
        }

        ButterKnife.bind(holder, holder.itemView)

        return holder
    }

    open class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

    }

    class MainInfoViewHolder(itemView: View?, private val listener: InteractionListener) : ViewHolder(itemView) {
        @BindView(R.id.txt_description) lateinit var txtDescription: TextView
        @BindView(R.id.txt_tagline) lateinit var txtTagline: TextView
        var homepageUri: Uri? = null
        var trailerUri: Uri? = null
        var traktUri: Uri? = null

        fun onClickHomepage() {
            if (homepageUri != null) {
                listener.onOpenWebPageClick(homepageUri as Uri)
            }
        }

        fun onClickTrailer() {
            if (trailerUri != null) {
                listener.onOpenWebPageClick(trailerUri as Uri)
            }
        }

        fun onClickTraktpage() {
            if (traktUri != null) {
                listener.onOpenWebPageClick(traktUri as Uri)
            }
        }
    }

    inner class InfoRowViewHolder(itemView: View?, listener: InteractionListener) : ViewHolder(itemView), View.OnClickListener {
        @BindView(R.id.txt_label) lateinit var txtLabel: TextView
        @BindView(R.id.txt_value) lateinit var txtValue: TextView

        override fun onClick(view: View?) {
            if (view?.tag != null) {
                listener.onOpenWebPageClick(Uri.parse(view.tag as String))
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

    data class Item(val viewType: Int, val season: Season? = null, val episode: Episode? = null, val infoItem: InfoItem? = null)
    data class InfoItem(val label: String, val value: String, val link: String? = null)
}