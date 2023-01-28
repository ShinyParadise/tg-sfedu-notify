import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.telegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitText
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import models.CalendarEvent
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.Calendar
import utils.buildCalendarFileName
import utils.buildRequestURL
import utils.downloadFile
import utils.extractCalendar
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
            val group = waitForGroup(it)

            updateUserGroup(it.chat.id.toString(), group)
            SendTextMessage(
                it.chat.id,
                "Теперь твоя группа - $group."
            )
        }

        onCommand("today_schedule", requireOnlyCommandInMessage = true) {
            val userId = it.chat.id.toString()

            if (isUserWithAGroup(userId)) {
                val user = users.find { user -> user.id == userId }!!
                val requestURL = buildRequestURL(user)
                val outputFileName = buildCalendarFileName(user.group)

                if (!File(outputFileName).exists()) {
                    downloadFile(url = requestURL, outputFileName = outputFileName)
                }

                val calendar = extractCalendar(outputFileName)
                val events = calendar.components
                val parsed = events.map { event -> convertToCalendarEvent(event) }

            } else {
                SendTextMessage(
                    it.chat.id,
                    "Ты не ввел свою группу, дурак. Используй команду /setgroup"
                )
            }
        }

    }.join()
}

private fun convertToCalendarEvent(event: Any?): CalendarEvent {
    val begin = event.toString()
    val end = event.toString()
    val name = event.toString()

    return CalendarEvent(name, begin, end)
}

private suspend fun BehaviourContext.waitForGroup(it: CommonMessage<TextContent>) =
    waitText(
        SendTextMessage(
            it.chat.id,
            "Введи свою группу как в зачетке."
        )
    ).first().text

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
