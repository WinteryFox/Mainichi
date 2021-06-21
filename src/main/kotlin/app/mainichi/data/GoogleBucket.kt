package app.mainichi.data

import app.mainichi.BUCKET_NAME
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.Blob
import com.google.cloud.storage.StorageOptions
import org.springframework.stereotype.Service
import java.security.MessageDigest
import javax.xml.bind.DatatypeConverter

@Service
class GoogleBucket : Bucket {
    private val options = StorageOptions
        .newBuilder()
        .setCredentials(GoogleCredentials.getApplicationDefault())
        .build()
    private val service = options.service
    private val bucket = service.get(BUCKET_NAME)!!

    override suspend fun put(key: String, bytes: ByteArray, type: String?): Blob =
        if (type == null)
            bucket.create(key, bytes)
        else
            bucket.create(key, bytes, type)

    override suspend fun putWithHash(key: String, bytes: ByteArray, type: String?): Blob =
        put(
            "$key/${DatatypeConverter.printHexBinary(MessageDigest.getInstance("MD5").digest(bytes)).lowercase()}",
            bytes,
            type
        )

    override suspend fun get(key: String): Blob? =
        bucket.get(key)

    override suspend fun delete(key: String): Boolean =
        bucket.get(key).delete()
}