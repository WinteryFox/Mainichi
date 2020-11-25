package app.mainichi.controller

import app.mainichi.objects.Post
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CookieValue

@Controller
class FeedController {
    @Autowired
    private lateinit var messagingTemplate: SimpMessagingTemplate

    @MessageMapping("/feed")
    fun getFeed(@CookieValue token: String) {
        messagingTemplate.convertAndSendToUser(
                "0",
                "/feed/receive",
                Post(0, 0, "Hello")
        )
    }
}