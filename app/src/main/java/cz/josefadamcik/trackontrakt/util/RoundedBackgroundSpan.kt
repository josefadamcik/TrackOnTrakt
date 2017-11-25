
package cz.josefadamcik.trackontrakt.util

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.style.ReplacementSpan


class RoundedBackgroundSpan(
    val config: Config
) : ReplacementSpan() {

    data class Config(
        val backgroundColor: Int,
        val textColor: Int,
        val horizontalInnerPad: Int = 16,
        val verticalInnerPad: Int = 8,
        val verticalOuterPad: Int = 8,
        val cornerRadius: Int = 8
    )

    override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
//        Timber.d("draw start $start end $end x $x top $top y $y bottom $bottom")
        val rect = RectF(x, (top + config.verticalOuterPad).toFloat(), x + measureText(paint, text, start, end) + 2 * config.horizontalInnerPad, (bottom - config.verticalOuterPad).toFloat())
        paint.color = config.backgroundColor
        canvas.drawRoundRect(rect, config.cornerRadius.toFloat(), config.cornerRadius.toFloat(), paint)
        paint.color = config.textColor
        canvas.drawText(text, start, end, x + config.horizontalInnerPad, y.toFloat(), paint)
    }

    override fun getSize(paint: Paint, text: CharSequence, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
//        Timber.d("getSize %s", fm)
        val growHeight = config.verticalInnerPad + config.verticalOuterPad
        if (fm != null) {
            fm.top -= growHeight
            fm.ascent -= growHeight
            fm.bottom += growHeight
            fm.descent += growHeight
            fm.leading = growHeight
        }
//        Timber.d("getSize %s", fm)
        return config.horizontalInnerPad * 2 + Math.round(paint.measureText(text, start, end))
    }



    private fun measureText(paint: Paint, text: CharSequence, start: Int, end: Int): Float {
        return paint.measureText(text, start, end)
    }


}


