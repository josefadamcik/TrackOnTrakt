package cz.josefadamcik.trackontrakt.util

import android.widget.TextView


/**
 *
 */
fun TextView.isEllipsized(): Boolean {
    var ellipsized = false
    val lineCount = layout?.lineCount ?: 0
    if (lineCount > 0) {
        val ellispisCount = layout?.getEllipsisCount(lineCount - 1) ?: 0
        ellipsized = ellispisCount > 0
    }
    return ellipsized
}