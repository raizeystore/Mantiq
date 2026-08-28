package com.raizey.mantiq.core

import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TemporalOffsetParserTest {
    @Test
    fun decimalHoursAreConvertedExactly() {
        assertEquals(Duration.ofMinutes(90), TemporalOffsetParser.parse("1.5h"))
        assertEquals(Duration.ofMinutes(75), TemporalOffsetParser.parse("1.25h"))
        assertEquals(Duration.ofMinutes(165), TemporalOffsetParser.parse("2.75h"))
    }

    @Test
    fun bareDecimalUsesTheSelectedDefaultUnit() {
        assertEquals(Duration.ofMinutes(90), TemporalOffsetParser.parse("1.5"))
        assertEquals(
            Duration.ofHours(36),
            TemporalOffsetParser.parse("1.5", DefaultTemporalUnit.DAYS),
        )
    }

    @Test
    fun compoundAndClockDurationsAreSupported() {
        assertEquals(Duration.ofMinutes(90), TemporalOffsetParser.parse("1h30m"))
        assertEquals(Duration.ofMinutes(90), TemporalOffsetParser.parse("01:30"))
    }

    @Test
    fun invalidOffsetsAreRejected() {
        assertFailsWith<IllegalArgumentException> { TemporalOffsetParser.parse("1.5months") }
        assertFailsWith<IllegalArgumentException> { TemporalOffsetParser.parse("01:90") }
    }
}

