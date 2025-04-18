package me.hawai.repo.user

import me.hawai.model.user.data.User
import me.hawai.repo.CrudRepo
import java.util.*

interface UserRepo : CrudRepo<UUID, User> {
    suspend fun createNewUser(telegramId: Long, text: String, university: String) = save(User(UUID.randomUUID(), telegramId, university, text))

    suspend fun getByTelegramId(telegramId: Long): User?

    suspend fun getAll(): List<User>
}