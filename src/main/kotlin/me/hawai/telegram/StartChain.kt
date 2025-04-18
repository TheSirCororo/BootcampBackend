package me.hawai.telegram

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.InputChain
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.generated.getAllState
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.internal.ChainLink
import eu.vendeli.tgbot.types.internal.ProcessedUpdate
import eu.vendeli.tgbot.types.internal.chain.BaseStatefulLink
import me.hawai.inject.telegramApi
import me.hawai.service.UserService
import org.koin.core.component.get

@InputChain
object StartChain {
    object University : BaseStatefulLink() {
        override suspend fun action(
            user: User,
            update: ProcessedUpdate,
            bot: TelegramBot
        ): String {
            message { "Введи текст своей анкеты." }.send(user, bot)
            return update.text
        }
    }

    object Text : ChainLink() {
        override suspend fun action(
            tgUser: User,
            update: ProcessedUpdate,
            bot: TelegramBot
        ) = telegramApi {
            val state = tgUser.getAllState(StartChain)
            val university = state.University ?: return@telegramApi
            val text = update.text
            val userService = get<UserService>()
            val user = userService.getUser(tgUser.id)
            if (user == null) {
                userService.createUser(tgUser.id, university, text)
                message { "Успешная регистрация! Чтобы изменить анкету, введи /modify" }.send(tgUser, bot)
            } else {
                userService.save(user.copy(university = university, text = text))
                message { "Анкета успешно изменена!" }.send(tgUser, bot)
            }

        }

    }
}