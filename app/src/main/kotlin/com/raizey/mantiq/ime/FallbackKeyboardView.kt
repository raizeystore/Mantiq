package com.raizey.mantiq.ime

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/** Minimal keyboard used only when the full keyboard view cannot be created. */
class FallbackKeyboardView(
    context: Context,
    private val listener: MantiqKeyboardView.Listener,
) : LinearLayout(context) {
    init {
        orientation = VERTICAL
        setPadding(6.dp, 6.dp, 6.dp, 8.dp)
        setBackgroundColor(Color.rgb(10, 14, 19))

        addView(TextView(context).apply {
            text = "Mantiq — الوضع الآمن"
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(91, 224, 179))
            textSize = 14f
        }, LayoutParams(LayoutParams.MATCH_PARENT, 36.dp))

        KeyboardLayouts.ARABIC_ROWS.forEach(::addCharacterRow)

        val controls = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
        }
        controls.addView(key("،") { listener.onText("،") }, weight())
        controls.addView(key("مسافة") { listener.onSpace() }, weight(3f))
        controls.addView(key("⌫") { listener.onBackspace() }, weight())
        controls.addView(key("⏎") { listener.onEnter() }, weight())
        addView(controls, LayoutParams(LayoutParams.MATCH_PARENT, 52.dp))
    }

    private fun addCharacterRow(characters: List<String>) {
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
            layoutDirection = LAYOUT_DIRECTION_LTR
        }
        characters.forEach { character ->
            row.addView(key(character) { listener.onText(character) }, weight())
        }
        addView(row, LayoutParams(LayoutParams.MATCH_PARENT, 50.dp))
    }

    private fun key(label: String, action: () -> Unit) = Button(context).apply {
        text = label
        textSize = if (label == "مسافة") 13f else 17f
        isAllCaps = false
        setTextColor(Color.WHITE)
        minWidth = 0
        minimumWidth = 0
        minHeight = 0
        minimumHeight = 0
        setPadding(0, 0, 0, 0)
        setBackgroundColor(Color.rgb(27, 35, 45))
        setOnClickListener { action() }
    }

    private fun weight(value: Float = 1f) =
        LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, value).apply {
            setMargins(2.dp, 2.dp, 2.dp, 2.dp)
        }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}
