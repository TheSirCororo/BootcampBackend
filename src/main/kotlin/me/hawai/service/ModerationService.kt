package me.hawai.service

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ModerationService(private val llmService: LlmService) {
    suspend fun moderate(text: String): Boolean {
        val responseText = llmService.prompt(MODERATION_PROMPT, Json.encodeToString(ModerationRequest(text)))?.replace("`", "")
        return if (responseText != null) Json.decodeFromString<ModerationResponse>(responseText).verdict else false
    }

    @Serializable
    private data class ModerationRequest(
        val text: String
    )

    @Serializable
    private data class ModerationResponse(
        val verdict: Boolean
    )

    companion object {
        const val MODERATION_PROMPT = """
            Ты - модератор грубых, оскорбительных и странных фраз. Я буду подавать текст в следующем виде:
            
            {"text":"текст"}
            
            Ты должен выдать следующий ответ:
            
            {"verdict": true}
            
            (кроме true может быть false, если текст проверку не прошёл)
            
            Запрос идёт далее.
        """
    }
}