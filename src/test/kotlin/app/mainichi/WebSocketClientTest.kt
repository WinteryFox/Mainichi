package app.mainichi

import app.mainichi.table.Post
import io.rsocket.metadata.WellKnownMimeType
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.codec.cbor.Jackson2CborDecoder
import org.springframework.http.codec.cbor.Jackson2CborEncoder
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.messaging.rsocket.connectWebSocketAndAwait
import org.springframework.messaging.rsocket.retrieveFlux
import org.springframework.security.rsocket.metadata.SimpleAuthenticationEncoder
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata
import org.springframework.util.MimeTypeUtils
import java.net.URI

@SpringBootTest
class WebSocketClientTest(@Autowired val webSocketController: WebSocketController) {
    @ExperimentalUnsignedTypes
    @Test
    fun testStream(): Unit = runBlocking {
        RSocketRequester.builder()
            .rsocketStrategies(
                RSocketStrategies.builder()
                    .encoders {
                        it.add(Jackson2CborEncoder())
                        it.add(SimpleAuthenticationEncoder())
                        //it.add(ByteBufferEncoder())
                    }
                    .decoders { it.add(Jackson2CborDecoder()) }
                    .build()
            )
            .connectWebSocketAndAwait(URI("ws://localhost:8181/gateway"))
            .route("feed")
            .metadata(
                    /*ByteBuffer.wrap(ubyteArrayOf(
                    0xfeU, 0x00U, 0x00U, 0x05U, 0x04U, 0x66U, 0x65U, 0x65U, 0x64U, 0x22U, 0x6dU, 0x65U, 0x73U, 0x73U, 0x61U, 0x67U,
                    0x65U, 0x2fU, 0x78U, 0x2eU, 0x72U, 0x73U, 0x6fU, 0x63U, 0x6bU, 0x65U, 0x74U, 0x2eU, 0x61U, 0x75U, 0x74U, 0x68U,
                    0x65U, 0x6eU, 0x74U, 0x69U, 0x63U, 0x61U, 0x74U, 0x69U, 0x6fU, 0x6eU, 0x2eU, 0x76U, 0x30U, 0x00U, 0x00U, 0x0cU,
                    0x80U, 0x00U, 0x04U, 0x74U, 0x65U, 0x73U, 0x74U, 0x31U, 0x32U, 0x33U, 0x34U, 0x35U
                    ).toByteArray()),*/
                UsernamePasswordMetadata("test", "12345"),
                MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.string)
            )
            .retrieveFlux<Post>()
            .map {
                println(it.content)
                it
            }
            .blockLast()
        /*webSocketController.getFeed()
            .collect {
                println(it.content)
            }*/
    }
}