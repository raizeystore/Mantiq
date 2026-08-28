package com.raizey.mantiq

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildContent())
    }

    private fun buildContent(): LinearLayout {
        val padding = 24.dp
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(padding, 48.dp, padding, padding)
            setBackgroundColor(Color.rgb(10, 14, 19))

            addView(TextView(context).apply {
                text = getString(R.string.welcome_title)
                textSize = 36f
                setTextColor(Color.rgb(91, 224, 179))
                gravity = Gravity.CENTER
            }, matchWrap())

            addView(TextView(context).apply {
                text = getString(R.string.welcome_body)
                textSize = 18f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                setPadding(0, 24.dp, 0, 32.dp)
            }, matchWrap())

            addView(actionButton(R.string.enable_keyboard) {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }, matchWrap())

            addView(actionButton(R.string.choose_keyboard) {
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .showInputMethodPicker()
            }, matchWrap())
        }
    }

    private fun actionButton(label: Int, action: () -> Unit) = Button(this).apply {
        text = getString(label)
        textSize = 16f
        isAllCaps = false
        setOnClickListener { action() }
    }

    private fun matchWrap() = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT,
    ).apply {
        bottomMargin = 12.dp
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}

