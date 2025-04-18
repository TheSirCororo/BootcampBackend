package me.hawai

import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.server.netty.EngineMain
import me.hawai.plugin.configureCORS
import me.hawai.plugin.configureDatabase
import me.hawai.plugin.configureKoin
import me.hawai.plugin.configureLLM
import me.hawai.plugin.configureNegotiation
import me.hawai.plugin.configureRouting
import me.hawai.plugin.configureStatusPages
import me.hawai.plugin.configureSwagger
import me.hawai.plugin.configureTelegram
import me.hawai.plugin.configureValidation


fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    log.info("Starting application...")

//    configureDatabase()
    configureKoin()
    configureNegotiation()
    configureCORS()
    configureValidation()
    configureStatusPages()
    configureRouting()
    configureSwagger()
    configureTelegram()
    configureLLM()
}
