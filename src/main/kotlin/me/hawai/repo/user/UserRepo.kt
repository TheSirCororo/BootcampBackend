package me.hawai.repo.user

import me.hawai.model.user.data.User
import me.hawai.repo.CrudRepo
import java.util.*

interface UserRepo : CrudRepo<UUID, User> {
    suspend fun createNewUser(telegramId: Long, name: String, interests: String, university: String, text: String, photoId: UUID?) =
        save(User(UUID.randomUUID(), telegramId, name, university, text, interests, photoId))

    suspend fun getByTelegramId(telegramId: Long): User?

    suspend fun getAll(): List<User>
}