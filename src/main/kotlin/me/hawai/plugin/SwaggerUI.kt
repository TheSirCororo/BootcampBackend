package me.hawai.plugin

import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.github.smiley4.ktorswaggerui.routing.openApiSpec
import io.github.smiley4.ktorswaggerui.routing.swaggerUI
import io.github.smiley4.schemakenerator.reflection.processReflection
import io.github.smiley4.schemakenerator.swagger.compileReferencingRoot
import io.github.smiley4.schemakenerator.swagger.data.TitleType
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.handleCoreAnnotations
import io.github.smiley4.schemakenerator.swagger.withTitle
import io.ktor.server.application.*
import io.ktor.server.routing.*
import java.util.*

fun Application.configureSwagger() {
    install(SwaggerUI) {
        info {
            title = "CoraSense API"
            version = "latest"
            description = "Агрегатор рекламных объявлений."
        }

        server {
            url = "http://localhost:8080"
            description = "Server"
        }

        routing {
            route("/openapi/api.json") {
                openApiSpec()
            }

            route("/swagger") {
                swaggerUI("/openapi/api.json")
            }
        }

        schemas {
            overwrite<UUID, String>()

            generator = { type ->
                type
                    .processReflection {
                        redirect<UUID, String>()
                    }
                    .generateSwaggerSchema()
                    .handleCoreAnnotations()
                    .withTitle(TitleType.SIMPLE)
                    .compileReferencingRoot()
            }
        }
    }
}