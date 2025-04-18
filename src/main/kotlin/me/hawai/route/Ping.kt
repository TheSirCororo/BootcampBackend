package me.hawai.route

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.Route
import ru.cororo.corasense.route.Paths
import me.hawai.util.get

fun Route.ping() {
    get<Paths.Ping>({
        summary = "Проверка доступности приложения."

        response {
            code(HttpStatusCode.OK) {
                body<String>()
            }
        }
    }) {
        call.respondText("АЛЕКСАНДР ШАХОВ Я ВАШ ФАНАТ")
    }
}
