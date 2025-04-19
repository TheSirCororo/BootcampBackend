package me.hawai.repo.view

import me.hawai.model.view.data.FormView
import java.util.UUID

interface FormViewRepo {
    suspend fun markAsViewed(whoViewed: UUID, whichViewed: UUID)

    suspend fun markAsLiked(whoViewed: UUID, whichViewed: UUID)

    suspend fun getFormView(whoViewed: UUID, whichViewed: UUID): FormView?
}