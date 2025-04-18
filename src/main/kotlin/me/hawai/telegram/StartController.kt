package me.hawai.telegram

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.annotations.InputHandler
import eu.vendeli.tgbot.api.botactions.setMyCommands
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.internal.ProcessedUpdate
import eu.vendeli.tgbot.utils.setChain
import me.hawai.inject.telegramApi
import me.hawai.service.LlmService
import me.hawai.service.UserService
import org.koin.core.component.get

@CommandHandler(["/start"])
suspend fun start(user: User, bot: TelegramBot) = telegramApi {
    setMyCommands {
        botCommand("/start", "Регистрация")
        botCommand("/modify", "Изменить анкету")
        botCommand("/match", "Найти сожителей")
    }.send(bot)

    val userService = get<UserService>()
    val dbUser = userService.getUser(user.id)
    if (dbUser != null) {
        message { "Привет! Твоя анкета в ${dbUser.university}: ${dbUser.text}" }.send(user, bot)
    } else {
        message { "Заполни анкету. Напиши свой вуз." }.send(user, bot)
        bot.inputListener.setChain(user, StartChain.University)
    }
}

@CommandHandler(["/modify"])
suspend fun modify(user: User, bot: TelegramBot) {
    message { "Меняем анкету... Напиши свой вуз." }.send(user, bot)
    bot.inputListener.setChain(user, StartChain.University)
}
