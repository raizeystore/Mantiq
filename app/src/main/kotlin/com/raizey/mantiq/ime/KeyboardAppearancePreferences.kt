package com.raizey.mantiq.ime

import android.content.Context
import android.graphics.Color

data class KeyboardPalette(
    val id: String,
    val name: String,
    val background: Int,
    val key: Int,
    val actionKey: Int,
    val accentKey: Int,
    val pressedKey: Int,
    val border: Int,
    val accent: Int,
)

object KeyboardAppearancePreferences {
    private const val FILE_NAME = "mantiq_keyboard_preferences"
    private const val KEY_THEME = "keyboard_theme"

    val palettes = listOf(
        KeyboardPalette("mantiq", "Mantiq", rgb(9, 14, 20), rgb(27, 38, 49), rgb(20, 29, 38), rgb(17, 61, 51), rgb(46, 66, 80), rgb(53, 71, 86), rgb(82, 224, 177)),
        KeyboardPalette("ocean", "Ocean", rgb(6, 18, 28), rgb(16, 48, 67), rgb(12, 36, 52), rgb(9, 66, 84), rgb(26, 82, 106), rgb(42, 89, 111), rgb(80, 205, 255)),
        KeyboardPalette("violet", "Violet", rgb(16, 11, 25), rgb(43, 31, 59), rgb(34, 24, 48), rgb(62, 37, 79), rgb(79, 54, 99), rgb(90, 67, 109), rgb(211, 145, 255)),
        KeyboardPalette("sand", "Sand", rgb(27, 22, 17), rgb(62, 50, 38), rgb(49, 39, 30), rgb(73, 54, 30), rgb(88, 70, 51), rgb(100, 81, 61), rgb(255, 190, 95)),
    )

    fun selectedId(context: Context): String =
        preferences(context).getString(KEY_THEME, palettes.first().id) ?: palettes.first().id

    fun select(context: Context, id: String) {
        if (palettes.none { it.id == id }) return
        preferences(context).edit().putString(KEY_THEME, id).apply()
    }

    fun palette(context: Context): KeyboardPalette =
        palettes.firstOrNull { it.id == selectedId(context) } ?: palettes.first()

    private fun preferences(context: Context) =
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    private fun rgb(red: Int, green: Int, blue: Int) = Color.rgb(red, green, blue)
}
