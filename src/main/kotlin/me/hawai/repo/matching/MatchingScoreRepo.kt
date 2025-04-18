package me.hawai.repo.matching

import me.hawai.model.matching.data.MatchingScore
import java.util.UUID

interface MatchingScoreRepo {
    suspend fun saveScore(firstUser: UUID, secondUser: UUID, score: Float): MatchingScore

    suspend fun findScore(firstUser: UUID, secondUser: UUID): MatchingScore?

    suspend fun getSortedScores(user: UUID): List<MatchingScore>
}