package me.hawai.repo.user

import me.hawai.model.user.data.User
import me.hawai.repo.CrudRepo
import java.util.*

interface UserRepo : CrudRepo<UUID, User> {
    suspend fun createNewUser(telegramId: Long, name: String, interests: String, university: String, text: String) =
        save(User(UUID.randomUUID(), telegramId, name, university, text, interests))

    suspend fun getByTelegramId(telegramId: Long): User?

    suspend fun getAll(): List<User>
}