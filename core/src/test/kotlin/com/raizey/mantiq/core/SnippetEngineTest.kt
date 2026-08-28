package com.raizey.mantiq.core

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SnippetEngineTest {
    private val context = TemplateContext(
        clock = Clock.fixed(Instant.parse("2026-08-28T15:00:00Z"), ZoneOffset.UTC),
        zoneId = ZoneOffset.UTC,
        locale = Locale.ENGLISH,
        timePattern = "HH:mm",
    )

    private val engine = SnippetEngine(
        listOf(
            Snippet("!بعد1.5", "{{time+1.5h}}"),
            Snippet("@@1", "personal@example.com"),
        ),
    )

    @Test
    fun expandsOnlyTheTrailingTrigger() {
        val expansion = engine.expandBeforeDelimiter("موعدنا !بعد1.5", context)

        assertEquals("!بعد1.5", expansion?.trigger)
        assertEquals("!بعد1.5".length, expansion?.deleteCharacters)
        assertEquals("16:30", expansion?.replacement)
    }

    @Test
    fun supportsSequentialSymbolTriggers() {
        assertEquals("personal@example.com", engine.expandTrigger("@@1", context))
    }

    @Test
    fun leavesUnknownTextUntouched() {
        assertNull(engine.expandBeforeDelimiter("نص عادي", context))
    }
}
