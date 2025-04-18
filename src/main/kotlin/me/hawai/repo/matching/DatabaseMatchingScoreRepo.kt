package me.hawai.repo.matching

import me.hawai.model.matching.data.MatchingScore
import me.hawai.plugin.sql
import org.jetbrains.exposed.sql.*
import java.util.*

object DatabaseMatchingScoreRepo : MatchingScoreRepo {
    override suspend fun saveScore(
        firstUser: UUID,
        secondUser: UUID,
        scoreValue: Float
    ): MatchingScore = sql {
        val score = findScore(firstUser, secondUser)
        if (score != null) {
            MatchingScoreTable.update(where = {
                ((MatchingScoreTable.firstUser eq firstUser) and (
                        MatchingScoreTable.secondUser eq secondUser)) or ((MatchingScoreTable.secondUser eq firstUser) and (MatchingScoreTable.firstUser eq secondUser))
            }) {
                it[this.score] = scoreValue
            }

            findScore(firstUser, secondUser)!!
        } else {
            MatchingScoreTable.insert {
                it[this.firstUser] = firstUser
                it[this.secondUser] = secondUser
                it[this.score] = scoreValue
            }.let {
                MatchingScore(
                    it[MatchingScoreTable.firstUser].value,
                    it[MatchingScoreTable.secondUser].value,
                    it[MatchingScoreTable.score]
                )
            }
        }
    }

    override suspend fun findScore(
        firstUser: UUID,
        secondUser: UUID
    ): MatchingScore? = sql {
        MatchingScoreTable.selectAll().where {
            ((MatchingScoreTable.firstUser eq firstUser) and (
                    MatchingScoreTable.secondUser eq secondUser)) or ((MatchingScoreTable.secondUser eq firstUser) and (MatchingScoreTable.firstUser eq secondUser))
        }
            .singleOrNull()?.let {
                MatchingScore(
                    it[MatchingScoreTable.firstUser].value,
                    it[MatchingScoreTable.secondUser].value,
                    it[MatchingScoreTable.score]
                )
            }
    }

    override suspend fun getSortedScores(user: UUID): List<MatchingScore> = sql {
        MatchingScoreTable.selectAll()
            .where { (MatchingScoreTable.firstUser eq user) or (MatchingScoreTable.secondUser eq user) }.orderBy(
                MatchingScoreTable.score, SortOrder.ASC
            ).map {
                MatchingScore(
                    it[MatchingScoreTable.firstUser].value,
                    it[MatchingScoreTable.secondUser].value,
                    it[MatchingScoreTable.score]
                )
            }
    }
}