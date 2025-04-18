package me.hawai.plugin

import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS

fun Application.configureCORS() {
    install(CORS) {
        allowCredentials = true
        allowSameOrigin = true
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Patch)
        anyHost()
        allowNonSimpleContentTypes = true
    }
}
