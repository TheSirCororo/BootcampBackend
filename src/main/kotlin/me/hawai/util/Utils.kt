package me.hawai.util

import io.ktor.server.application.*
import me.hawai.model.dto.Errors
import me.hawai.model.dto.respond
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*

fun ApplicationCall.getUuid(stringId: String): UUID =
    try {
        UUID.fromString(stringId)
    } catch (_: Exception) {
        Errors.BadRequest.respond()
    }

fun <T : Any> IdTable<T>.deleteById(id: T) = deleteWhere { this.id eq id }

fun <T : Any> IdTable<T>.getById(id: T) = selectAll().where { this@getById.id eq id }.singleOrNull()

fun ColumnSet.select(
    expression: Expression<*>,
    vararg tables: Table,
    additionalExpressions: List<Expression<*>> = listOf()
) =
    select(tables.flatMap { it.columns } + expression + additionalExpressions)


