package com.raizey.mantiq.core

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class TemplateEngineTest {
    private val context = TemplateContext(
        clock = Clock.fixed(Instant.parse("2026-08-28T15:00:00Z"), ZoneOffset.UTC),
        zoneId = ZoneOffset.UTC,
        locale = Locale.ENGLISH,
        timePattern = "HH:mm",
        datePattern = "yyyy-MM-dd",
        dateTimePattern = "yyyy-MM-dd HH:mm",
    )

    @Test
    fun timeSupportsFractionalAddition() {
        assertEquals("16:30", TemplateEngine().render("{{time+1.5h}}", context))
        assertEquals("16:30", TemplateEngine().render("{{time+1.5}}", context))
    }

    @Test
    fun timeSupportsSubtractionAndDayRollover() {
        assertEquals("13:30", TemplateEngine().render("{{time-1.5h}}", context))
        assertEquals("2026-08-29 01:30", TemplateEngine().render("{{datetime+10.5h}}", context))
    }

    @Test
    fun dateUsesDaysForBareDecimals() {
        assertEquals("2026-08-30", TemplateEngine().render("{{date+1.5}}", context))
    }
}
