package app.mainichi

import app.mainichi.objects.Post
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.codec.cbor.Jackson2CborDecoder
import org.springframework.http.codec.cbor.Jackson2CborEncoder
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.messaging.rsocket.connectWebSocketAndAwait
import org.springframework.messaging.rsocket.retrieveFlux
import java.net.URI

@SpringBootTest
class WebSocketClientTest {
    @Test
    fun testStream(): Unit = runBlocking {
        RSocketRequester.builder()
            .rsocketStrategies(
                RSocketStrategies.builder()
                    .encoders { it.add(Jackson2CborEncoder()) }
                    .decoders { it.add(Jackson2CborDecoder()) }
                    .build()
            )
            .connectWebSocketAndAwait(URI("ws://localhost:8181/gateway"))
            .route("feed")
            .retrieveFlux<Post>()
            .map {
                println(it.content)
                it
            }
            .blockLast()
    }
}