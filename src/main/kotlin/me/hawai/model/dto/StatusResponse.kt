package me.hawai.model.dto

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.respond
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Serializable
sealed interface StatusResponse {
    val message: String
    val statusCode: Int
    val httpCode: Int

    @Serializable
    data class Ok(
        @SerialName("status")
        override val message: String = "ok",
        @Transient
        override val statusCode: Int = 0,
        @Transient
        override val httpCode: Int = 200
    ) : StatusResponse

    @Serializable
    data class Error(
        override val message: String,
        override val statusCode: Int,
        @Transient override val httpCode: Int = 400
    ) :
        StatusResponse

}

data class StatusCodeException(val response: StatusResponse) : RuntimeException(response.message)

inline fun <reified T : Any> ErrorBuilder(
    httpCode: Int = 400, statusCode: Int = 1, crossinline messageBuilder: (T) -> String
): (T) -> StatusResponse.Error = { StatusResponse.Error(messageBuilder(it), statusCode, httpCode) }

suspend fun ApplicationCall.respond(statusResponse: StatusResponse) =
    respond(HttpStatusCode.fromValue(statusResponse.httpCode), statusResponse)

fun StatusResponse.respond(): Nothing = throw StatusCodeException(this)

suspend fun ApplicationCall.respondOk(message: String = "ok") = respond(StatusResponse.Ok(message))

