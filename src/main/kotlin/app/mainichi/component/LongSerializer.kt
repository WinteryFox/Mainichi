package app.mainichi.component

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.springframework.boot.jackson.JsonComponent

@JsonComponent
class LongSerializer : StdSerializer<Long>(Long::class.java) {
    override fun serialize(value: Long, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(value.toString())
    }
}