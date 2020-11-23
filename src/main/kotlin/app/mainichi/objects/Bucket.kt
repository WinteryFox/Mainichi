package app.mainichi.objects

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.PutObjectResult
import java.io.File
import java.security.MessageDigest
import java.util.*

class Bucket {
    private val client = AmazonS3ClientBuilder.standard()
        .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration("s3.nl-ams.scw.cloud", "nl-ams"))
        .withCredentials(
            AWSStaticCredentialsProvider(
                BasicAWSCredentials(
                    System.getenv("bucket-access-key"),
                    System.getenv("bucket-secret-key")
                )
            )
        )
        .build()

    fun upload(key: String, file: File): PutObjectResult =
        client.putObject("mainichi", key, file)

    fun uploadAvatar(file: File) {
        val hash =
            Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(file.inputStream().readAllBytes()))

        println(hash)
        upload("avatars/$hash", file)
    }
}