package models

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

                CalendarEvent(
                    name = name,
                    begin = LocalDateTime.parse(begin, formatter).plusHours(3), // to russian timezone, temporary solution
                    end = LocalDateTime.parse(end, formatter).plusHours(3)
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        private val formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
    }

    override fun toString(): String {
        return "Пара: ${this.name}\nВремя: от ${this.begin.hour}:${this.begin.minute} до ${this.end.hour}:${this.end.minute}"
    }
}
