package me.hawai.telegram

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.InputChain
import eu.vendeli.tgbot.api.getFile
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.generated.getAllState
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.internal.BreakCondition
import eu.vendeli.tgbot.types.internal.ChainLink
import eu.vendeli.tgbot.types.internal.MessageUpdate
import eu.vendeli.tgbot.types.internal.ProcessedUpdate
import eu.vendeli.tgbot.types.internal.chain.BaseStatefulLink
import eu.vendeli.tgbot.types.internal.getOrNull
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentDisposition
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import me.hawai.inject.telegramApi
import me.hawai.service.ImageService
import me.hawai.service.ModerationService
import me.hawai.service.UserService
import org.koin.core.component.get
import java.util.UUID

private val httpClient = HttpClient(CIO)

@InputChain
object StartChain {
    object Name : BaseStatefulLink() {

        override suspend fun action(user: User, update: ProcessedUpdate, bot: TelegramBot): String {
            message { "\uD83D\uDCDA Введи свой вуз." }.send(user, bot)
            return update.text
        }
    }

    object University : BaseStatefulLink() {
        override suspend fun action(
            user: User,
            update: ProcessedUpdate,
            bot: TelegramBot
        ): String {
            message { "⚾\uFE0F Расскажи о своих интересах. Это может помочь при поиске подходящего сожителя." }.send(user, bot)
            return update.text
        }
    }

    object Interests : BaseStatefulLink() {
        override suspend fun action(user: User, update: ProcessedUpdate, bot: TelegramBot): String {
            message { "\uD83D\uDDD2 Введи текст своей анкеты." }.send(user, bot)
            return update.text
        }
    }

    object Text : BaseStatefulLink() {
        override suspend fun action(
            tgUser: User,
            update: ProcessedUpdate,
            bot: TelegramBot
        ): String {
            message { "\uD83C\uDFDE Отправь изображение своей анкеты или нажми на кнопку отмены." }
                .replyKeyboardMarkup {
                    +"\uD83D\uDEAB Отмена"
                }.send(tgUser, bot)

            return update.text
        }

    }

    object Image : ChainLink() {
        override val breakCondition: BreakCondition = BreakCondition { user, update, _ ->
            update !is MessageUpdate || (update.message.text != "\uD83D\uDEAB Отмена" && update.message.photo == null)
        }

        override suspend fun breakAction(user: User, update: ProcessedUpdate, bot: TelegramBot) {
            message { "Некорректное фото. Повтори попытку." }.replyKeyboardMarkup {
                +"\uD83D\uDEAB Отмена"
            }.send(user, bot)
        }

        override suspend fun action(tgUser: User, update: ProcessedUpdate, bot: TelegramBot) = telegramApi {
            if (update !is MessageUpdate) return@telegramApi

            val photo = update.message.photo?.firstOrNull()
            if (photo == null && update.text != "\uD83D\uDEAB Отмена") {
                return@telegramApi
            }

            val photoFile = photo?.let {
                getFile(it.fileId).sendReturning(bot).getOrNull() ?: run {
                    message { "Некорректное фото!" }.send(tgUser, bot)
                    return@telegramApi
                }
            }

            val photoId = if (photoFile != null) {
                val imageBytes = httpClient.get(bot.getFileDirectUrl(photoFile) ?: return@telegramApi).bodyAsChannel()
                val partData = PartData.FileItem({ imageBytes }, {}, Headers.build {
                    this[HttpHeaders.ContentDisposition] =
                        ContentDisposition.File.withParameter(ContentDisposition.Parameters.FileName, "image.jpg")
                            .toString()
                })

                val multipart = object : MultiPartData {
                    override suspend fun readPart(): PartData = partData
                }

                val id = UUID.randomUUID()
                val imageService = get<ImageService>()
                imageService.uploadImage(multipart, id)
                imageService.saveImage(id, "image.jpg")
                id
            } else {
                null
            }

            val state = tgUser.getAllState(StartChain)

            val name = state.Name ?: return@telegramApi
            val university = state.University ?: return@telegramApi
            val interests = state.Interests ?: return@telegramApi
            val text = state.Text ?: return@telegramApi

            val moderationService = get<ModerationService>()
            val moderationText = "$name\n$university\n$interests\n$text"
            if (!moderationService.moderate(moderationText)) {
                message { "Нам не нравится, когда вы создаёте непристойные анкеты. Попробуйте снова." }.send(tgUser, bot)
                return@telegramApi
            }

            val userService = get<UserService>()
            val user = userService.getUser(tgUser.id)

            if (user == null) {
                userService.createUser(tgUser.id, name, interests, university, text, photoId)
                message { "✅ Успешная регистрация! Чтобы изменить анкету, введи /modify" }.replyKeyboardRemove().send(tgUser, bot)
            } else {
                userService.save(user.copy(university = university, text = text, interests = interests, name = name, photoId = photoId))
                message { "✅ Анкета успешно изменена!" }.replyKeyboardRemove().send(tgUser, bot)
            }

            start(tgUser, bot)
        }
    }
}