package cz.josefadamcik.trackontrakt.detail

import android.content.res.Resources
import android.support.v7.util.DiffUtil
import android.text.method.LinkMovementMethod
import android.view.View
import cz.josefadamcik.trackontrakt.R
import cz.josefadamcik.trackontrakt.data.api.model.EpisodeWithProgress
import cz.josefadamcik.trackontrakt.data.api.model.SeasonWithProgress

sealed class RowItemModel(val viewType: Int) {
    companion object {
        const val VIEWTYPE_MEDIA_INFO = 1
        const val VIEWTYPE_MEDIA_INFO_ROW = 2
        const val VIEWTYPE_EPISODE = 3
        const val VIEWTYPE_SEASON_HEADER = 4
        const val VIEWTYPE_NEXT_EPISODE_HEADER = 5
    }

    abstract fun bindViewHolder(holder: MediaDetailAdapter.ViewHolder, resources: Resources)

    object NextEpisodeHeaderRowItem : RowItemModel(VIEWTYPE_NEXT_EPISODE_HEADER) {
        override fun bindViewHolder(holder: MediaDetailAdapter.ViewHolder, resources: Resources) {}
    }

    data class MainInfoRowItem(val info: MediaDetailModel.MediaDetailInfo) : RowItemModel(VIEWTYPE_MEDIA_INFO) {
        override fun bindViewHolder(holder: MediaDetailAdapter.ViewHolder, resources: Resources) {
            if (holder is MediaDetailAdapter.MainInfoViewHolder) {
                if (info.tagline == null || info.tagline.isEmpty()) {
                    holder.txtTagline.visibility = View.GONE
                } else {
                    holder.txtTagline.visibility = View.VISIBLE
                    holder.txtTagline.text = info.tagline
                }
                if (info.description == null || info.description.isEmpty()) {
                    holder.txtDescription.visibility = View.GONE
                } else {
                    holder.txtDescription.visibility = View.VISIBLE
                    holder.txtDescription.text = info.description
                }
            }
        }
    }

    data class InfoRowItem(
            val labelResource: Int,
            val value: String,
            val iconResource: Int,
            val link: String? = null) : RowItemModel(VIEWTYPE_MEDIA_INFO_ROW) {
        override fun bindViewHolder(holder: MediaDetailAdapter.ViewHolder, resources: Resources) {
            if (holder is MediaDetailAdapter.InfoRowViewHolder) {
                holder.txtLabel.text = resources.getString(labelResource)
                holder.txtValue.movementMethod = LinkMovementMethod.getInstance()
                holder.txtValue.text = value
                holder.iconInfo.setImageResource(iconResource)
                holder.iconInfo.contentDescription = resources.getString(labelResource)
                if (link != null) {
                    holder.link = link
                    holder.txtValue.setSingleLine(true)
                    holder.itemView.setOnClickListener(holder)

                } else {
                    holder.link = null
                    holder.itemView.setOnClickListener(null)
                    holder.txtValue.setSingleLine(false)
                }
            }
        }
    }

    data class EpisodeRowItem(val episodeWithProgress: EpisodeWithProgress) : RowItemModel(VIEWTYPE_EPISODE) {
        override fun bindViewHolder(holder: MediaDetailAdapter.ViewHolder, resources: Resources) {
            if (holder is MediaDetailAdapter.EpisodeInfoViewHolder) {
                val episode = episodeWithProgress.episode
                val progress = episodeWithProgress.progress
                holder.txtEpisodeInfo.text = resources.getString(R.string.episode_item_number_and_season_info, episode.season, episode.number)
                holder.txtTitle.text = episode.title
                if (episode.overview.isNullOrEmpty()) {
                    holder.txtOverview.visibility = View.GONE
                } else {
                    holder.txtOverview.visibility = View.VISIBLE
                    holder.txtOverview.text = episode.overview
                }

                if (episode.rating == null || episode.votes == null) {
                    holder.txtRating.visibility = View.GONE
                } else {
                    holder.txtRating.visibility = View.VISIBLE
                    holder.txtRating.text = resources.getString(R.string.media_detail_votes, episode.rating * 10, episode.votes);
                }

                if (progress.completed) {
                    holder.btnCheckin.setImageDrawable(holder.drawableIcEye)
                } else {
                    holder.btnCheckin.setImageDrawable(holder.drawableIcCheck)
                }
//                    holder.btnCheckin.setColorFilter(resources.getColor(R.color.secondaryDarkColor), android.graphics.PorterDuff.Mode.SRC_IN)
            }
        }
    }

    data class SeasonRowItem(val seasonWithProgress: SeasonWithProgress, val state: SeasonRowItemState) : RowItemModel(VIEWTYPE_SEASON_HEADER) {
        override fun bindViewHolder(holder: MediaDetailAdapter.ViewHolder, resources: Resources) {
            if (holder is MediaDetailAdapter.SeasonHeaderViewHolder) {
                val season = seasonWithProgress.season

                if (!season.title.isNullOrEmpty()) {
                    holder.txtTitle.text = season.title
                } else {
                    if (season.number == 0) {
                        holder.txtTitle.text = resources.getString(R.string.season_specials_title)
                    } else {
                        holder.txtTitle.text = resources.getString(R.string.season_info, season.number)
                    }
                }

                if (!season.overview.isNullOrEmpty()) {
                    holder.txtOverview.text = season.overview
                    holder.txtOverview.visibility = View.VISIBLE
                } else {
                    holder.txtOverview.visibility = View.GONE
                }

                holder.icoExpandLess.visibility = View.GONE
                holder.icoExpandMore.visibility = View.GONE
                holder.progSeasonLoding.visibility = View.GONE

                when (state) {
                    SeasonRowItemState.Loading -> holder.progSeasonLoding.visibility = View.VISIBLE
                    SeasonRowItemState.Expanded -> holder.icoExpandLess.visibility = View.VISIBLE
                    SeasonRowItemState.Collapsed -> holder.icoExpandMore.visibility = View.VISIBLE
                }
            }
        }
    }

    sealed class SeasonRowItemState {
        object Loading : SeasonRowItemState()
        object Expanded : SeasonRowItemState()
        object Collapsed : SeasonRowItemState()
    }


    class DiffUtilCallback(
            private val oldList: List<RowItemModel>,
            private val newList: List<RowItemModel>) : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]

            return (oldItem is MainInfoRowItem && newItem is MainInfoRowItem)
                    || (oldItem is InfoRowItem && oldItem == newItem)
                    || (oldItem is NextEpisodeHeaderRowItem && newItem is NextEpisodeHeaderRowItem)
                    || (oldItem is SeasonRowItem && newItem is SeasonRowItem
                        && oldItem.seasonWithProgress.season.number == newItem.seasonWithProgress.season.number)
                    || (oldItem is EpisodeRowItem && newItem is EpisodeRowItem
                    && oldItem.episodeWithProgress.episode.ids.trakt == newItem.episodeWithProgress.episode.ids.trakt)
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