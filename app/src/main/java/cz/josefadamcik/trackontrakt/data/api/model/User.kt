package cz.josefadamcik.trackontrakt.data.api.model

data class User(
    val username: String,
    val name: String,
    val `private`: Boolean,
    val vip: Boolean,
    val vip_ep: Boolean,
    val ids: Ids,
    val images: Map<String, Image>
) {
    companion object {
        const val IMAGES_KEY_AVATAR = "avatar"
    }
}