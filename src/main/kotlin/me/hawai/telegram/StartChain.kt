package me.hawai.telegram

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.InputChain
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.generated.getAllState
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.internal.BreakCondition
import eu.vendeli.tgbot.types.internal.ChainLink
import eu.vendeli.tgbot.types.internal.ProcessedUpdate
import eu.vendeli.tgbot.types.internal.chain.BaseStatefulLink
import me.hawai.inject.telegramApi
import me.hawai.service.ModerationService
import me.hawai.service.UserService
import org.koin.core.component.get

@InputChain
object StartChain {
    object Name : BaseStatefulLink() {

        override suspend fun action(user: User, update: ProcessedUpdate, bot: TelegramBot): String {
            message { "Введи свой вуз." }.send(user, bot)
            return update.text
        }
    }

    object University : BaseStatefulLink() {
        override suspend fun action(
            user: User,
            update: ProcessedUpdate,
            bot: TelegramBot
        ): String {
            message { "Расскажи о своих интересах. Это может помочь при поиске подходящего сожителя." }.send(user, bot)
            return update.text
        }
    }

    object Interests : BaseStatefulLink() {
        override suspend fun action(user: User, update: ProcessedUpdate, bot: TelegramBot): String {
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

            val name = state.Name ?: return@telegramApi
            val university = state.University ?: return@telegramApi
            val interests = state.Interests ?: return@telegramApi
            val text = update.text

            val moderationService = get<ModerationService>()
            val moderationText = "$name\n$university\n$interests\n$text"
            if (!moderationService.moderate(moderationText)) {
                message { "Нам не нравится, когда вы создаёте непристойные анкеты. Попробуйте снова." }.send(tgUser, bot)
                return@telegramApi
            }

            val userService = get<UserService>()
            val user = userService.getUser(tgUser.id)

            if (user == null) {
                userService.createUser(tgUser.id, name, interests, university, text)
                message { "Успешная регистрация! Чтобы изменить анкету, введи /modify" }.send(tgUser, bot)
            } else {
                userService.save(user.copy(university = university, text = text))
                message { "Анкета успешно изменена!" }.send(tgUser, bot)
            }

        }

    }
}