package me.hawai.inject

import io.ktor.server.routing.*
import org.koin.core.component.KoinComponent

class ApiRoute : KoinComponent

fun <T : Route> T.api(configure: T.(ApiRoute) -> Unit) = configure(ApiRoute())

suspend fun <R> telegramApi(configure: suspend ApiRoute.() -> R): R = configure(ApiRoute())
