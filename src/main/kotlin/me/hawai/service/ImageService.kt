package me.hawai.service

import io.ktor.http.content.*
import me.hawai.repo.image.ImageRepo
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import java.io.FileNotFoundException
import java.util.*

class ImageService(private val imageRepo: ImageRepo, private val s3Service: S3Service) {
    val maxImageSize = 512L * 1024L // 512 KB

    suspend fun uploadImage(multipart: MultiPartData, id: UUID): String? {
        val part = multipart.readPart() ?: return null
        val fileName: String?
        if (part is PartData.FileItem) {
            fileName = s3Service.uploadImage(part, id)
        } else {
            return null
        }

        part.dispose()

        return fileName
    }

    suspend fun saveImage(id: UUID, name: String) = imageRepo.createNewImage(id, name)

    suspend fun getImage(id: UUID) = imageRepo.get(id)

    suspend fun deleteImage(id: UUID) {
        try {
            s3Service.deleteImage(id)
        } catch (_: Exception) {
        }
        imageRepo.delete(id)
    }

    fun loadImageBytes(id: UUID) = try {
        s3Service.loadImage(id)
    } catch (_: NoSuchKeyException) {
        null
    } catch (_: FileNotFoundException) {
        null
    }
}