package me.hawai.repo.view

import org.jetbrains.exposed.sql.Table

object FormViewTable : Table("form_views") {
    val whoViewed = uuid("who_viewed")
    val whichViewed = uuid("which_viewed")
    val liked = bool("liked")

    init {
        uniqueIndex(whoViewed, whichViewed)
    }
}