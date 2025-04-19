package me.hawai.service

import me.hawai.repo.view.FormViewRepo
import java.util.UUID

class FormViewService(private val userService: UserService, private val formViewRepo: FormViewRepo) {
    suspend fun markAsViewed(whoViewed: UUID, whichViewed: UUID) = formViewRepo.markAsViewed(whoViewed, whichViewed)

    suspend fun markAsLiked(whoViewed: UUID, whichViewed: UUID) = formViewRepo.markAsLiked(whoViewed, whichViewed)

    suspend fun getFormView(whoViewed: UUID, whichViewed: UUID) = formViewRepo.getFormView(whoViewed, whichViewed)
}