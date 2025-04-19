package me.hawai.telegram

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.api.botactions.setMyCommands
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.utils.setChain
import me.hawai.inject.telegramApi
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
        message {
            """
            Привет! Твоя анкета: ${dbUser.text}
            
            Имя: ${dbUser.name}
            Интересы: ${dbUser.interests}
            ВУЗ: ${dbUser.university}
            Текст анкеты: ${dbUser.text}
            """.trimIndent()
        }.send(user, bot)
    } else {
        message { "Заполни анкету. Сначала напиши своё имя." }.send(user, bot)
        bot.inputListener.setChain(user, StartChain.Name)
    }
}

@CommandHandler(["/modify"])
suspend fun modify(user: User, bot: TelegramBot) {
    message { "Меняем анкету... Сначала напиши своё имя." }.send(user, bot)
    bot.inputListener.setChain(user, StartChain.Name)
}
