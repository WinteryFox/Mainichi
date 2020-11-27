package app.mainichi

import app.mainichi.controller.WebSocketController
import app.mainichi.objects.Post
import io.rsocket.metadata.WellKnownAuthType
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
import org.springframework.security.config.annotation.rsocket.RSocketSecurity
import org.springframework.security.rsocket.metadata.BearerTokenMetadata
import org.springframework.security.rsocket.metadata.SimpleAuthenticationEncoder
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata
import org.springframework.util.MimeType
import org.springframework.util.MimeTypeUtils
import java.net.URI

@SpringBootTest
class WebSocketClientTest(@Autowired val webSocketController: WebSocketController) {
    @Test
    fun testStream(): Unit = runBlocking {
        RSocketRequester.builder()
                .rsocketStrategies(
                        RSocketStrategies.builder()
                                .encoders {
                                    it.add(Jackson2CborEncoder())
                                    it.add(SimpleAuthenticationEncoder())
                                }
                                .decoders { it.add(Jackson2CborDecoder()) }
                                .build()
                )
                .connectWebSocketAndAwait(URI("ws://localhost:8181/gateway"))
                .route("feed")
                .metadata(
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