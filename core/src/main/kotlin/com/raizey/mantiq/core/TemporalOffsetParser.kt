package com.raizey.mantiq.core

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Duration

enum class DefaultTemporalUnit(val seconds: Long) {
    MINUTES(60),
    HOURS(3_600),
    DAYS(86_400),
}

/**
 * Parses friendly durations such as 1.5h, 1h30m, 01:30, or a bare 1.5.
 * Bare values use the variable's default unit: hours for time and days for date.
 */
object TemporalOffsetParser {
    private val token = Regex("""(\d+(?:\.\d+)?)(min|m|h|d|w)""")
    private val bareNumber = Regex("""\d+(?:\.\d+)?""")
    private val clockDuration = Regex("""(\d{1,4}):(\d{2})""")

    fun parse(
        expression: String,
        defaultUnit: DefaultTemporalUnit = DefaultTemporalUnit.HOURS,
    ): Duration {
        val value = expression.trim()
        require(value.isNotEmpty()) { "Temporal offset cannot be empty" }

        clockDuration.matchEntire(value)?.let { match ->
            val hours = match.groupValues[1].toLong()
            val minutes = match.groupValues[2].toLong()
            require(minutes in 0..59) { "Minutes must be between 00 and 59" }
            return Duration.ofHours(hours).plusMinutes(minutes)
        }

        if (bareNumber.matches(value)) {
            return decimalDuration(value, defaultUnit.seconds)
        }

        var consumed = 0
        var seconds = 0L
        token.findAll(value).forEach { match ->
            require(match.range.first == consumed) { "Invalid temporal offset: $expression" }
            val unitSeconds = when (match.groupValues[2]) {
                "m", "min" -> 60L
                "h" -> 3_600L
                "d" -> 86_400L
                "w" -> 604_800L
                else -> error("Unsupported temporal unit")
            }
            seconds = Math.addExact(seconds, decimalSeconds(match.groupValues[1], unitSeconds))
            consumed = match.range.last + 1
        }

        require(consumed == value.length && consumed > 0) { "Invalid temporal offset: $expression" }
        return Duration.ofSeconds(seconds)
    }

    private fun decimalDuration(value: String, unitSeconds: Long): Duration =
        Duration.ofSeconds(decimalSeconds(value, unitSeconds))

    private fun decimalSeconds(value: String, unitSeconds: Long): Long =
        BigDecimal(value)
            .multiply(BigDecimal.valueOf(unitSeconds))
            .setScale(0, RoundingMode.HALF_UP)
            .longValueExact()
}

