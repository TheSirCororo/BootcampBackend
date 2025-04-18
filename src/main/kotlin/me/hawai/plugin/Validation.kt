package me.hawai.plugin

import io.konform.validation.Invalid
import io.konform.validation.ValidationError
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.plugins.requestvalidation.ValidationResult
import me.hawai.validation.validate

fun Application.configureValidation() {
    install(RequestValidation) {
        validate<Any> { data ->
            val result = data.validate()
            if (result != null && result is Invalid) {
                ValidationResult.Invalid(result.errors.map { it.asString(data) })
            } else if (result == null) {
                if (data is Iterable<*>) {
                    log.debug("Validator for {} was not found. Validating each element.", data)
                    data.mapNotNull { it?.validate() }.let {
                        val invalidResults = it.filter { res -> res is Invalid }
                        if (invalidResults.isNotEmpty()) {
                            ValidationResult.Invalid(invalidResults.flatMap { it.errors.map { it.asString(data) } })
                        } else {
                            ValidationResult.Valid
                        }
                    }
                } else {
                    log.debug("Validator for {} was not found.", data)
                    ValidationResult.Valid
                }
            } else {
                ValidationResult.Valid
            }
        }
    }
}

private fun <T : Any> ValidationError.asString(data: T) = "${data::class.simpleName}$dataPath: $message"
