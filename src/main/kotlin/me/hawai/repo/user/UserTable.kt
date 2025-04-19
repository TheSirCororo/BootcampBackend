package me.hawai.repo.user

import me.hawai.repo.image.ImageTable
import org.jetbrains.exposed.dao.id.UUIDTable

object UserTable : UUIDTable("users") {
    val telegramId = long("telegram_id").uniqueIndex()
    val university = varchar("university", 250)
    val text = largeText("text")
    val interests = largeText("interests")
    val name = varchar("name", 64)
    val photoId = reference("photo_id", ImageTable).nullable()
}
