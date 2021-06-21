package app.mainichi.data

import com.google.cloud.storage.Blob
import java.nio.ByteBuffer

interface Bucket {
    suspend fun put(key: String, bytes: ByteArray, type: String? = null): Blob

    suspend fun putWithHash(key: String, bytes: ByteArray, type: String? = null): Blob

    suspend fun get(key: String): Blob?

    suspend fun delete(key: String): Boolean
}

fun Blob.toBuffer(): ByteBuffer {
    val buffer = ByteBuffer.allocate(this.size.toInt())
    this.reader().read(buffer)

    return buffer
}