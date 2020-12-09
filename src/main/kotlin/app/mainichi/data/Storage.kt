package app.mainichi.data

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.Bucket
import com.google.cloud.storage.StorageOptions
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.URI
import java.nio.ByteBuffer
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.*
import javax.xml.bind.DatatypeConverter

class Storage {
    private val service = StorageOptions
        .newBuilder()
        .setProjectId("mainichi")
        .setCredentials(GoogleCredentials.fromStream(Storage::class.java.getResourceAsStream("/google_storage.json")))
        .build()
        .service
    private val bucket = service.get("mainichi")

    fun put(key: String, bytes: ByteArray, type: String? = null) {
        if (type == null)
            bucket.create(key, bytes)
        else
            bucket.create(key, bytes, type)
    }

    fun putWithHash(key: String, stream: InputStream, type: String? = null): String {
        stream.use {
            val bytes = it.readBytes()
            val hash = DatatypeConverter.printHexBinary(MessageDigest.getInstance("MD5").digest(bytes)).toLowerCase()
            val k = "$key/$hash"

            put(k, bytes, type)
            return hash
        }
    }

    fun get(key: String): ByteBuffer {
        val buffer = ByteBuffer.allocate(128 * 1000)
        bucket.get(key)?.reader()?.read(buffer)

        return buffer
    }

    fun delete(key: String) = bucket.get(key)?.delete()
}