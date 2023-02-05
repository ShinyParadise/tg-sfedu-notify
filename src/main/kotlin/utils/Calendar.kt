package utils

import dto.CalendarEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.toLocalDateTime
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.Calendar
import java.io.FileInputStream

suspend fun extractCalendar(outputFileName: String): Calendar {
    val calendar = withContext(Dispatchers.IO) {
        val calendarFile = FileInputStream(outputFileName)
        val builder = CalendarBuilder()
        return@withContext builder.build(calendarFile)
    }

    return calendar
}

fun convertToCalendarEvent(event: Any?): CalendarEvent {
    val strEvent = event.toString()

    val name = strEvent.substringAfter("SUMMARY:").substringBefore("\r\n")
    val begin = strEvent.substringAfter("DTSTART:").substringBefore("\r\n")
    val end = strEvent.substringAfter("DTEND:").substringBefore("\r\n")

    return CalendarEvent(name = name, begin = begin.toLocalDateTime(), end = end.toLocalDateTime())
}

fun buildCalendarFileName(groupName: String): String {
    return "src/main/resources/calendar_$groupName.ics"
}