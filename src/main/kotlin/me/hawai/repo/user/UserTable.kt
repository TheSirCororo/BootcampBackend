package me.hawai.repo.user

import org.jetbrains.exposed.dao.id.UUIDTable

object UserTable : UUIDTable("users") {
    val telegramId = long("telegram_id").uniqueIndex()
    val university = varchar("university", 250)
    val text = largeText("text")
}
