package com.raizey.mantiq

import android.app.Application
import com.raizey.mantiq.diagnostics.CrashStore

class MantiqApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val systemHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            CrashStore.record(this, "uncaught:${thread.name}", error)
            systemHandler?.uncaughtException(thread, error)
        }
    }
}
