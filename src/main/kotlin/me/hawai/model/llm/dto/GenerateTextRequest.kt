package me.hawai.model.llm.dto

import kotlinx.serialization.Serializable

@Serializable
data class GenerateTextRequest(
    val modelUri: String,
    val completionOptions: CompletionOptions,
    val messages: List<Message>
) {

    @Serializable
    data class CompletionOptions(
        val temperature: Double
    )

    @Serializable
    data class Message(
        val role: String,
        val text: String
    )
}