package com.raizey.mantiq.diagnostics

import android.content.ComponentName
import android.content.Context
import android.os.Build
import com.raizey.mantiq.BuildConfig
import com.raizey.mantiq.MainActivity

object DeviceDiagnostics {
    fun create(
        context: Context,
        state: MainActivity.KeyboardState,
        component: ComponentName,
    ): String = buildString {
        appendBase(context)
        appendLine("ime_component=${component.flattenToString()}")
        appendLine("ime_enabled=${state.enabled}")
        appendLine("ime_selected=${state.selected}")
        appendLastCrash(context)
    }

    fun createSafeMode(context: Context, error: Throwable): String = buildString {
        appendBase(context)
        appendLine("safe_mode=true")
        appendLine("current_error=${error.stackTraceToString().take(4_000)}")
        appendLastCrash(context)
    }

    private fun StringBuilder.appendBase(context: Context) {
        appendLine("Mantiq diagnostics")
        appendLine("app=${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        appendLine("package=${context.packageName}")
        appendLine("device=${Build.MANUFACTURER} ${Build.MODEL}")
        appendLine("android=${Build.VERSION.RELEASE} sdk=${Build.VERSION.SDK_INT}")
    }

    private fun StringBuilder.appendLastCrash(context: Context) {
        CrashStore.lastCrash(context)?.let {
            appendLine("last_crash_begin")
            appendLine(it)
            appendLine("last_crash_end")
        }
    }
}
