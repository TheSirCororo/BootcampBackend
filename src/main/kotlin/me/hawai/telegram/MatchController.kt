package me.hawai.telegram

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.api.answer.answerCallbackQuery
import eu.vendeli.tgbot.api.media.photo
import eu.vendeli.tgbot.api.message.deleteMessage
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.internal.CallbackQueryUpdate
import eu.vendeli.tgbot.types.internal.ImplicitFile
import eu.vendeli.tgbot.utils.toInputFile
import me.hawai.inject.telegramApi
import me.hawai.service.FormViewService
import me.hawai.service.ImageService
import me.hawai.service.MatchingScoreService
import me.hawai.service.UserService
import org.koin.core.component.get

@CommandHandler(["/match"])
suspend fun matchCommand(tgUser: User, bot: TelegramBot) {
    message { "⬇\uFE0F Нажми на кнопку ниже, чтобы начать искать возможных сожителей:" }.inlineKeyboardMarkup {
        callbackData("\uD83D\uDE46\u200D♂\uFE0F Твои сожители") { "match?page=0" }
    }.send(tgUser, bot)
}

@CommandHandler.CallbackQuery(["match"])
suspend fun match(
    tgUser: User, bot: TelegramBot, update: CallbackQueryUpdate
) = telegramApi {
    answerCallbackQuery(update.callbackQuery.id).send(tgUser, bot)

    val userService = get<UserService>()
    val matchService = get<MatchingScoreService>()
    val formViewService = get<FormViewService>()
    val imageService = get<ImageService>()

    val fromMessageId = update.callbackQuery.message?.messageId
    if (fromMessageId == null) return@telegramApi

    val user = userService.getUser(tgUser.id) ?: return@telegramApi
    val scores = matchService.getSortedScores(user.id)
    val sortedUsers = scores.map {
        if (it.firstUser == user.id) it.secondUser
        else it.firstUser
    }.filter {
        formViewService.getFormView(user.id, it) == null
    }

    if (sortedUsers.isEmpty()) {
        message { "\uD83E\uDD72 Ты уже прочитал все анкеты! Возвращайся позже..." }.send(tgUser, bot)
        return@telegramApi
    }

    val matchedUserId = sortedUsers.first()
    val matchedUser = userService.getUserById(matchedUserId) ?: return@telegramApi

    formViewService.markAsViewed(user.id, matchedUserId)

    deleteMessage(fromMessageId).send(tgUser, bot)

    val text = """
☘️ Вот твой возможный сожитель:

${matchedUser.asView()}
""".trimIndent()
    if (matchedUser.photoId == null) {
        message { text }.inlineKeyboardMarkup {
            callbackData("❤\uFE0F Нравится") { "like?id=${matchedUser.telegramId}" }
            br()
            callbackData("↪\uFE0F Следующий кандидат") { "match" }
        }.send(tgUser, bot)
    } else {
        val imageData = imageService.loadImageBytes(matchedUser.photoId) ?: return@telegramApi

        photo(ImplicitFile.InpFile(imageData.toInputFile("image.jpg", "image/jpeg"))).caption { text }
            .inlineKeyboardMarkup {
                callbackData("❤\uFE0F Нравится") { "like?id=${matchedUser.telegramId}" }
                br()
                callbackData("↪\uFE0F Следующий кандидат") { "match" }
            }.send(tgUser, bot)
    }
}

@CommandHandler.CallbackQuery(["like"])
suspend fun like(id: String, tgUser: User, bot: TelegramBot, update: CallbackQueryUpdate) = telegramApi {
    answerCallbackQuery(update.callbackQuery.id).send(tgUser, bot)

    val userService = get<UserService>()
    val formViewService = get<FormViewService>()

    val fromMessageId = update.callbackQuery.message?.messageId
    if (fromMessageId == null) return@telegramApi

    val user = userService.getUser(tgUser.id) ?: return@telegramApi
    val matchedUser = userService.getUser(id.toLong()) ?: return@telegramApi

    formViewService.markAsLiked(user.id, matchedUser.id)

    deleteMessage(fromMessageId).send(tgUser, bot)
    val text =
        """
🩷 Ты лайкнул своего возможного сожителя:

${matchedUser.asView()}
""".trimIndent()

    val photoId = matchedUser.photoId
    if (photoId != null) {
        val imageService = get<ImageService>()
        val imageData = imageService.loadImageBytes(matchedUser.photoId) ?: return@telegramApi

        photo(ImplicitFile.InpFile(imageData.toInputFile("image.jpg", "image/jpeg"))).caption { text }.inlineKeyboardMarkup {
            callbackData("↪\uFE0F Следующий кандидат") { "match" }
        }.send(tgUser, bot)
    } else {
        message { text }.inlineKeyboardMarkup {
            callbackData("↪\uFE0F Следующий кандидат") { "match" }
        }.send(tgUser, bot)
    }

    val anotherFormView = formViewService.getFormView(matchedUser.id, user.id)
    val wasNotBothLike = anotherFormView == null || !anotherFormView.liked
    val startMessage = if (wasNotBothLike) {
        "\uD83D\uDD14 Дзынь-дзынь! Твою анкету лайкнул @${tgUser.username}."
    } else {
        "\uD83D\uDC20 Привет! У тебя взаимный лайк!"
    }

    val secondText = """
$startMessage

${user.asView()}
""".trimIndent()

    if (user.photoId != null) {
        val imageService = get<ImageService>()
        val imageData = imageService.loadImageBytes(user.photoId) ?: return@telegramApi
        photo(ImplicitFile.InpFile(imageData.toInputFile("image.jpg", "image/jpeg"))).caption { text }
            .inlineKeyboardMarkup {
                if (wasNotBothLike) {
                    callbackData("❤\uFE0F Нравится") { "like?id=${user.telegramId}" }
                    br()
                    callbackData("\uD83D\uDEAB Игнорировать") { "start-callback" }
                }
            }.send(matchedUser.telegramId, bot)
    } else {
       message { text }
            .inlineKeyboardMarkup {
                if (wasNotBothLike) {
                    callbackData("❤\uFE0F Нравится") { "like?id=${user.telegramId}" }
                    br()
                    callbackData("\uD83D\uDEAB Игнорировать") { "start-callback" }
                }
            }.send(matchedUser.telegramId, bot)
    }

}