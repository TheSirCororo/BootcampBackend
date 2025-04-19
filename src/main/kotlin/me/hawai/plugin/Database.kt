package me.hawai.plugin

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import io.ktor.server.config.*
import me.hawai.repo.matching.MatchingScoreTable
import me.hawai.repo.user.UserTable
import me.hawai.repo.view.FormViewTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.TimeUnit

internal var hikariDs: HikariDataSource? = null

fun Application.configureDatabase() {
    val config = environment.config
    val databaseJdbcUrl = config.tryGetString("database.jdbc_url") ?: return
    val databaseUsername = config.tryGetString("database.username") ?: return
    val databasePassword = config.tryGetString("database.password") ?: return
    val hikariPoolEnabled = config.tryGetString("database.hikari_enabled")?.toBooleanStrictOrNull() != false
    if (hikariPoolEnabled) {
        hikariDs = setupHikari(databaseJdbcUrl, databaseUsername, databasePassword)
        Database.connect(datasource = hikariDs!!)
    } else {
        Database.connect(url = databaseJdbcUrl, user = databaseUsername, password = databasePassword)
    }

    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(UserTable, MatchingScoreTable, FormViewTable)
    }

    monitor.subscribe(ApplicationStopped) {
        hikariDs?.close()
    }
}

private fun setupHikari(databaseJdbcUrl: String, databaseUsername: String, databasePassword: String): HikariDataSource =
    HikariDataSource(HikariConfig().apply {
        driverClassName = "org.postgresql.Driver"
        jdbcUrl = databaseJdbcUrl
        maximumPoolSize = Runtime.getRuntime().availableProcessors() * 2 + 3
        minimumIdle = maximumPoolSize / 2
        isAutoCommit = false
        username = databaseUsername
        password = databasePassword
        validate()
    })

private val sqlDebug = System.getProperty("sql_debug") != null

suspend fun <R> sql(block: suspend Transaction.() -> R): R {
    val start = System.nanoTime()
    return try {
        newSuspendedTransaction {
            if (sqlDebug) {
                addLogger(StdOutSqlLogger)
            }

            block()
        }
    } finally {
        val delta = (System.nanoTime() - start).let { TimeUnit.NANOSECONDS.toMillis(it) }
        if (sqlDebug) {
            println("Full transaction duration: $delta ms")
        }
    }
}
