package me.hawai.repo

interface CrudRepo<ID, Entity> {
    suspend fun Entity.getId(): ID

    suspend fun save(entity: Entity)

    suspend fun saveAll(entities: Iterable<Entity>)

    suspend fun delete(id: ID)

    suspend fun get(id: ID): Entity?
}