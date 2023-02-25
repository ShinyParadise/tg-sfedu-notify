package utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

fun buildCalendarFileName(groupName: String): String {
    return "src/main/resources/calendar_$groupName.ics"
}
