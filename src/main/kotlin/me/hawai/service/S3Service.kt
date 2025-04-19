package me.hawai.service

import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.utils.io.*
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.http.apache.ApacheHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.net.URI
import java.util.*

class S3Service(application: Application) {
    private val s3Endpoint = application.environment.config.tryGetString("image.s3_endpoint") ?: ""
    private val s3KeyId = application.environment.config.tryGetString("image.s3_key_id") ?: ""
    private val s3KeyValue = application.environment.config.tryGetString("image.s3_key_value") ?: ""
    private val s3Bucket = application.environment.config.tryGetString("image.s3_bucket") ?: ""
    private val s3Client = S3Client.builder()
        .endpointOverride(URI.create(s3Endpoint))
        .region(Region.EU_NORTH_1)
        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(s3KeyId, s3KeyValue)))
        .httpClientBuilder(ApacheHttpClient.builder())
        .build()

    init {
        s3Client.listBuckets().buckets().find { it.name() == s3Bucket } ?: error("Бакет с именем $s3Bucket не найден!")
    }

    suspend fun uploadImage(
        multiPartData: PartData.FileItem,
        id: UUID
    ): String? {
        val fileName = multiPartData.originalFileName ?: return null
        if (!allowedFileExtensions.any { fileName.endsWith(it) }) return null
        s3Client.putObject(
            PutObjectRequest.builder().bucket(s3Bucket).key(id.toString()).build(),
            RequestBody.fromBytes(multiPartData.provider().toByteArray())
        )

        return fileName
    }

    fun loadImage(id: UUID) =
        s3Client.getObject(GetObjectRequest.builder().bucket(s3Bucket).key(id.toString()).build()).readBytes()

    fun deleteImage(id: UUID) {
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(s3Bucket).bucket(id.toString()).build())
    }

    companion object {
        val allowedFileExtensions = listOf(".png", ".bmp", ".jpg", ".jpeg", ".svg", ".webp", ".gif")
    }
}