package cz.josefadamcik.trackontrakt.util

import android.net.Uri

class AndroidUriQueryParamParser : UriQueryParamParser {
    override fun getUriParam(uri: String, name: String): String? {
        val androidUri = Uri.parse(uri)
        return androidUri?.getQueryParameter(name)
    }

}