package cz.josefadamcik.trackontrakt.testutil

import android.content.Context
import java.io.IOException
import java.io.InputStream

fun asset(context: Context, assetPath: String): String {
    try {
        val buf = StringBuilder()
        val inputStream = context.assets.open("apimock/" + assetPath)
        return convertStreamToString(inputStream, "UTF-8")
    } catch (e: IOException) {
        throw RuntimeException(e)
    }

}

private fun convertStreamToString(input: InputStream, charsetName: String): String {
    val s = java.util.Scanner(input, charsetName).useDelimiter("\\A")
    return if (s.hasNext()) s.next() else ""
}
