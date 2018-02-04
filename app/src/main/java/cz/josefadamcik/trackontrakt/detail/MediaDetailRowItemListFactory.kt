package cz.josefadamcik.trackontrakt.detail

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import cz.josefadamcik.trackontrakt.R.drawable
import cz.josefadamcik.trackontrakt.R.string

/**
 *
 */
class MediaDetailRowItemListFactory() {

    fun buildItems(model: MediaDetailModel): List<RowItemModel> {
        val list = mutableListOf<RowItemModel>()
        list.add(RowItemModel.MainInfoRowItem(model.basic))

        with(model.basic) {
            if (genres.isNotEmpty()) {
                list.add(itemFormInfoRow(string.label_genres, drawable.ic_label_outline_black_24dp, genres.joinToString(", ")))
            }
            network?.let { list.add(itemFormInfoRow(string.label_network, drawable.ic_television_classic, it)) }
            language?.let { list.add(itemFormInfoRow(string.label_language, drawable.ic_language_black_24dp, it)) }
            //status?.let { list.add(itemFormInfoRow(R.string.label_status, it)) }
            trailer?.let { list.add(itemFormInfoRow(string.label_trailer, drawable.ic_ondemand_video_black_24dp, it, it)) }
            homepage?.let { list.add(itemFormInfoRow(string.label_homepage, drawable.ic_web_black_24dp, it, it)) }
            list.add(itemFormInfoRow(string.label_traktpage, drawable.ic_web_black_24dp, traktPage ?: "", traktPage))
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
        return list
    }

    private fun itemFormInfoRow(@StringRes labelResource: Int, @DrawableRes iconResource: Int, value: String, link: String? = null): RowItemModel {
        return RowItemModel.InfoRowItem(labelResource, value, iconResource, link)
    }

}