package app.mainichi.controller

import app.mainichi.objects.Post
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux

@Controller
class WebSocketController {
    @Autowired
    private lateinit var client: DatabaseClient

    @MessageMapping("feed")
    fun getFeed(): Flux<Post> =
        client
            .sql("SELECT * FROM posts WHERE snowflake = :snowflake")
            .bind("snowflake", 0)
            .map { row, _ ->
                Post(
                    row["snowflake"] as Long,
                    row["author"] as Long,
                    row["content"] as String
                )
            }
            .all()
}