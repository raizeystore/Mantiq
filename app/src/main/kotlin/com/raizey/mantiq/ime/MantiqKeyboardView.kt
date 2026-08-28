package com.raizey.mantiq.ime

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MantiqKeyboardView(
    context: Context,
    private val listener: Listener,
) : LinearLayout(context) {
    interface Listener {
        fun onText(text: String)
        fun onSpace()
        fun onBackspace()
        fun onEnter()
        fun onAiRequested()
    }

    private var arabic = true

    init {
        orientation = VERTICAL
        setPadding(4.dp, 6.dp, 4.dp, 8.dp)
        setBackgroundColor(BACKGROUND)
        render()
    }

    private fun render() {
        removeAllViews()
        addToolbar()
        val rows = if (arabic) ARABIC_ROWS else ENGLISH_ROWS
        rows.forEach { addKeyRow(it, if (arabic) LAYOUT_DIRECTION_RTL else LAYOUT_DIRECTION_LTR) }
        addBottomRow()
    }

    private fun addToolbar() {
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutDirection = LAYOUT_DIRECTION_LTR
        }
        row.addView(TextView(context).apply {
            text = "Mantiq"
            textSize = 16f
            setTextColor(ACCENT)
            setPadding(10.dp, 0, 0, 0)
        }, LayoutParams(0, 40.dp, 1f))
        row.addView(keyButton("AI") { listener.onAiRequested() }, LayoutParams(64.dp, 40.dp))
        addView(row, LayoutParams(LayoutParams.MATCH_PARENT, 44.dp))
    }

    private fun addKeyRow(keys: List<String>, direction: Int) {
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
            layoutDirection = direction
        }
        keys.forEach { key ->
            row.addView(keyButton(key) { listener.onText(key) }, weightedKey())
        }
        addView(row, LayoutParams(LayoutParams.MATCH_PARENT, 50.dp))
    }

    private fun addBottomRow() {
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
            layoutDirection = LAYOUT_DIRECTION_LTR
        }
        row.addView(keyButton("🌐") {
            arabic = !arabic
            render()
        }, weightedKey(1.1f))
        row.addView(keyButton(if (arabic) "،" else ",") { listener.onText(if (arabic) "،" else ",") }, weightedKey())
        row.addView(keyButton(" ") { listener.onSpace() }, weightedKey(3.5f))
        row.addView(keyButton(".") { listener.onText(".") }, weightedKey())
        row.addView(keyButton("⌫") { listener.onBackspace() }, weightedKey(1.1f))
        row.addView(keyButton("⏎") { listener.onEnter() }, weightedKey(1.1f))
        addView(row, LayoutParams(LayoutParams.MATCH_PARENT, 54.dp))
    }

    private fun keyButton(label: String, action: () -> Unit) = Button(context).apply {
        text = label
        textSize = if (label == "AI") 14f else 18f
        isAllCaps = false
        setTextColor(Color.WHITE)
        gravity = Gravity.CENTER
        minWidth = 0
        minimumWidth = 0
        minHeight = 0
        minimumHeight = 0
        setPadding(0, 0, 0, 0)
        background = GradientDrawable().apply {
            setColor(KEY_BACKGROUND)
            cornerRadius = 10.dp.toFloat()
            setStroke(1.dp, KEY_BORDER)
        }
        setOnClickListener { action() }
    }

    private fun weightedKey(weight: Float = 1f) = LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, weight).apply {
        setMargins(2.dp, 2.dp, 2.dp, 2.dp)
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()

    private companion object {
        val BACKGROUND = Color.rgb(10, 14, 19)
        val KEY_BACKGROUND = Color.rgb(27, 35, 45)
        val KEY_BORDER = Color.rgb(55, 70, 82)
        val ACCENT = Color.rgb(91, 224, 179)

        val ARABIC_ROWS = listOf(
            listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج", "د"),
            listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك", "ط"),
            listOf("ئ", "ء", "ؤ", "ر", "لا", "ى", "ة", "و", "ز", "ظ"),
        )
        val ENGLISH_ROWS = listOf(
            listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
            listOf("z", "x", "c", "v", "b", "n", "m"),
        )
    }
}

