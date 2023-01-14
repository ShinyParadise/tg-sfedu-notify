import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.telegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitText
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.utils.PreviewFeature
import kotlinx.coroutines.flow.first
import java.io.File

@OptIn(PreviewFeature::class)
suspend fun main() {
    val token = File("src/main/resources/token.txt").readText(Charsets.UTF_8)

    val bot = telegramBot(token)

    bot.buildBehaviourWithLongPolling {
        println(getMe())

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

            println(group)

        }

    }.join()
}