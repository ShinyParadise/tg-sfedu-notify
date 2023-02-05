package dto

import kotlinx.datetime.LocalDateTime

data class CalendarEvent(
    val name: String,
    val begin: LocalDateTime,
    val end: LocalDateTime,
)