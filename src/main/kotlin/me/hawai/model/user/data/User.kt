package me.hawai.model.user.data

import java.util.UUID

data class User(
    val id: UUID,
    val telegramId: Long,
    val university: String,
    val text: String
)