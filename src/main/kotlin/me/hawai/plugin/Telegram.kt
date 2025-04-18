package me.hawai.plugin

import io.ktor.server.application.*
import me.hawai.service.TelegramBotService
import org.koin.ktor.ext.get

fun Application.configureTelegram() {
    get<TelegramBotService>().start()

    monitor.subscribe(ApplicationStopped) {
        get<TelegramBotService>().close()
    }
}