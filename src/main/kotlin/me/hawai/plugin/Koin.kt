package me.hawai.plugin

import io.ktor.server.application.*
import me.hawai.repo.matching.DatabaseMatchingScoreRepo
import me.hawai.repo.matching.MatchingScoreRepo
import me.hawai.repo.user.DatabaseUserRepo
import me.hawai.repo.user.UserRepo
import me.hawai.repo.view.DatabaseFormViewRepo
import me.hawai.repo.view.FormViewRepo
import me.hawai.service.FormViewService
import me.hawai.service.LlmService
import me.hawai.service.MatchingScoreService
import me.hawai.service.ModerationService
import me.hawai.service.TelegramBotService
import me.hawai.service.UserService
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
            single<UserRepo> { DatabaseUserRepo }
            single<UserService> { UserService(get(), get()) }
            single<MatchingScoreRepo> { DatabaseMatchingScoreRepo }
            single<MatchingScoreService> { MatchingScoreService(get(), get()) }
            single<ModerationService> { ModerationService(get()) }
            single<FormViewRepo> { DatabaseFormViewRepo }
            single<FormViewService> { FormViewService(get(), get()) }
        })

        modules(appModules)
        allowOverride(true)
    }

    monitor.subscribe(ApplicationStopped) {
        stopKoin()
    }
}
