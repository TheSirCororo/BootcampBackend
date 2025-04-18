package me.hawai.telegram

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.User

@CommandHandler(["/start"])
suspend fun helloWorld(user: User, bot: TelegramBot) {
    message { "Hello, world!" }.send(user, bot)
}
