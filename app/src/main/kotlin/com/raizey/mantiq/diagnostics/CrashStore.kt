package com.raizey.mantiq.diagnostics

import android.content.Context
import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter
import java.time.Instant

object CrashStore {
    private const val TAG = "Mantiq"
    private const val PREFERENCES = "mantiq_diagnostics"
    private const val LAST_CRASH = "last_crash"
    private const val MAX_REPORT_LENGTH = 8_000

    fun record(context: Context, source: String, error: Throwable) {
        Log.e(TAG, source, error)
        runCatching {
            val trace = StringWriter().also { writer ->
                error.printStackTrace(PrintWriter(writer))
            }.toString()
            val report = buildString {
                appendLine("time=${Instant.now()}")
                appendLine("source=$source")
                append(trace)
            }.take(MAX_REPORT_LENGTH)
            context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
                .edit()
                .putString(LAST_CRASH, report)
                .apply()
        }
    }

    fun lastCrash(context: Context): String? = runCatching {
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .getString(LAST_CRASH, null)
    }.getOrNull()
}
