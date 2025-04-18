package me.hawai.service

import me.hawai.model.user.data.User
import me.hawai.repo.user.UserRepo
import java.util.UUID

class UserService(private val userRepo: UserRepo, private val matchingScoreService: MatchingScoreService) {
    suspend fun getUser(telegramId: Long) = userRepo.getByTelegramId(telegramId)

    suspend fun getUserById(id: UUID) = userRepo.get(id)

    suspend fun createUser(telegramId: Long, university: String, text: String) =
        userRepo.createNewUser(telegramId, text, university)

    suspend fun save(user: User) = userRepo.save(user).also {
        val otherUsers = userRepo.getAll().filter { it.telegramId != user.telegramId }
        matchingScoreService.matchUsers(user, otherUsers)
    }
}