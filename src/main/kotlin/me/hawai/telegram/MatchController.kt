package me.hawai.telegram

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.api.answer.answerCallbackQuery
import eu.vendeli.tgbot.api.message.editMessageText
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.internal.CallbackQueryUpdate
import me.hawai.inject.telegramApi
import me.hawai.service.MatchingScoreService
import me.hawai.service.UserService
import org.koin.core.component.get

@CommandHandler(["/match"])
suspend fun matchCommand(tgUser: User, bot: TelegramBot) {
    message { "Нажми на кнопку ниже, чтобы начать искать возможных сожителей:" }
        .inlineKeyboardMarkup {
            callbackData("Твои сожители") { "match?page=0" }
        }.send(tgUser, bot)
}

@CommandHandler.CallbackQuery(["match"])
suspend fun match(
    page: String, tgUser: User, bot: TelegramBot, update: CallbackQueryUpdate
) = telegramApi {
    answerCallbackQuery(update.callbackQuery.id).send(tgUser, bot)

    val page = page.toInt()
    val userService = get<UserService>()
    val matchService = get<MatchingScoreService>()

    val fromMessageId = update.callbackQuery.message?.messageId
    if (fromMessageId == null) return@telegramApi

    val user = userService.getUser(tgUser.id) ?: return@telegramApi
    val scores = matchService.getSortedScores(user.id)
    val sortedUsers = scores.map {
        if (it.firstUser == user.id) it.secondUser
        else it.firstUser
    }

    val pageCount = scores.size
    if (sortedUsers.isEmpty() || pageCount == 0 || page > pageCount - 1) return@telegramApi

    val matchedUserId = try {
        sortedUsers[maxOf(page, pageCount - 1)]
    } catch (_: Exception) {
        return@telegramApi
    }

    val matchedUser = userService.getUserById(matchedUserId) ?: return@telegramApi

    editMessageText(fromMessageId) {
        "Вот твой возможный сожитель:\nВУЗ: ${matchedUser.university}\nТекст анкеты:\n${matchedUser.text}"
    }.inlineKeyboardMarkup {
        callbackData("◀\uFE0F") { "match?page=${page - 1}" }
        callbackData("$page / $pageCount") { "nothing" }
        callbackData("▶\uFE0F") { "match?page=${page + 1}" }
    }
}