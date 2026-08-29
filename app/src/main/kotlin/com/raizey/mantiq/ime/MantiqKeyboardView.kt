package com.raizey.mantiq.ime

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
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
        fun onQuickSnippet(trigger: String)
        fun onAiRequested()
    }

    private enum class Mode { ARABIC, ENGLISH, SYMBOLS }

    private data class Key(
        val label: String,
        val value: String = label,
        val weight: Float = 1f,
        val role: KeyRole = KeyRole.LETTER,
    )

    private enum class KeyRole { LETTER, ACTION, ACCENT, SPACE }

    private var mode = Mode.ARABIC
    private var previousLetterMode = Mode.ARABIC
    private var shifted = false
    private var capsLock = false
    private val palette = KeyboardAppearancePreferences.palette(context)
    private val repeatHandler = Handler(Looper.getMainLooper())
    private var repeatAction: Runnable? = null

    init {
        orientation = VERTICAL
        layoutDirection = LAYOUT_DIRECTION_LTR
        setPadding(4.dp, 4.dp, 4.dp, 6.dp)
        setBackgroundColor(palette.background)
        render()
    }

    override fun onDetachedFromWindow() {
        stopRepeating()
        super.onDetachedFromWindow()
    }

    private fun render() {
        removeAllViews()
        addToolbar()
        when (mode) {
            Mode.ARABIC -> addArabicLayout()
            Mode.ENGLISH -> addEnglishLayout()
            Mode.SYMBOLS -> addSymbolLayout()
        }
        addBottomRow()
    }

    private fun addToolbar() {
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutDirection = LAYOUT_DIRECTION_LTR
            setPadding(4.dp, 0, 4.dp, 0)
        }
        row.addView(TextView(context).apply {
            text = "Mantiq"
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(palette.accent)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(8.dp, 0, 4.dp, 0)
        }, LayoutParams(0, 40.dp, 1f))
        row.addView(toolbarChip("الوقت") { listener.onQuickSnippet("!الوقت") })
        row.addView(toolbarChip("التاريخ") { listener.onQuickSnippet("!تاريخ") })
        row.addView(toolbarChip("AI", accent = true) { listener.onAiRequested() })
        addView(row, LayoutParams(LayoutParams.MATCH_PARENT, 44.dp))
    }

    private fun addArabicLayout() {
        // Explicit left-to-right screen order avoids vendor-specific RTL row reversal.
        addKeyRow(ARABIC_ROW_1)
        addKeyRow(ARABIC_ROW_2, sideInset = 0.42f)
        addKeyRow(ARABIC_ROW_3, sideInset = 0.78f, includeBackspace = true)
    }

    private fun addEnglishLayout() {
        val transform: (String) -> String = { value ->
            if (shifted || capsLock) value.uppercase() else value
        }
        addKeyRow(ENGLISH_ROW_1.map { Key(transform(it.label), transform(it.value)) })
        addKeyRow(ENGLISH_ROW_2.map { Key(transform(it.label), transform(it.value)) }, sideInset = 0.5f)

        val thirdRow = buildList {
            add(Key(if (capsLock) "⇪" else "⇧", weight = 1.35f, role = if (shifted || capsLock) KeyRole.ACCENT else KeyRole.ACTION))
            addAll(ENGLISH_ROW_3.map { Key(transform(it.label), transform(it.value)) })
        }
        addKeyRow(thirdRow, sideInset = 0.06f, includeBackspace = true, shiftKey = true)
    }

    private fun addSymbolLayout() {
        addKeyRow(SYMBOL_ROW_1)
        addKeyRow(SYMBOL_ROW_2, sideInset = 0.18f)
        addKeyRow(SYMBOL_ROW_3, sideInset = 0.62f, includeBackspace = true)
    }

    private fun addKeyRow(
        keys: List<Key>,
        sideInset: Float = 0f,
        includeBackspace: Boolean = false,
        shiftKey: Boolean = false,
    ) {
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
            layoutDirection = LAYOUT_DIRECTION_LTR
        }
        if (sideInset > 0f) row.addView(View(context), weightedLayout(sideInset, margins = false))

        keys.forEachIndexed { index, key ->
            val keyView = keyView(key) {
                if (shiftKey && index == 0) {
                    toggleShift()
                } else {
                    listener.onText(key.value)
                    if (mode == Mode.ENGLISH && shifted && !capsLock) {
                        shifted = false
                        render()
                    }
                }
            }
            if (shiftKey && index == 0) {
                keyView.setOnLongClickListener {
                    feedback(it)
                    capsLock = !capsLock
                    shifted = capsLock
                    render()
                    true
                }
            }
            row.addView(keyView, weightedLayout(key.weight))
        }
        if (includeBackspace) {
            row.addView(
                repeatingKey(Key("⌫", weight = 1.4f, role = KeyRole.ACTION)) { listener.onBackspace() },
                weightedLayout(1.4f),
            )
        }
        if (sideInset > 0f) row.addView(View(context), weightedLayout(sideInset, margins = false))
        addView(row, LayoutParams(LayoutParams.MATCH_PARENT, 52.dp))
    }

    private fun addBottomRow() {
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
            layoutDirection = LAYOUT_DIRECTION_LTR
        }

        val modeLabel = if (mode == Mode.SYMBOLS) "أبج" else "123"
        row.addView(keyView(Key(modeLabel, weight = 1.25f, role = KeyRole.ACTION)) {
            if (mode == Mode.SYMBOLS) {
                mode = previousLetterMode
            } else {
                previousLetterMode = mode
                mode = Mode.SYMBOLS
            }
            shifted = false
            capsLock = false
            render()
        }, weightedLayout(1.25f))

        val languageLabel = when (mode) {
            Mode.ARABIC -> "EN"
            Mode.ENGLISH -> "ع"
            Mode.SYMBOLS -> if (previousLetterMode == Mode.ARABIC) "EN" else "ع"
        }
        row.addView(keyView(Key(languageLabel, weight = 1.15f, role = KeyRole.ACTION)) {
            val next = when (if (mode == Mode.SYMBOLS) previousLetterMode else mode) {
                Mode.ARABIC -> Mode.ENGLISH
                else -> Mode.ARABIC
            }
            previousLetterMode = next
            mode = next
            shifted = false
            capsLock = false
            render()
        }, weightedLayout(1.15f))

        val comma = if (mode == Mode.ARABIC) "،" else ","
        row.addView(keyView(Key(comma, role = KeyRole.ACTION)) { listener.onText(comma) }, weightedLayout())
        row.addView(keyView(Key("مسافة", weight = 4f, role = KeyRole.SPACE)) { listener.onSpace() }, weightedLayout(4f))
        row.addView(keyView(Key(".", role = KeyRole.ACTION)) { listener.onText(".") }, weightedLayout())
        row.addView(keyView(Key("↵", weight = 1.3f, role = KeyRole.ACCENT)) { listener.onEnter() }, weightedLayout(1.3f))
        addView(row, LayoutParams(LayoutParams.MATCH_PARENT, 56.dp))
    }

    private fun toggleShift() {
        shifted = !shifted
        if (!shifted) capsLock = false
        render()
    }

    private fun toolbarChip(label: String, accent: Boolean = false, action: () -> Unit): TextView =
        keyView(
            Key(label, role = if (accent) KeyRole.ACCENT else KeyRole.ACTION),
            textSize = 13f,
            action = action,
        ).apply {
            layoutParams = LayoutParams(if (label == "AI") 54.dp else 66.dp, 38.dp).apply {
                setMargins(2.dp, 2.dp, 2.dp, 2.dp)
            }
        }

    private fun keyView(key: Key, textSize: Float = 18f, action: () -> Unit) = TextView(context).apply {
        text = key.label
        this.textSize = if (key.role == KeyRole.SPACE) 13f else textSize
        setTextColor(if (key.role == KeyRole.ACCENT) palette.accent else Color.WHITE)
        gravity = Gravity.CENTER
        isClickable = true
        isFocusable = true
        isSoundEffectsEnabled = false
        contentDescription = key.label
        background = keyBackground(key.role)
        setOnClickListener {
            feedback(it)
            action()
        }
    }

    private fun repeatingKey(key: Key, action: () -> Unit): TextView = keyView(key, action = action).apply {
        setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    feedback(view)
                    action()
                    startRepeating(action)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    stopRepeating()
                    view.performClick()
                    true
                }
                else -> true
            }
        }
        setOnClickListener { }
    }

    private fun startRepeating(action: () -> Unit) {
        stopRepeating()
        val repeat = object : Runnable {
            override fun run() {
                action()
                repeatHandler.postDelayed(this, REPEAT_INTERVAL_MS)
            }
        }
        repeatAction = repeat
        repeatHandler.postDelayed(repeat, REPEAT_START_DELAY_MS)
    }

    private fun stopRepeating() {
        repeatAction?.let(repeatHandler::removeCallbacks)
        repeatAction = null
    }

    private fun feedback(view: View) {
        if (KeyboardPreferences.hapticsEnabled(context)) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
        if (KeyboardPreferences.soundEnabled(context)) {
            (context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager)
                ?.playSoundEffect(AudioManager.FX_KEY_CLICK)
        }
    }

    private fun keyBackground(role: KeyRole): StateListDrawable {
        val normal = when (role) {
            KeyRole.LETTER, KeyRole.SPACE -> palette.key
            KeyRole.ACTION -> palette.actionKey
            KeyRole.ACCENT -> palette.accentKey
        }
        return StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed), rounded(palette.pressedKey, palette.accent))
            addState(intArrayOf(android.R.attr.state_focused), rounded(normal, palette.accent))
            addState(intArrayOf(), rounded(normal, palette.border))
        }
    }

    private fun rounded(color: Int, stroke: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = 9.dp.toFloat()
        setStroke(1.dp, stroke)
    }

    private fun weightedLayout(weight: Float = 1f, margins: Boolean = true) =
        LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, weight).apply {
            if (margins) setMargins(2.dp, 2.dp, 2.dp, 2.dp)
        }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()

    private companion object {
        const val REPEAT_START_DELAY_MS = 420L
        const val REPEAT_INTERVAL_MS = 55L

        // Exact visual order from the left edge to the right edge.
        val ARABIC_ROW_1 = KeyboardLayouts.ARABIC_ROWS[0].map { Key(it) }
        val ARABIC_ROW_2 = KeyboardLayouts.ARABIC_ROWS[1].map { Key(it) }
        val ARABIC_ROW_3 = KeyboardLayouts.ARABIC_ROWS[2].map { Key(it) }

        val ENGLISH_ROW_1 = KeyboardLayouts.ENGLISH_ROWS[0].map { Key(it) }
        val ENGLISH_ROW_2 = KeyboardLayouts.ENGLISH_ROWS[1].map { Key(it) }
        val ENGLISH_ROW_3 = KeyboardLayouts.ENGLISH_ROWS[2].map { Key(it) }

        val SYMBOL_ROW_1 = "1234567890".map { Key(it.toString()) }
        val SYMBOL_ROW_2 = listOf("@", "#", "$", "_", "&", "-", "+", "(", ")", "/").map { Key(it) }
        val SYMBOL_ROW_3 = listOf("*", "\"", "'", ";", ":", "!", "؟", "?").map { Key(it) }
    }
}
