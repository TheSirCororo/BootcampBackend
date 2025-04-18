package me.hawai.util

import io.github.smiley4.ktorswaggerui.dsl.routes.OpenApiRoute
import io.github.smiley4.ktorswaggerui.dsl.routing.documentation
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.resources.delete
import io.ktor.utils.io.KtorDsl

@KtorDsl
inline fun <reified T : Any> Route.get(
    noinline builder: OpenApiRoute.() -> Unit = { },
    noinline body: suspend RoutingContext.(T) -> Unit
): Route {
    return documentation(builder) { get(body) }
}

@KtorDsl
inline fun <reified T : Any> Route.post(
    noinline builder: OpenApiRoute.() -> Unit = { },
    noinline body: suspend RoutingContext.(T) -> Unit
): Route {
    return documentation(builder) { post(body) }
}

@KtorDsl
inline fun <reified T : Any> Route.put(
    noinline builder: OpenApiRoute.() -> Unit = { },
    noinline body: suspend RoutingContext.(T) -> Unit
): Route {
    return documentation(builder) { put(body) }
}

@KtorDsl
inline fun <reified T : Any> Route.delete(
    noinline builder: OpenApiRoute.() -> Unit = { },
    noinline body: suspend RoutingContext.(T) -> Unit
): Route {
    return documentation(builder) { delete(body) }
}
