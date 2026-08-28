package com.raizey.mantiq.ime

import android.inputmethodservice.InputMethodService
import android.content.res.Configuration
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.raizey.mantiq.core.Snippet
import com.raizey.mantiq.core.SnippetEngine
import com.raizey.mantiq.core.TemplateContext
import com.raizey.mantiq.diagnostics.CrashStore
import java.time.Clock
import java.time.ZoneId
import java.util.Locale

class MantiqImeService : InputMethodService(), MantiqKeyboardView.Listener {
    private val snippets = SnippetEngine(
        listOf(
            Snippet("!بعد1.5", "{{time+1.5h}}"),
            Snippet("!بعد4", "{{time+4h}}"),
            Snippet("!تاريخ", "{{date}}"),
            Snippet("!الوقت", "{{time}}"),
        ),
    )

    private val templateContext
        get() = TemplateContext(
            clock = Clock.systemUTC(),
            zoneId = ZoneId.of("Africa/Khartoum"),
            locale = Locale("ar"),
            timePattern = "h:mm a",
            datePattern = "dd/MM/yyyy",
            dateTimePattern = "dd/MM/yyyy h:mm a",
        )

    override fun onCreateInputView(): View = runCatching {
        MantiqKeyboardView(this, this)
    }.getOrElse { error ->
        CrashStore.record(this, "MantiqImeService.onCreateInputView", error)
        FallbackKeyboardView(this, this)
    }

    override fun onEvaluateInputViewShown(): Boolean {
        val systemDecision = super.onEvaluateInputViewShown()
        val configuration = resources.configuration
        val visibleHardwareKeyboard =
            configuration.keyboard != Configuration.KEYBOARD_NOKEYS &&
                configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO
        return systemDecision || !visibleHardwareKeyboard
    }

    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onText(text: String) {
        runCatching { currentInputConnection?.commitText(text, 1) }
            .onFailure { CrashStore.record(this, "MantiqImeService.onText", it) }
    }

    override fun onSpace() {
        val connection = currentInputConnection ?: return
        val sensitive = SensitiveFieldDetector.isSensitive(currentInputEditorInfo?.inputType ?: 0)
        if (!sensitive) {
            val beforeCursor = connection.getTextBeforeCursor(MAX_TRIGGER_LENGTH, 0)?.toString().orEmpty()
            val expansion = snippets.expandBeforeDelimiter(beforeCursor, templateContext)
            if (expansion != null) {
                connection.deleteSurroundingText(expansion.deleteCharacters, 0)
                connection.commitText(expansion.replacement + " ", 1)
                return
            }
        }
        connection.commitText(" ", 1)
    }

    override fun onBackspace() {
        val connection = currentInputConnection ?: return
        val selected = connection.getSelectedText(0)
        if (!selected.isNullOrEmpty()) {
            connection.commitText("", 1)
        } else {
            connection.deleteSurroundingTextInCodePoints(1, 0)
        }
    }

    override fun onEnter() {
        val connection = currentInputConnection ?: return
        val action = currentInputEditorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)
            ?: EditorInfo.IME_ACTION_NONE
        if (action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
            connection.performEditorAction(action)
        } else {
            connection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            connection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        }
    }

    override fun onAiRequested() {
        Toast.makeText(this, "مساعد Mantiq AI سيُضاف بعد اكتمال المحرك الأساسي", Toast.LENGTH_SHORT).show()
    }

    private companion object {
        const val MAX_TRIGGER_LENGTH = 128
    }
}
