package com.raizey.mantiq.ime

/**
 * Keyboard rows in exact visual order from the left edge to the right edge,
 * matching the familiar Arabic PC/Gboard mapping.
 */
object KeyboardLayouts {
    val ARABIC_ROWS = listOf(
        listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج", "د"),
        listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك", "ط"),
        listOf("ئ", "ء", "ؤ", "ر", "لا", "ى", "ة", "و", "ز", "ظ"),
    )

    val ENGLISH_ROWS = listOf(
        "qwertyuiop".map { it.toString() },
        "asdfghjkl".map { it.toString() },
        "zxcvbnm".map { it.toString() },
    )
}
