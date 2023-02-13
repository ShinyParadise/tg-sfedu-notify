import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.telegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitText
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dto.CalendarEvent
import dto.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import utils.*
import java.io.File

// Databases are for noobs
private val users = mutableListOf<User>()

suspend fun main() {
    val token = File("src/main/resources/token.txt").readText(Charsets.UTF_8)
    val bot = telegramBot(token)
    readUsers()

    bot.buildBehaviourWithLongPolling {
        onCommand("start", requireOnlyCommandInMessage = true) {
            reply(it, "Привет :)")
        }

        onCommand("setgroup", requireOnlyCommandInMessage = true) {
            val group = waitForGroup(it)

            SendTextMessage(
                it.chat.id,
                "Теперь твоя группа - $group."
            )
            updateUserGroup(it.chat.id.toString(), group)
        }

        onCommand("today_schedule", requireOnlyCommandInMessage = true) {
            sendTodaySchedule(it)
        }

    }.join()
}

private suspend fun readUsers() {
    withContext(Dispatchers.IO) {
        runCatching {
            File("src/main/resources/users.txt").readLines().forEach {
                val (id, group) = it.split(" ")
                users.add(User(id, group))
            }
        }.onSuccess {
            println("Users read successful")
        }.onFailure {
            println("Users read fail")
        }
    }
}

private suspend fun sendTodaySchedule(it: CommonMessage<TextContent>) {
    val userId = it.chat.id.toString()

    if (isUserWithAGroup(userId)) {
        val user = users.find { user -> user.id == userId }!!
        val requestURL = buildRequestURL(user)
        val outputFileName = buildCalendarFileName(user.group)

        if (!File(outputFileName).exists()) {
            downloadFile(url = requestURL, outputFileName = outputFileName)
        }

        val events = parseTodayEvents(outputFileName)

    } else {
        SendTextMessage(
            it.chat.id,
            "Ты не ввел свою группу, дурак. Используй команду /setgroup"
        )
    }
}

private suspend fun parseTodayEvents(outputFileName: String): List<CalendarEvent> {
    val calendar = extractCalendar(outputFileName)
    val events = calendar.components
    val calendarEvents = events.map { event -> convertToCalendarEvent(event) }

    return calendarEvents.filter {
        it.begin.date == LocalDateTime.now()
    }
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
        File("src/main/resources/users.txt").writeText("$userId $group")
        users.add(User(id = userId, group = group))
    } else {
        existingUser.group = group
    }
}
