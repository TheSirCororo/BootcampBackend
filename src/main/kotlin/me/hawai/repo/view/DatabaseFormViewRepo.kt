package me.hawai.repo.view

import me.hawai.model.view.data.FormView
import me.hawai.plugin.sql
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.upsert
import java.util.UUID

object DatabaseFormViewRepo : FormViewRepo {
    override suspend fun markAsViewed(whoViewed: UUID, whichViewed: UUID): Unit = sql {
        FormViewTable.upsert(FormViewTable.whoViewed, FormViewTable.whichViewed, onUpdate = {
            it[FormViewTable.liked] = false
        }) {
            it[FormViewTable.whoViewed] = whoViewed
            it[FormViewTable.whichViewed] = whichViewed
            it[liked] = false
        }
    }

    override suspend fun markAsLiked(whoViewed: UUID, whichViewed: UUID): Unit = sql {
        FormViewTable.upsert(FormViewTable.whoViewed, FormViewTable.whichViewed, onUpdate = {
            it[FormViewTable.liked] = true
        }) {
            it[FormViewTable.whoViewed] = whoViewed
            it[FormViewTable.whichViewed] = whichViewed
            it[liked] = true
        }
    }

    override suspend fun getFormView(
        whoViewed: UUID,
        whichViewed: UUID
    ): FormView? = sql {
        FormViewTable.selectAll().where { (FormViewTable.whoViewed eq whoViewed) and (FormViewTable.whichViewed eq whichViewed) }.singleOrNull()?.let {
            FormView(
                it[FormViewTable.whoViewed],
                it[FormViewTable.whichViewed],
                it[FormViewTable.liked]
            )
        }
    }
}