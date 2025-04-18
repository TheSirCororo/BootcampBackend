package me.hawai.model.llm.dto

import kotlinx.serialization.Serializable

@Serializable
data class GenerateTextResponse(
    val result: Result
) {

    @Serializable
    data class Result(
        val alternatives: List<Alternative>
    )

    @Serializable
    data class Alternative(
        val message: Message
    )

    @Serializable
    data class Message(
        val role: String,
        val text: String
    )
}