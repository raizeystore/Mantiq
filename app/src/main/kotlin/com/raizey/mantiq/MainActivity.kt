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
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var statusView: TextView
    private lateinit var testField: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildContent())
    }

    override fun onResume() {
        super.onResume()
        if (::statusView.isInitialized) updateKeyboardStatus()
    }

    private fun buildContent(): LinearLayout {
        val padding = 24.dp
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(padding, 48.dp, padding, padding)
            setBackgroundColor(Color.rgb(10, 14, 19))

            addView(ImageView(context).apply {
                setImageResource(R.mipmap.ic_launcher)
                contentDescription = getString(R.string.app_name)
            }, LinearLayout.LayoutParams(96.dp, 96.dp).apply {
                bottomMargin = 16.dp
            })

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

            statusView = TextView(context).apply {
                textSize = 16f
                gravity = Gravity.CENTER
                setPadding(8.dp, 12.dp, 8.dp, 20.dp)
            }
            addView(statusView, matchWrap())

            testField = EditText(context).apply {
                hint = getString(R.string.test_keyboard)
                textSize = 18f
                setTextColor(Color.WHITE)
                setHintTextColor(Color.rgb(155, 165, 175))
                setBackgroundColor(Color.rgb(27, 35, 45))
                setPadding(16.dp, 14.dp, 16.dp, 14.dp)
                gravity = Gravity.START
                isSingleLine = false
                minLines = 2
            }
            addView(testField, matchWrap())

            addView(actionButton(R.string.open_keyboard) {
                openKeyboardForTesting()
            }, matchWrap())

            updateKeyboardStatus()
        }
    }

    private fun updateKeyboardStatus() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val enabled = inputManager.enabledInputMethodList.any {
            it.packageName == packageName && it.serviceName == MANTIQ_SERVICE
        }
        val selected = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD,
        ).orEmpty().contains(packageName)

        val (message, color) = when {
            enabled && selected -> R.string.status_selected to Color.rgb(91, 224, 179)
            enabled -> R.string.status_enabled to Color.rgb(255, 193, 7)
            else -> R.string.status_disabled to Color.rgb(255, 107, 107)
        }
        statusView.setText(message)
        statusView.setTextColor(color)
    }

    private fun openKeyboardForTesting() {
        testField.requestFocus()
        testField.post {
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .showSoftInput(testField, InputMethodManager.SHOW_IMPLICIT)
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

    private companion object {
        const val MANTIQ_SERVICE = "com.raizey.mantiq.ime.MantiqImeService"
    }
}
