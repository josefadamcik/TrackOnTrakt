package cz.josefadamcik.trackontrakt.util

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.view.Menu


fun Drawable.tint(@ColorInt color: Int): Drawable {
    val result = DrawableCompat.wrap(this).mutate()
    DrawableCompat.setTint(result, color)
    return result
}

fun Drawable.tint(context: Context, @ColorRes colorRes: Int): Drawable {
    return this.tint(ContextCompat.getColor(context, colorRes))
}


fun Menu.tintIcons(context: Context, @ColorRes colorRes: Int) {
    val color = ContextCompat.getColor(context, colorRes)
    (0 until this.size())
            .map { this.getItem(it) }
            .forEach { it.icon?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP) }
}
