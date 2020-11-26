package app.mainichi

import app.mainichi.controller.WebSocketController
import kotlinx.coroutines.reactive.collect
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class WebSocketClientTest(@Autowired val webSocketController: WebSocketController) {
    @Test
    fun testStream(): Unit = runBlocking {
        webSocketController.getFeed()
            .collect {
                println(it.content)
            }
    }
}