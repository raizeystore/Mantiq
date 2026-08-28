package com.raizey.mantiq.ime

import android.content.Context

object KeyboardPreferences {
    private const val FILE_NAME = "mantiq_keyboard_preferences"
    private const val KEY_HAPTICS = "haptics_enabled"
    private const val KEY_SOUND = "sound_enabled"

    fun hapticsEnabled(context: Context): Boolean =
        preferences(context).getBoolean(KEY_HAPTICS, true)

    fun setHapticsEnabled(context: Context, enabled: Boolean) {
        preferences(context).edit().putBoolean(KEY_HAPTICS, enabled).apply()
    }

    fun soundEnabled(context: Context): Boolean =
        preferences(context).getBoolean(KEY_SOUND, false)

    fun setSoundEnabled(context: Context, enabled: Boolean) {
        preferences(context).edit().putBoolean(KEY_SOUND, enabled).apply()
    }

    private fun preferences(context: Context) =
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
}
