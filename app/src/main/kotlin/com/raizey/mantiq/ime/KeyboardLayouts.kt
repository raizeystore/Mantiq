package com.raizey.mantiq.ime

/**
 * Keyboard rows in exact visual order from the left edge to the right edge.
 * Arabic typing order is therefore obtained by reading each row right-to-left.
 */
object KeyboardLayouts {
    val ARABIC_ROWS = listOf(
        listOf("د", "ج", "ح", "خ", "ه", "ع", "غ", "ف", "ق", "ث", "ص", "ض"),
        listOf("ط", "ك", "م", "ن", "ت", "ا", "ل", "ب", "ي", "س", "ش"),
        listOf("ظ", "ز", "و", "ة", "ى", "لا", "ر", "ؤ", "ء", "ئ"),
    )

    val ENGLISH_ROWS = listOf(
        "qwertyuiop".map { it.toString() },
        "asdfghjkl".map { it.toString() },
        "zxcvbnm".map { it.toString() },
    )
}
