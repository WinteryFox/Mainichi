package app.mainichi.session

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class AttributeService {
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> fromBlob(attributes: ByteArray): T =
        ByteArrayInputStream(attributes).use { bais ->
            ObjectInputStream(bais).use { ois ->
                ois.readObject() as T
            }
        }

    fun toBlob(attributes: Any): ByteArray =
        ByteArrayOutputStream(1024).use { baos ->
            ObjectOutputStream(baos).use { oos ->
                oos.writeObject(attributes)
                baos.toByteArray()
            }
        }
}