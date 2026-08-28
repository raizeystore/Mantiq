package com.raizey.mantiq

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.raizey.mantiq.diagnostics.CrashStore
import com.raizey.mantiq.diagnostics.DeviceDiagnostics
import com.raizey.mantiq.ime.MantiqImeService

class MainActivity : Activity() {
    private lateinit var statusView: TextView
    private lateinit var detailView: TextView
    private lateinit var testField: EditText
    private var uiReady = false
    private var keyboardOpenRequested = false

    private val inputManager
        get() = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    private val imeComponent
        get() = ComponentName(this, MantiqImeService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runCatching {
            setContentView(R.layout.activity_main)
            bindViews()
            uiReady = true
            updateKeyboardStatus()
            if (intent.getBooleanExtra(EXTRA_OPEN_TEST_KEYBOARD, false)) {
                window.decorView.postDelayed(
                    { openKeyboardForTesting() },
                    AUTOMATED_TEST_DELAY_MS,
                )
            }
        }.onFailure { error ->
            CrashStore.record(this, "MainActivity.onCreate", error)
            showSafeMode(error)
        }
    }

    override fun onResume() {
        super.onResume()
        if (uiReady) window.decorView.post { updateKeyboardStatus() }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && keyboardOpenRequested && uiReady) scheduleKeyboardOpenAttempts()
    }

    private fun bindViews() {
        statusView = findViewById(R.id.keyboard_status)
        detailView = findViewById(R.id.keyboard_status_detail)
        testField = findViewById(R.id.keyboard_test_field)

        findViewById<Button>(R.id.enable_keyboard_button).setOnClickListener {
            openInputMethodSettings()
        }
        findViewById<Button>(R.id.choose_keyboard_button).setOnClickListener {
            chooseCurrentKeyboard()
        }
        findViewById<Button>(R.id.open_keyboard_button).setOnClickListener {
            openKeyboardForTesting()
        }
        findViewById<Button>(R.id.copy_diagnostics_button).setOnClickListener {
            copyDiagnostics()
        }
    }

    private fun currentState(): KeyboardState {
        val enabled = runCatching {
            inputManager.enabledInputMethodList.any {
                it.packageName == packageName && it.serviceName == imeComponent.className
            }
        }.getOrDefault(false)

        val selectedComponent = runCatching {
            val value = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.DEFAULT_INPUT_METHOD,
            ).orEmpty()
            ComponentName.unflattenFromString(value)
        }.getOrNull()

        return KeyboardState(enabled = enabled, selected = selectedComponent == imeComponent)
    }

    private fun updateKeyboardStatus() {
        runCatching {
            val state = currentState()
            when {
                state.selected -> {
                    statusView.setText(R.string.status_selected)
                    statusView.setTextColor(getColor(R.color.mantiq_accent))
                    detailView.setText(R.string.status_selected_detail)
                }
                state.enabled -> {
                    statusView.setText(R.string.status_enabled)
                    statusView.setTextColor(getColor(R.color.mantiq_warning))
                    detailView.setText(R.string.status_enabled_detail)
                }
                else -> {
                    statusView.setText(R.string.status_disabled)
                    statusView.setTextColor(getColor(R.color.mantiq_error))
                    detailView.setText(R.string.status_disabled_detail)
                }
            }
        }.onFailure { error ->
            CrashStore.record(this, "MainActivity.updateKeyboardStatus", error)
            statusView.setText(R.string.status_unknown)
            statusView.setTextColor(getColor(R.color.mantiq_error))
            detailView.text = error.javaClass.simpleName
        }
    }

    private fun openInputMethodSettings() {
        runCatching { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }
            .recoverCatching { startActivity(Intent(Settings.ACTION_SETTINGS)) }
            .onFailure { showError(it) }
    }

    private fun chooseCurrentKeyboard() {
        if (!currentState().enabled) {
            Toast.makeText(this, R.string.enable_first_message, Toast.LENGTH_LONG).show()
            openInputMethodSettings()
            return
        }
        runCatching { inputManager.showInputMethodPicker() }
            .onFailure { error ->
                CrashStore.record(this, "MainActivity.chooseCurrentKeyboard", error)
                openInputMethodSettings()
            }
    }

    private fun openKeyboardForTesting() {
        if (!currentState().selected) {
            Toast.makeText(this, R.string.choose_first_message, Toast.LENGTH_LONG).show()
            chooseCurrentKeyboard()
            return
        }

        testField.requestFocus()
        keyboardOpenRequested = true
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        scheduleKeyboardOpenAttempts()
    }

    private fun scheduleKeyboardOpenAttempts() {
        KEYBOARD_OPEN_RETRY_DELAYS_MS.forEach { delay ->
            testField.postDelayed({
                if (!keyboardOpenRequested || isFinishing) return@postDelayed
                testField.requestFocus()
                inputManager.restartInput(testField)
                if (inputManager.showSoftInput(testField, InputMethodManager.SHOW_IMPLICIT)) {
                    keyboardOpenRequested = false
                }
            }, delay)
        }
    }

    private fun copyDiagnostics() {
        val report = DeviceDiagnostics.create(this, currentState(), imeComponent)
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Mantiq diagnostics", report))
        Toast.makeText(this, R.string.diagnostics_copied, Toast.LENGTH_SHORT).show()
    }

    private fun showSafeMode(error: Throwable) {
        val padding = (20 * resources.displayMetrics.density).toInt()
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(padding, padding, padding, padding)
            setBackgroundColor(Color.rgb(10, 14, 19))
        }
        container.addView(TextView(this).apply {
            setText(R.string.safe_mode_title)
            textSize = 24f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        })
        container.addView(TextView(this).apply {
            text = getString(R.string.safe_mode_body, error.javaClass.simpleName)
            textSize = 16f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, padding, 0, padding)
        })
        container.addView(Button(this).apply {
            setText(R.string.enable_keyboard)
            isAllCaps = false
            setOnClickListener { openInputMethodSettings() }
        })
        container.addView(Button(this).apply {
            setText(R.string.copy_diagnostics)
            isAllCaps = false
            setOnClickListener { copySafeModeDiagnostics(error) }
        })
        setContentView(container)
    }

    private fun copySafeModeDiagnostics(error: Throwable) {
        val report = DeviceDiagnostics.createSafeMode(this, error)
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Mantiq diagnostics", report))
        Toast.makeText(this, R.string.diagnostics_copied, Toast.LENGTH_SHORT).show()
    }

    private fun showError(error: Throwable) {
        CrashStore.record(this, "MainActivity", error)
        Toast.makeText(this, R.string.unexpected_error, Toast.LENGTH_LONG).show()
    }

    data class KeyboardState(val enabled: Boolean, val selected: Boolean)

    private companion object {
        const val AUTOMATED_TEST_DELAY_MS = 600L
        const val EXTRA_OPEN_TEST_KEYBOARD = "open_test_keyboard"
        val KEYBOARD_OPEN_RETRY_DELAYS_MS = longArrayOf(100L, 400L, 900L)
    }
}
