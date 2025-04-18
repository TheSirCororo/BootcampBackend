package me.hawai.model.matching.data

import java.util.UUID

data class MatchingScore(
    val firstUser: UUID,
    val secondUser: UUID,
    val score: Float
)
