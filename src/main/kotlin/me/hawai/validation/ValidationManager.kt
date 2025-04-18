package me.hawai.validation

import io.konform.validation.Validation
import io.konform.validation.ValidationBuilder
import kotlin.reflect.KClass

object ValidationManager {
    private val validators = mutableMapOf<KClass<out Any>, Validation<*>>()

    fun <T : Any> registerValidator(
        kClass: KClass<T>,
        validator: Validation<T>,
    ): Validation<T> {
        validators.put(kClass, validator)
        return validator
    }

    inline fun <reified T : Any> registerValidator(validator: Validation<T>) = registerValidator(T::class, validator)

    inline fun <reified T : Any> registerValidator(noinline builder: ValidationBuilder<T>.() -> Unit) =
        registerValidator(Validation(builder))

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> validate(value: T) = (validators[value::class] as? Validation<T>)?.validate(value)
}

fun <T : Any> T.validate() = ValidationManager.validate(this)
