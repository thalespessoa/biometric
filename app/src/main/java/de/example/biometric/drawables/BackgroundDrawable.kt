package de.example.biometric.drawables

import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt

class BackgroundDrawable : Drawable() {
    @ColorInt
    private val FIRST_COLOR = 0xff00d0be

    @ColorInt
    private val LAST_COLOR = 0xff8f5be1

    private val paint by lazy {
        Paint().apply {
            style = Paint.Style.FILL
        }
    }

    override fun draw(canvas: Canvas?) {
        val rect = RectF(bounds)
        val linearGradient = LinearGradient(
                0f,
                0f,
                bounds.width().toFloat(),
                bounds.height().toFloat(),
                FIRST_COLOR.toInt(),
                LAST_COLOR.toInt(),
                Shader.TileMode.MIRROR)

        paint.shader = linearGradient
        canvas?.drawRect(rect, paint)
    }

    override fun setAlpha(value: Int) {
        paint.alpha = value
    }

    override fun getOpacity(): Int {
        return paint.alpha
    }

    override fun setColorFilter(p0: ColorFilter?) {
    }

}