import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.telegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitText
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.Calendar
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels

// Databases are for noobs
private val users = mutableListOf<User>()

suspend fun main() {
    val token = File("src/main/resources/token.txt").readText(Charsets.UTF_8)
    val bot = telegramBot(token)

    bot.buildBehaviourWithLongPolling {
        onCommand("start", requireOnlyCommandInMessage = true) {
            reply(it, "Привет :)")
        }

        onCommand("setgroup", requireOnlyCommandInMessage = true) {
            val group = waitText(
                SendTextMessage(
                    it.chat.id,
                    "Введи свою группу как в зачетке."
                )
            ).first().text

            updateUserGroup(it.chat.id.toString(), group)
        }

        onCommand("today_schedule", requireOnlyCommandInMessage = true) {
            val userId = it.chat.id.toString()

            if (isUserWithAGroup(userId)) {
                val user = users.find { user -> user.id == userId }!!
                val requestURL = URL(DOWNLOAD_CALENDAR_BASE_URL + user.group)

                val outputFileName = buildCalendarFileName(user.group)
                if (!File(outputFileName).exists()) {
                    downloadFile(url = requestURL, outputFileName = outputFileName)
                }

                val calendar = extractCalendar(outputFileName)
                println(calendar.components)
            } else {
                reply(it, "Ты не ввел свою группу, дурак. Используй команду /setgroup")
            }
        }

    }.join()
}

private fun isUserWithAGroup(userId: String): Boolean {
    val existingUser = users.find { it.id == userId }
    existingUser?.let {
        return it.group.isNotBlank()
    }

    return false
}

private fun updateUserGroup(userId: String, group: String) {
    val existingUser = users.find { user -> (user.id == userId) }
    if (existingUser == null) {
        users.add(User(id = userId, group = group))
    } else {
        existingUser.group = group
    }
}

private fun downloadFile(url: URL, outputFileName: String) {
    url.openStream().use {
        Channels.newChannel(it).use { rbc ->
            FileOutputStream(outputFileName).use { fos ->
                fos.channel.transferFrom(rbc, 0, Long.MAX_VALUE)
            }
        }
    }
}

private suspend fun extractCalendar(outputFileName: String): Calendar {
    val calendar = withContext(Dispatchers.IO) {
        val calendarFile = FileInputStream(outputFileName)
        val builder = CalendarBuilder()
        return@withContext builder.build(calendarFile)
    }

    return calendar
}

private fun buildCalendarFileName(groupName: String): String {
    return "src/main/resources/calendar_$groupName.ics"
}