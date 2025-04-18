package me.hawai.service

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.hawai.model.user.data.User
import me.hawai.repo.matching.MatchingScoreRepo
import java.util.*

class MatchingScoreService(private val matchingScoreRepo: MatchingScoreRepo, private val llmService: LlmService) {
    suspend fun matchUsers(firstUser: User, otherUsers: List<User>) {
        if (otherUsers.isEmpty()) return

        val responseText = llmService.prompt(
            MATCHING_PROMPT + Json.encodeToString(
                PromptRequest(
                    firstUser.text,
                    otherUsers.associate { it.id.toString() to it.text })
            )
        )
        val response = Json.decodeFromString<PromptResponse>(responseText.replace("`", ""))
        response.response.forEach { (userId, score) ->
            matchingScoreRepo.saveScore(firstUser.id, UUID.fromString(userId), score)
        }
    }

    suspend fun getSortedScores(user: UUID) = matchingScoreRepo.getSortedScores(user)

    @Serializable
    private data class PromptRequest(
        val text: String,
        @SerialName("other_texts")
        val otherTexts: Map<String, String>
    )

    @Serializable
    private data class PromptResponse(
        val response: Map<String, Float>
    )

    companion object {
        const val MATCHING_PROMPT = """
            Привет. Сейчас ты получишь запрос в следующем формате:
            {"text": "текст анкеты", "other_texts": [{"123123": "другой текст анкеты"}]}
            
            Это нужно для метчинга анкет между друг другом. Нужно сметчить анкету с текстом под параметром text с другими. 123123 в этом примере - идентификатор другой анкеты. Выдай ответ в следующем формате:
            
            {"response": {"123123": 1.0}}
           
           Это рейтинг каждой анкеты для исходной. Он должен быть вещественным числом от 0.0 до 10.0. Не используй кавычки типа ` (markdown)
           
           Вот данные:
           
        """
    }
}