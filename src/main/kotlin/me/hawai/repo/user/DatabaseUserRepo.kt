package me.hawai.repo.user

import me.hawai.model.user.data.User
import me.hawai.plugin.sql
import me.hawai.util.deleteById
import me.hawai.util.getById
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert
import java.util.*

object DatabaseUserRepo : UserRepo {
    override suspend fun User.getId(): UUID = id

    override suspend fun getByTelegramId(telegramId: Long): User? = sql {
        UserTable.selectAll().where {
            UserTable.telegramId eq telegramId
        }.singleOrNull()?.let {
            User(
                it[UserTable.id].value,
                it[UserTable.telegramId],
                it[UserTable.name],
                it[UserTable.university],
                it[UserTable.text],
                it[UserTable.interests],
                it[UserTable.photoId]?.value
            )
        }
    }

    override suspend fun save(entity: User): Unit = sql {
        UserTable.upsert(UserTable.telegramId, onUpdate = {
            it[UserTable.university] = entity.university
            it[UserTable.text] = entity.text
            it[UserTable.interests] = entity.interests
            it[UserTable.name] = entity.name
            it[UserTable.photoId] = entity.photoId
        }) {
            it[telegramId] = entity.telegramId
            it[university] = entity.university
            it[text] = entity.text
            it[interests] = entity.interests
            it[name] = entity.name
            it[photoId] = entity.photoId
        }
    }

    override suspend fun saveAll(entities: Iterable<User>) = sql {
        entities.forEach {
            save(it)
        }
    }

    override suspend fun delete(id: UUID): Unit = sql {
        UserTable.deleteById(id)
    }

    override suspend fun get(id: UUID): User? = sql {
        UserTable.getById(id)?.let {
            User(
                it[UserTable.id].value,
                it[UserTable.telegramId],
                it[UserTable.name],
                it[UserTable.university],
                it[UserTable.text],
                it[UserTable.interests],
                it[UserTable.photoId]?.value
            )
        }
    }

    override suspend fun getAll(): List<User> = sql {
        UserTable.selectAll().map {
            User(
                it[UserTable.id].value,
                it[UserTable.telegramId],
                it[UserTable.name],
                it[UserTable.university],
                it[UserTable.text],
                it[UserTable.interests],
                it[UserTable.photoId]?.value
            )
        }
    }

}