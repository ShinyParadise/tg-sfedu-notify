import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.telegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitText
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import models.CalendarEvent
import models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import models.UserContainer
import models.UserContainerImpl
import utils.*
import java.io.File
import java.time.LocalDateTime

// Databases are for noobs
private val users: UserContainer = UserContainerImpl()

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
            users.updateUserGroup(it.chat.id.toString(), group)
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
                users.addUsers(User(id, group))
            }
        }.onSuccess {
            println("Users read successful")
        }.onFailure {
            println("Users read fail")
        }
    }
}

private suspend fun sendTodaySchedule(messageContext: CommonMessage<TextContent>) {
    val userId = messageContext.chat.id.toString()

    if (users.isUserWithAGroup(userId)) {
        val user = users.findUser(userId)!!

        val requestURL = buildRequestURL(user)
        val outputFileName = buildCalendarFileName(user.group)

        if (!File(outputFileName).exists()) {
            downloadFile(url = requestURL, outputFileName = outputFileName)
        }

        var events: List<CalendarEvent> = emptyList()

        runCatching {
            events = parseTodayEvents(outputFileName)
        }.onFailure {
            SendTextMessage(messageContext.chat.id, "Произошла ошибка")
        }.onSuccess {
            val message = events.joinToString("\n\n")
            SendTextMessage(messageContext.chat.id, message)
        }
    } else {
        SendTextMessage(
            messageContext.chat.id,
            "Ты не ввел свою группу, дурак. Используй команду /setgroup"
        )
    }
}

private suspend fun parseTodayEvents(outputFileName: String): List<CalendarEvent> {
    val calendar = extractCalendar(outputFileName)
    val events = calendar.components
    val calendarEvents = events.mapNotNull { event -> CalendarEvent.from(event) }

    return calendarEvents.filter { isToday(it) }
}

private fun isToday(it: CalendarEvent) = it.begin.dayOfYear == LocalDateTime.now().dayOfYear

private suspend fun BehaviourContext.waitForGroup(it: CommonMessage<TextContent>) =
    waitText(
        SendTextMessage(
            it.chat.id,
            "Введи свою группу как в зачетке."
        )
    ).first().text
