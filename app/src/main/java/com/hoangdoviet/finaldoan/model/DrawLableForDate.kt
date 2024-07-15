package com.hoangdoviet.finaldoan.model

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.LineBackgroundSpan


class DrawLableForDate(private val color: Int, private val value: String) : LineBackgroundSpan {
    override fun drawBackground(canvas: Canvas, paint: Paint,
                                left: Int, right: Int, top: Int,
                                baseline: Int, bottom: Int,
                                charSequence: CharSequence,
                                start: Int, end: Int, lineNum: Int) {
        val oldColor = paint.color
        val oldTextSize = paint.textSize
        if (color != 0) {
            paint.setColor(color)
        }
        if (value != "") {
            paint.setTextSize(35f)
        }
        val text = value
        val x = right / 2f
        val y = bottom * 1.25f
        canvas.drawText(text, x, y, paint)
        paint.textSize = oldTextSize
        paint.color = oldColor
    }
}
