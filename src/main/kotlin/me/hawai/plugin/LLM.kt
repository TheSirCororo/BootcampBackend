package me.hawai.plugin

import io.ktor.server.application.*
import kotlinx.coroutines.launch
import me.hawai.service.LlmService
import org.koin.ktor.ext.get

fun Application.configureLLM() {
    get<LlmService>().apply {
        launch {
            init()
        }
    }
}