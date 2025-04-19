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
            MATCHING_PROMPT, Json.encodeToString(
                PromptRequest(Form(firstUser.text, firstUser.university, firstUser.interests), otherUsers.associate {
                    it.id.toString() to Form(it.text, it.university, it.interests)
                })
            )
        ) ?: return

        val response = Json.decodeFromString<PromptResponse>(responseText.replace("`", ""))
        response.response.forEach { (userId, score) ->
            matchingScoreRepo.saveScore(firstUser.id, UUID.fromString(userId), score)
        }
    }

    suspend fun getSortedScores(user: UUID) = matchingScoreRepo.getSortedScores(user)

    @Serializable
    private data class Form(
        val text: String,
        val university: String,
        val interests: String
    )

    @Serializable
    private data class PromptRequest(
        val form: Form,
        @SerialName("other_forms")
        val otherForms: Map<String, Form>
    )

    @Serializable
    private data class PromptResponse(
        val response: Map<String, Float>
    )

    companion object {
        const val MATCHING_PROMPT = """
            Привет. Ты - бот для метчинга студентов, которые хотят вместе снимать квартиру. Студенты заполняют анкеты со следующими полями:
            В приоритете студенты, поступающие в один вуз. Но допустимы и те варианты, при которых два студента снимают квартиру, но при этом поступают в два разных вуза, которые находятся в одном городе.
            Следующий приоритет - общие интересы.
            И наконец, учитывай текст анкет.
            Сейчас ты получишь запрос в следующем формате:
            
            {"form": {"text": "текст анкеты", "university": "название вуза", "interests": "интересы"}, "other_texts": [{"123123": {"text": "другой текст анкеты", "university": "название вуза", "interests": "интересы"}}]}

            Это нужно для метчинга анкет между друг другом. Нужно сметчить анкету с текстом под параметром text с другими. 123123 в этом примере - идентификатор другой анкеты. Выдай ответ в следующем формате:
            
            {"response": {"123123": 5.0}}
           
           Отвечай ТОЛЬКО ТАК и НИКАК ИНАЧЕ. Это ОБЯЗАТЕЛЬНО должен быть валидный JSON.
           Это рейтинг каждой анкеты для исходной. Он должен быть вещественным числом СТРОГО от 0.0 до 10.0, где 10.0 - студенты супер подходят друг другу, а 0.0 - студенты абсолютно не подходят друг другу. Не используй кавычки типа ` (markdown)
           
           Данные ты получишь следующим сообщением.
           
        """
    }
}