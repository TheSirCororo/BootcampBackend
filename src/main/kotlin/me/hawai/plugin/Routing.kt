package me.hawai.plugin

import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*
import me.hawai.route.ping

fun Application.configureRouting() {
    install(Resources)

    routing {
        ping()
    }
}