package me.hawai.service

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import me.hawai.model.llm.dto.GenerateTextRequest
import me.hawai.model.llm.dto.GenerateTextResponse
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

class LlmService(private val application: Application, client: HttpClient? = null) : CoroutineScope {
    private val yandexApiFolder = application.environment.config.tryGetString("llm.folder") ?: ""
    private var llmEnabled =
        application.environment.config.tryGetString("llm.enabled")?.toBooleanStrictOrNull() == true
    private val yandexApiKey =
        application.environment.config.tryGetString("llm.api_key") ?: ""
    private val yandexModelUri = "gpt://$yandexApiFolder/yandexgpt-lite/latest"
    private val yandexBaseUrl = "https://llm.api.cloud.yandex.net/foundationModels/v1/completion"
    internal val client = client ?: HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }
    }

    override val coroutineContext: CoroutineContext = Dispatchers.IO + CoroutineName("LLM Service")

    suspend fun init() {
        if (llmEnabled) {
            var successful = false
            for (i in 0..<5) {
                if (test()) {
                    successful = true
                    break
                }

                application.log.warn("#${i + 1} Не удалось совершить запрос к LLM. Повторяем попытку через 1 секунду.")
                delay(1.seconds)
            }

            if (!successful) {
                application.log.warn("Не удалось подключиться к LLM за 5 попыток. Отключаем LLM.")
                llmEnabled = false
            }
        }
    }

    private suspend fun test(): Boolean {
        return try {
            prompt("Привет! Это тестовое сообщение, ответь на него просто Привет.")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun isActive() = llmEnabled

    suspend fun prompt(text: String) = this@LlmService.client.post(yandexBaseUrl) {
        setBody(GenerateTextRequest(yandexModelUri, GenerateTextRequest.CompletionOptions(0.3), listOf(GenerateTextRequest.Message(role = "user", text = text))))
        bearerAuth(yandexApiKey)
        contentType(ContentType.Application.Json)
    }.also {
        if (!it.status.isSuccess()) {
            application.log.warn("Got status ${it.status}. Body is: ${it.bodyAsText()}")
        }
    }.body<GenerateTextResponse>().result.alternatives.first().message.text

}