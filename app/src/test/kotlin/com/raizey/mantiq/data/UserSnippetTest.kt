package com.raizey.mantiq.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserSnippetTest {
    private val snippet = UserSnippet(
        id = "1",
        trigger = "@@mail",
        template = "me@example.com",
        enabled = true,
        allowedPackages = setOf("com.whatsapp", "com.google.android.gm"),
        createdAt = 0L,
    )

    @Test
    fun worksOnlyInsideExplicitlyAllowedApps() {
        assertTrue(snippet.isAllowedIn("com.whatsapp"))
        assertFalse(snippet.isAllowedIn("org.telegram.messenger"))
        assertFalse(snippet.isAllowedIn(null))
    }

    @Test
    fun emptyAppSelectionDisablesTheSnippetEverywhere() {
        assertFalse(snippet.copy(allowedPackages = emptySet()).isAllowedIn("com.whatsapp"))
    }

    @Test
    fun disabledSnippetNeverRuns() {
        assertFalse(snippet.copy(enabled = false).isAllowedIn("com.whatsapp"))
    }
}
