package com.hoangdoviet.finaldoan.model

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.LineBackgroundSpan

class TopRightDotSpan(private val radius: Float, private val color: Int) : LineBackgroundSpan {
    override fun drawBackground(
        c: Canvas, p: Paint,
        left: Int, right: Int,
        top: Int, baseline: Int,
        bottom: Int, text: CharSequence,
        start: Int, end: Int, lnum: Int
    ) {
        val oldColor = p.color
        if (color != 0) {
            p.color = color
        }
        c.drawCircle(
            (right - radius - 2).toFloat(),  // Adjust position to right
            (top + radius + 2).toFloat(),    // Adjust position to top
            radius, p
        )
        p.color = oldColor
    }
}
