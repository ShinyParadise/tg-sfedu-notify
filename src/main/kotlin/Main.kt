import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.telegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitText
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import kotlinx.coroutines.flow.first
import java.io.File

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

    }.join()
}

private fun updateUserGroup(userId: String, group: String) {
    val existingUser = users.find { user -> (user.id == userId) }
    if (existingUser == null) {
        users.add(User(id = userId, group = group))
    } else {
        existingUser.group = group
    }
}