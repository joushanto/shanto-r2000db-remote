package com.shanto.r2000dbremote

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import kotlin.math.min

/** Code-drawn dots keep the interface sharp without a downloaded font or bitmap. */
class DotGridLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {
    private val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(58, 65, 72) }
    init { setWillNotDraw(false) }
    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.rgb(26, 31, 37))
        val step = 18f * resources.displayMetrics.density
        var y = step / 2
        while (y < height) {
            var x = step / 2
            while (x < width) { canvas.drawCircle(x, y, 0.65f * resources.displayMetrics.density, dot); x += step }
            y += step
        }
    }
}

class DotTitle @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : AppCompatTextView(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    override fun onDraw(canvas: Canvas) {
        val label = text.toString().uppercase(java.util.Locale.ROOT)
        if (label.isEmpty()) return
        val columns = label.length * 6 - 1
        val cell = min((width - paddingLeft - paddingRight).toFloat() / columns,
            (height - paddingTop - paddingBottom).toFloat() / 7)
        paint.color = currentTextColor
        val top = (height - 7 * cell) / 2f
        for ((index, char) in label.withIndex()) {
            val rows = glyphs[char] ?: glyphs.getValue(' ')
            for (y in rows.indices) for (x in 0..4) {
                if (rows[y][x] == '1') canvas.drawCircle(paddingLeft + (index * 6 + x + 0.5f) * cell,
                    top + (y + 0.5f) * cell, cell * 0.32f, paint)
            }
        }
    }
    companion object {
        private val glyphs = mapOf(
            'R' to listOf("11110","10001","10001","11110","10100","10010","10001"),
            'D' to listOf("11110","10001","10001","10001","10001","10001","11110"),
            'B' to listOf("11110","10001","10001","11110","10001","10001","11110"),
            '2' to listOf("01110","10001","00001","00110","01000","10000","11111"),
            '0' to listOf("01110","10001","10011","10101","11001","10001","01110"),
            ' ' to List(7) { "00000" }
        )
    }
}

/** Native button semantics, circular ripple and dotted icons. Text is retained for TalkBack. */
class RemoteCircle @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : AppCompatButton(context, attrs) {
    private val ink = Paint(Paint.ANTI_ALIAS_FLAG)
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val key = tag?.toString() ?: "plus"
        val light = key == "plus" || key == "minus"
            backgroundTintList = null
            val fill = when { key == "power" -> Color.rgb(224, 47, 51); light -> Color.rgb(237, 238, 232); else -> Color.rgb(42, 48, 55) }
            val shape = GradientDrawable().apply {
                this.shape = GradientDrawable.OVAL
                setColor(fill)
                if (!light && key != "power") setStroke((resources.displayMetrics.density).toInt().coerceAtLeast(1), Color.rgb(67, 73, 81))
            }
            val mask = GradientDrawable().apply { this.shape = GradientDrawable.OVAL; setColor(Color.WHITE) }
            background = RippleDrawable(ColorStateList.valueOf(if (light) 0x22000000 else 0x33FFFFFF), shape, mask)
    }
    override fun onDraw(canvas: Canvas) {
        val key = tag?.toString() ?: "plus"
        val light = key == "plus" || key == "minus"
        ink.color = if (light) Color.rgb(29, 34, 40) else Color.rgb(241, 241, 235)
        ink.alpha = if (isEnabled) 255 else 80
        val icon = icons[key] ?: icons.getValue("plus")
        val cell = min(width, height) * (if (light) 0.057f else 0.052f)
        val left = (width - 9 * cell) / 2
        val top = (height - 9 * cell) / 2
        for (y in 0..8) for (x in 0..8) if (icon[y][x] == '1') {
            canvas.drawCircle(left + (x + 0.5f) * cell, top + (y + 0.5f) * cell, cell * 0.34f, ink)
        }
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isEnabled) when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> animate().scaleX(0.94f).scaleY(0.94f).setDuration(90).start()
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> animate().scaleX(1f).scaleY(1f).setDuration(160).start()
        }
        return super.onTouchEvent(event)
    }
    // Preserve the standard button's keyboard, TalkBack and touch click dispatch.
    override fun performClick(): Boolean = super.performClick()
    companion object {
        private fun rows(value: String) = value.split('/')
        private val icons = mapOf(
            "power" to rows("000010000/000010000/001010100/010010010/100010001/100000001/100000001/010000010/001111100"),
            "mute" to rows("000100000/001100010/111100100/111101000/111110000/111101000/111100100/001100010/000100000"),
            "plus" to rows("000000000/000010000/000010000/000010000/011111110/000010000/000010000/000010000/000000000"),
            "minus" to rows("000000000/000000000/000000000/000000000/011111110/000000000/000000000/000000000/000000000"),
            "line1" to rows("000010000/000110000/001010000/000010000/000010000/000010000/000010000/001111100/000000000"),
            "line2" to rows("001111100/010000010/000000010/000000100/000011000/000100000/001000000/011111110/000000000"),
            "optical" to rows("000010000/000101000/001000100/010010010/100111001/010010010/001000100/000101000/000010000"),
            "bluetooth" to rows("000010000/000011000/010010100/001011000/000110000/001011000/010010100/000011000/000010000"),
            "classic" to rows("000000000/000000000/001000100/001000100/101010101/101010101/101010101/101010101/000000000"),
            "dynamic" to rows("000010000/000010000/001010100/001010100/101010101/101010101/101010101/101010101/000000000")
        )
    }
}
