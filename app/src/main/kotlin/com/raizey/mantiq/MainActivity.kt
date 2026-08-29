package com.raizey.mantiq

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.raizey.mantiq.data.InstalledAppsRepository
import com.raizey.mantiq.data.SecureSnippetRepository
import com.raizey.mantiq.diagnostics.CrashStore
import com.raizey.mantiq.diagnostics.DeviceDiagnostics
import com.raizey.mantiq.ime.MantiqImeService
import com.raizey.mantiq.ui.MantiqApp
import com.raizey.mantiq.ui.MantiqTheme

class MainActivity : ComponentActivity() {
    private var keyboardState by mutableStateOf(KeyboardState(enabled = false, selected = false))

    private val inputManager
        get() = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    private val imeComponent
        get() = ComponentName(this, MantiqImeService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runCatching {
            keyboardState = currentState()
            val snippets = SecureSnippetRepository(this)
            val apps = InstalledAppsRepository(this)
            setContent {
                MantiqTheme {
                    MantiqApp(
                        keyboardState = keyboardState,
                        snippetRepository = snippets,
                        appsRepository = apps,
                        onEnableKeyboard = ::openInputMethodSettings,
                        onChooseKeyboard = ::chooseCurrentKeyboard,
                        onCopyDiagnostics = ::copyDiagnostics,
                    )
                }
            }
        }.onFailure { error ->
            CrashStore.record(this, "MainActivity.onCreate", error)
            Toast.makeText(this, R.string.unexpected_error, Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        keyboardState = currentState()
    }

    private fun currentState(): KeyboardState {
        val enabled = runCatching {
            inputManager.enabledInputMethodList.any {
                it.packageName == packageName && it.serviceName == imeComponent.className
            }
        }.getOrDefault(false)
        val selectedComponent = runCatching {
            ComponentName.unflattenFromString(
                Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD).orEmpty(),
            )
        }.getOrNull()
        return KeyboardState(enabled = enabled, selected = selectedComponent == imeComponent)
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
            .onFailure { openInputMethodSettings() }
    }

    private fun copyDiagnostics() {
        val report = DeviceDiagnostics.create(this, currentState(), imeComponent)
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Mantiq diagnostics", report))
        Toast.makeText(this, R.string.diagnostics_copied, Toast.LENGTH_SHORT).show()
    }

    private fun showError(error: Throwable) {
        CrashStore.record(this, "MainActivity", error)
        Toast.makeText(this, R.string.unexpected_error, Toast.LENGTH_LONG).show()
    }

    data class KeyboardState(val enabled: Boolean, val selected: Boolean)
}
