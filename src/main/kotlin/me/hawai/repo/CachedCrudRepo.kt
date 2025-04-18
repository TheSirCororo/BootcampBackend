package me.hawai.repo

import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.asLoadingCache
import com.sksamuel.aedile.core.expireAfterAccess
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

open class CachedCrudRepo<ID, Entity>(
    protected val backedRepo: CrudRepo<ID, Entity>,
    private val expiration: Duration = 15.minutes
) : CrudRepo<ID, Entity> {
    protected val cacheById = newCache<ID, Entity?> {
        backedRepo.get(it)
    }

    protected fun <K, V> newCache(loader: suspend (K) -> V) =
        Caffeine.newBuilder().expireAfterAccess(expiration).asLoadingCache<K, V> { loader(it) }

    override suspend fun Entity.getId() = with(backedRepo) { getId() }

    override suspend fun save(entity: Entity) {
        backedRepo.save(entity)
        cacheById.put(entity.getId(), entity)
    }

    override suspend fun saveAll(entities: Iterable<Entity>) {
        backedRepo.saveAll(entities)
        entities.forEach {
            cacheById.put(it.getId(), it)
        }
    }

    override suspend fun delete(id: ID) {
        backedRepo.delete(id)
        cacheById.invalidate(id)
    }

    override suspend fun get(id: ID): Entity? = cacheById.get(id)
}