package me.hawai.repo.image

import me.hawai.model.image.data.Image
import me.hawai.repo.CrudRepo
import java.util.*

interface ImageRepo : CrudRepo<UUID, Image> {
    suspend fun createNewImage(id: UUID, name: String): Image
}