package me.hawai.repo.matching

import me.hawai.repo.user.UserTable
import org.jetbrains.exposed.sql.Table

object MatchingScoreTable : Table("matching_scores") {
    val firstUser = reference("first_user", UserTable)
    val secondUser = reference("second_user", UserTable)
    val score = float("score")
}
