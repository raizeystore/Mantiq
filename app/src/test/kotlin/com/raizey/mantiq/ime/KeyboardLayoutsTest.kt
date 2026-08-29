package com.raizey.mantiq.ime

import org.junit.Assert.assertEquals
import org.junit.Test

class KeyboardLayoutsTest {
    @Test
    fun arabicRowsMatchTheStandardVisualOrderFromLeftToRight() {
        assertEquals(
            listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج", "د"),
            KeyboardLayouts.ARABIC_ROWS[0],
        )
        assertEquals(
            listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك", "ط"),
            KeyboardLayouts.ARABIC_ROWS[1],
        )
        assertEquals(
            listOf("ئ", "ء", "ؤ", "ر", "لا", "ى", "ة", "و", "ز", "ظ"),
            KeyboardLayouts.ARABIC_ROWS[2],
        )
    }

    @Test
    fun englishRowsUseQwertyOrder() {
        assertEquals(listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"), KeyboardLayouts.ENGLISH_ROWS[0])
        assertEquals(listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"), KeyboardLayouts.ENGLISH_ROWS[1])
        assertEquals(listOf("z", "x", "c", "v", "b", "n", "m"), KeyboardLayouts.ENGLISH_ROWS[2])
    }
}
