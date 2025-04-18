package me.hawai.plugin

import io.ktor.server.application.*
import me.hawai.service.LlmService
import me.hawai.service.TelegramBotService
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

val appModules = mutableListOf<Module>()

fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        appModules.add(0, module {
            single<Application> { this@configureKoin }
            single<TelegramBotService> { TelegramBotService(get()) }
            single<LlmService> { LlmService(get()) }
        })

        modules(appModules)
        allowOverride(true)
    }

    monitor.subscribe(ApplicationStopped) {
        stopKoin()
    }
}
