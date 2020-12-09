package app.mainichi.data

import app.mainichi.BUCKET_NAME
import app.mainichi.PROJECT_NAME
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.Blob
import com.google.cloud.storage.StorageOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.*
import javax.xml.bind.DatatypeConverter

@Suppress("BlockingMethodInNonBlockingContext")
class Storage {
    private val service = StorageOptions
        .newBuilder()
        .setProjectId(PROJECT_NAME)
        .setCredentials(GoogleCredentials.fromStream(Storage::class.java.getResourceAsStream("/google_storage.json")))
        .build()
        .service
    private val bucket = service.get(BUCKET_NAME)!!

    private suspend fun put(key: String, bytes: ByteArray, type: String? = null): Blob =
        withContext(Dispatchers.IO) {
            if (type == null)
                bucket.create(key, bytes)
            else
                bucket.create(key, bytes, type)
        }

    suspend fun putWithHash(key: String, bytes: ByteArray, type: String? = null): Blob =
        withContext(Dispatchers.IO) {
            val hash = DatatypeConverter.printHexBinary(MessageDigest.getInstance("MD5").digest(bytes)).toLowerCase()

            return@withContext put(
                "$key/$hash",
                bytes,
                type
            )
        }

    suspend fun get(key: String): ByteBuffer =
        withContext(Dispatchers.IO) {
            val blob = bucket.get(key) ?: return@withContext ByteBuffer.allocate(0)
            val buffer = ByteBuffer.allocate(blob.size.toInt())
            blob.reader().read(buffer)

            return@withContext buffer
        }

    suspend fun delete(key: String) =
        withContext(Dispatchers.IO) {
            return@withContext bucket.get(key)?.delete()
        }
}