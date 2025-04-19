package me.hawai.telegram

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.api.answer.answerCallbackQuery
import eu.vendeli.tgbot.api.botactions.setMyCommands
import eu.vendeli.tgbot.api.media.photo
import eu.vendeli.tgbot.api.message.deleteMessage
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.internal.CallbackQueryUpdate
import eu.vendeli.tgbot.types.internal.ImplicitFile
import eu.vendeli.tgbot.utils.setChain
import eu.vendeli.tgbot.utils.toInputFile
import me.hawai.inject.telegramApi
import me.hawai.service.ImageService
import me.hawai.service.UserService
import org.koin.core.component.get
import me.hawai.model.user.data.User as DbUser

@CommandHandler(["/start"])
suspend fun start(user: User, bot: TelegramBot) = telegramApi {
    setMyCommands {
        botCommand("/start", "Регистрация/просмотреть свою анкету")
        botCommand("/modify", "Изменить анкету")
        botCommand("/match", "Найти сожителей")
    }.send(bot)

    val userService = get<UserService>()
    val dbUser = userService.getUser(user.id)
    if (dbUser != null) {
        val text = """
☺️ Привет! Твоя анкета:

${dbUser.asView()}
            """.trimIndent()

        if (dbUser.photoId != null) {
            val imageService = get<ImageService>()
            val imageData = imageService.loadImageBytes(dbUser.photoId) ?: return@telegramApi
            photo(ImplicitFile.InpFile(imageData.toInputFile("image.jpg", "image/jpeg"))).caption { text }.inlineKeyboardMarkup {
                callbackData("\uD83D\uDCDD Изменить анкету") { "modify-callback" }
                br()
                callbackData("\uD83D\uDC40 Искать сожителей") { "match" }
            }.send(user, bot)
        } else {
            message {
                text
            }.inlineKeyboardMarkup {
                callbackData("\uD83D\uDCDD Изменить анкету") { "modify-callback" }
                br()
                callbackData("\uD83D\uDC40 Искать сожителей") { "match" }
            }.send(user, bot)
        }
    } else {
        message { "Заполни анкету. \uD83D\uDE3A Сначала напиши своё имя." }.send(user, bot)
        bot.inputListener.setChain(user, StartChain.Name)
    }
}

@CommandHandler.CallbackQuery(["ignore-callback"])
suspend fun startCallback(user: User, bot: TelegramBot, update: CallbackQueryUpdate) {
    answerCallbackQuery(update.callbackQuery.id).send(user, bot)
    val fromMessageId = update.callbackQuery.message?.messageId
    if (fromMessageId == null) return

    deleteMessage(fromMessageId).send(user, bot)
    start(user, bot)
}

@CommandHandler(["/modify"])
suspend fun modify(user: User, bot: TelegramBot) {
    message { "Меняем анкету... \uD83D\uDE3A Сначала напиши своё имя." }.send(user, bot)
    bot.inputListener.setChain(user, StartChain.Name)
}

@CommandHandler.CallbackQuery(["modify-callback"])
suspend fun modifyCallback(user: User, bot: TelegramBot, update: CallbackQueryUpdate) {
    answerCallbackQuery(update.callbackQuery.id).send(user, bot)
    modify(user, bot)
}

fun DbUser.asView() = """
😺 Имя: $name
⚾️ Интересы: $interests
📚 ВУЗ: $university
🗒 Текст анкеты: $text
""".trimIndent()
