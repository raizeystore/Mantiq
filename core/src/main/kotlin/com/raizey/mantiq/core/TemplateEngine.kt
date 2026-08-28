package com.raizey.mantiq.core

import java.time.Clock
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class TemplateContext(
    val clock: Clock = Clock.systemUTC(),
    val zoneId: ZoneId = ZoneId.systemDefault(),
    val locale: Locale = Locale.getDefault(),
    val timePattern: String = "h:mm a",
    val datePattern: String = "dd/MM/yyyy",
    val dateTimePattern: String = "dd/MM/yyyy h:mm a",
)

class TemplateEngine {
    private val variable = Regex("""\{\{(time|date|datetime)([+-][^}]*)?}}""")

    fun render(template: String, context: TemplateContext = TemplateContext()): String =
        variable.replace(template) { match ->
            val variableName = match.groupValues[1]
            val operation = match.groupValues[2]
            val defaultUnit = if (variableName == "date") {
                DefaultTemporalUnit.DAYS
            } else {
                DefaultTemporalUnit.HOURS
            }

            var value = ZonedDateTime.now(context.clock).withZoneSameInstant(context.zoneId)
            if (operation.isNotEmpty()) {
                val sign = operation.first()
                val duration = TemporalOffsetParser.parse(operation.drop(1), defaultUnit)
                value = if (sign == '+') value.plus(duration) else value.minus(duration)
            }

            val pattern = when (variableName) {
                "time" -> context.timePattern
                "date" -> context.datePattern
                else -> context.dateTimePattern
            }
            DateTimeFormatter.ofPattern(pattern, context.locale).format(value)
        }
}

