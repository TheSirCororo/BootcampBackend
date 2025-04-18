package me.hawai.validation

import io.konform.validation.ValidationBuilder

internal inline fun <reified T : Any> validator(noinline builder: ValidationBuilder<T>.() -> Unit) =
    ValidationManager.registerValidator(builder)
