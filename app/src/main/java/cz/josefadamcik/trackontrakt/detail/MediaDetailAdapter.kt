
package cz.josefadamcik.trackontrakt.detail

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindDrawable
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.data.api.model.EpisodeWithProgress
import cz.josefadamcik.trackontrakt.data.api.model.SeasonWithProgress
import cz.josefadamcik.trackontrakt.util.RoundedBackgroundSpan
import cz.josefadamcik.trackontrakt.util.isEllipsized
import java.text.DateFormat


class MediaDetailAdapter(
    private val inflater: LayoutInflater,
    private val resources: Resources,
    private val context: Context,
    private val listener: InteractionListener,
    private val roundedSpanConfig: RoundedBackgroundSpan.Config
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val VIEWTYPE_MEDIA_INFO = 1
        const val VIEWTYPE_MEDIA_INFO_ROW = 2
        const val VIEWTYPE_EPISODE = 3
        const val VIEWTYPE_SEASON_HEADER = 4
        const val VIEWTYPE_NEXT_EPISODE_HEADER = 5
        val dateFormat: DateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
    }

    interface InteractionListener {
        fun onEpisodeCheckInClick(episode: EpisodeWithProgress)
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
                    list.add(itemFormInfoRow(R.string.label_genres, R.drawable.ic_label_outline_black_24dp, genres.joinToString(", ")))
                }
                network?.let { list.add(itemFormInfoRow(R.string.label_network, R.drawable.ic_television_classic, it)) }
                language?.let { list.add(itemFormInfoRow(R.string.label_language, R.drawable.ic_language_black_24dp, it)) }
                //status?.let { list.add(itemFormInfoRow(R.string.label_status, it)) }
                trailer?.let { list.add(itemFormInfoRow(R.string.label_trailer, R.drawable.ic_ondemand_video_black_24dp, it, it)) }
                homepage?.let { list.add(itemFormInfoRow(R.string.label_homepage, R.drawable.ic_web_black_24dp, it, it)) }
                list.add(itemFormInfoRow(R.string.label_traktpage, R.drawable.ic_web_black_24dp, traktPage ?: "", traktPage))
            }

            model.nextShowEpisodeToWatch?.let { (season, episode) ->
                list.add(Item(VIEWTYPE_NEXT_EPISODE_HEADER))
                list.add(Item(VIEWTYPE_EPISODE, season, episode))
            }

            if (model.seasons.isNotEmpty()) {
                model.seasons.forEach { season ->
                    list.add(Item(VIEWTYPE_SEASON_HEADER, season = season))
                    season.episodes.mapTo(list, transform = { ep -> Item(VIEWTYPE_EPISODE, season, ep) })
                }
            }
        }
        return list
    }

    private fun itemFormInfoRow(@StringRes labelResource: Int, @DrawableRes iconResource: Int, value: String, link: String? = null): Item {
        return Item(
            VIEWTYPE_MEDIA_INFO_ROW,
            infoItem = InfoItem(resources.getString(labelResource), value, iconResource, link)
        )
    }

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
                    holder.iconInfo.setImageResource(it.iconResource)
                    holder.iconInfo.contentDescription = it.label
                    if (it.link != null) {
                        holder.link = it.link
                        holder.txtValue.setSingleLine(true)
                        holder.itemView.setOnClickListener(holder)

                    } else {
                        holder.link = null
                        holder.itemView.setOnClickListener(null)
                        holder.txtValue.setSingleLine(false)
                    }
                }
            }
            is EpisodeInfoViewHolder -> {
                items[position].episode?.let {

                    holder.txtEpisodeInfo.text = resources.getString(R.string.episode_item_number_and_season_info, it.episode.season, it.episode.number)
                    holder.txtTitle.text = it.episode.title
                    if (it.episode.overview.isNullOrEmpty()) {
                        holder.txtOverview.visibility = View.GONE
                    } else {
                        holder.txtOverview.visibility = View.VISIBLE
                        holder.txtOverview.text = it.episode.overview
                    }


                    if (it.episode.rating == null || it.episode.votes == null) {
                        holder.txtRating.visibility = View.GONE
                    } else {
                        holder.txtRating.visibility = View.VISIBLE
                        holder.txtRating.text = resources.getString(R.string.media_detail_votes, it.episode.rating * 10, it.episode.votes);
                    }



                    if (it.progress.completed) {
                        holder.btnCheckin.setImageDrawable(holder.drawableIcEye)
                    } else {
                        holder.btnCheckin.setImageDrawable(holder.drawableIcCheck)
                    }
                    holder.btnCheckin.setColorFilter(resources.getColor(R.color.secondaryDarkColor), android.graphics.PorterDuff.Mode.SRC_IN)
                }
            }
            is SeasonHeaderViewHolder -> {
                items[position].season?.let {
                    if (!it.season.title.isNullOrEmpty()) {
                        holder.txtTitle.text = it.season.title
                    } else {
                        if (it.season.number == 0) {
                            holder.txtTitle.text = resources.getString(R.string.season_specials_title)
                        } else {
                            holder.txtTitle.text = resources.getString(R.string.season_info, it.season.number)
                        }
                    }

                    if (!it.season.overview.isNullOrEmpty()) {
                        holder.txtOverview.text = it.season.overview
                        holder.txtOverview.visibility = View.VISIBLE
                    } else {
                        holder.txtOverview.visibility = View.GONE
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
            VIEWTYPE_SEASON_HEADER -> SeasonHeaderViewHolder(inflater.inflate(R.layout.item_media_info_season_header, parent, false))
            VIEWTYPE_NEXT_EPISODE_HEADER -> ViewHolder(inflater.inflate(R.layout.item_media_info_next_episode_separator, parent, false))
            else ->  /*dummy*/ ViewHolder(inflater.inflate(R.layout.item_media_info_next_episode_separator, parent, false))
        }



        return holder
    }

    open inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            ButterKnife.bind(this, itemView)
        }

        protected fun toggleEllipsize(txtOverview: TextView) {
            if (txtOverview.isEllipsized()) {
                txtOverview.maxLines = Integer.MAX_VALUE
                txtOverview.ellipsize = null
            } else {
                txtOverview.maxLines = resources.getInteger(R.integer.media_detail_overview_maxlines)
                txtOverview.ellipsize = TextUtils.TruncateAt.END
            }
        }
    }

    inner class MainInfoViewHolder(itemView: View, private val listener: InteractionListener) : ViewHolder(itemView), View.OnClickListener {
        @BindView(R.id.txt_description) lateinit var txtDescription: TextView
        @BindView(R.id.txt_tagline) lateinit var txtTagline: TextView

        init {
            itemView?.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            toggleEllipsize(txtDescription)
        }
    }

    inner class InfoRowViewHolder(itemView: View, listener: InteractionListener) : ViewHolder(itemView), View.OnClickListener {
        public var link: String? = null
        @BindView(R.id.txt_label) lateinit var txtLabel: TextView
        @BindView(R.id.txt_value) lateinit var txtValue: TextView
        @BindView(R.id.icon_info) lateinit var iconInfo: ImageView

        override fun onClick(view: View?) {
            if (link != null) {
                listener.onOpenWebPageClick(Uri.parse(link))
            }
        }
    }

    inner class EpisodeInfoViewHolder(itemView: View) : ViewHolder(itemView), View.OnClickListener {
        @BindView(R.id.title) lateinit var txtTitle: TextView
        @BindView(R.id.overview) lateinit var txtOverview: TextView
        @BindView(R.id.episode_info) lateinit var txtEpisodeInfo: TextView
        @BindView(R.id.rating) lateinit var txtRating: TextView
        @BindView(R.id.btn_checkin) lateinit var btnCheckin: ImageView
        @BindDrawable(R.drawable.ic_check_circle_black_24dp) lateinit var drawableIcCheck: Drawable
        @BindDrawable(R.drawable.ic_remove_red_eye_black_24dp) lateinit var drawableIcEye: Drawable
        @BindDrawable(R.drawable.ic_thumbs_up_down_black_20dp) lateinit var drawableIcThumbsUpDown: Drawable

        @OnClick(R.id.btn_checkin) fun onCheckinClick() {
            items[adapterPosition].episode?.let { listener.onEpisodeCheckInClick(it) }
        }

        init {
            itemView.setOnClickListener(this)
            val drawable = DrawableCompat.wrap(drawableIcThumbsUpDown).mutate()
            DrawableCompat.setTint(drawable, ContextCompat.getColor(context, R.color.textColorSecondary))
            txtRating.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        }

        override fun onClick(v: View?) {
            toggleEllipsize(txtOverview)
        }
    }

    inner class SeasonHeaderViewHolder(itemView: View) : ViewHolder(itemView), View.OnClickListener {
        @BindView(R.id.title) lateinit var txtTitle: TextView
        @BindView(R.id.overview) lateinit var txtOverview: TextView

        init {
            itemView?.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            toggleEllipsize(txtOverview)
        }
    }

    data class Item(val viewType: Int, val season: SeasonWithProgress? = null, val episode: EpisodeWithProgress? = null, val infoItem: InfoItem? = null)
    data class InfoItem(val label: String, val value: String, val iconResource: Int, val link: String? = null)
}