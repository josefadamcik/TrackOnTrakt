
package cz.josefadamcik.trackontrakt.detail

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
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
import cz.josefadamcik.trackontrakt.util.RoundedBackgroundSpan
import cz.josefadamcik.trackontrakt.util.isEllipsized
import cz.josefadamcik.trackontrakt.util.tint


class MediaDetailAdapter(
    private val inflater: LayoutInflater,
    private val resources: Resources,
    private val context: Context,
    private val listener: InteractionListener,
    private val roundedSpanConfig: RoundedBackgroundSpan.Config
) : RecyclerView.Adapter<MediaDetailAdapter.ViewHolder>() {

    interface InteractionListener {
        fun onEpisodeCheckInClick(episode: EpisodeWithProgress)
        fun onOpenWebPageClick(uri: Uri)
    }

    private var items = listOf<RowItemModel>()

    var model: MediaDetailModel? = null
        set(value) {
            field = value
            val newItems  = buildItems()
            val duCallback = RowItemModel.DiffUtilCallback(items, newItems)
            val diffResult = DiffUtil.calculateDiff(duCallback, true)
            items = newItems
            diffResult.dispatchUpdatesTo(this)
        }


    override fun getItemViewType(position: Int): Int {
        return items[position].viewType
    }


    override fun getItemCount(): Int {
        return items.size
    }

    private fun buildItems(): List<RowItemModel> {
        val list = mutableListOf<RowItemModel>()
        val model = this.model
        if (model != null) {
            list.add(RowItemModel.MainInfoRowItem(model.basic))

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
                list.add(RowItemModel.NextEpisodeHeaderRowItem)
                list.add(RowItemModel.EpisodeRowItem(episode))
            }

            if (model.seasons.isNotEmpty()) {
                model.seasons.forEach { season ->
                    list.add(RowItemModel.SeasonRowItem(season))
                    season.episodes.mapTo(list, transform = { ep -> RowItemModel.EpisodeRowItem(ep) })
                }
            }
        }
        return list
    }

    private fun itemFormInfoRow(@StringRes labelResource: Int, @DrawableRes iconResource: Int, value: String, link: String? = null): RowItemModel {
        return RowItemModel.InfoRowItem(resources.getString(labelResource), value, iconResource, link)
    }

    override fun onBindViewHolder(holder: MediaDetailAdapter.ViewHolder?, position: Int) {
        holder?.let { items[position].bindViewHolder(it, resources) }
    }



    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MediaDetailAdapter.ViewHolder {
        return when (viewType) {
            RowItemModel.VIEWTYPE_MEDIA_INFO -> MainInfoViewHolder(inflater.inflate(R.layout.item_media_info, parent, false), listener)
            RowItemModel.VIEWTYPE_MEDIA_INFO_ROW -> InfoRowViewHolder(inflater.inflate(R.layout.item_media_info_row, parent, false), listener)
            RowItemModel.VIEWTYPE_EPISODE -> EpisodeInfoViewHolder(inflater.inflate(R.layout.item_media_info_episode, parent, false))
            RowItemModel.VIEWTYPE_SEASON_HEADER -> SeasonHeaderViewHolder(inflater.inflate(R.layout.item_media_info_season_header, parent, false))
            RowItemModel.VIEWTYPE_NEXT_EPISODE_HEADER -> ViewHolder(inflater.inflate(R.layout.item_media_info_next_episode_separator, parent, false))
            else ->  /*dummy*/ ViewHolder(inflater.inflate(R.layout.item_media_info_next_episode_separator, parent, false))
        }
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
            itemView.setOnClickListener(this)
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
            (items[adapterPosition] as RowItemModel.EpisodeRowItem).episodeWithProgress.let { listener.onEpisodeCheckInClick(it) }
        }

        init {
            itemView.setOnClickListener(this)
            txtRating.setCompoundDrawablesWithIntrinsicBounds(drawableIcThumbsUpDown.tint(context, R.color.textColorSecondary), null, null, null)
        }

        override fun onClick(v: View?) {
            toggleEllipsize(txtOverview)
        }
    }

    inner class SeasonHeaderViewHolder(itemView: View) : ViewHolder(itemView), View.OnClickListener {
        @BindView(R.id.title) lateinit var txtTitle: TextView
        @BindView(R.id.overview) lateinit var txtOverview: TextView

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            toggleEllipsize(txtOverview)
        }
    }
}