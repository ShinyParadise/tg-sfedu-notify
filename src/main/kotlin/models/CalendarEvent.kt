package models

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toLocalDateTime

data class CalendarEvent(
    val name: String,
    val begin: LocalDateTime,
    val end: LocalDateTime,
) {
    companion object {
        fun from(event: Any?): CalendarEvent? {
            return try {
                val strEvent = event.toString()

                val name = strEvent.substringAfter("SUMMARY:").substringBefore("\r\n")
                val begin = strEvent.substringAfter("DTSTART:").substringBefore("\r\n")
                val end = strEvent.substringAfter("DTEND:").substringBefore("\r\n")

                CalendarEvent(name = name, begin = begin.toLocalDateTime(), end = end.toLocalDateTime())
            } catch (_: Exception) {
                null
            }
        }
    }
}
