package me.hawai.model.view.data

import java.util.UUID

data class FormView(
    val whoViewed: UUID,
    val whichViewed: UUID,
    val liked: Boolean
)