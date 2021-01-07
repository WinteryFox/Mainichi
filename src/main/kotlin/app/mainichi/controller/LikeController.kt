package app.mainichi.controller

import app.mainichi.table.Like
import app.mainichi.repository.LikeRepository
import kotlinx.coroutines.flow.Flow
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.server.awaitSession
import org.springframework.web.server.ServerWebExchange

/**
 * REST controller for like data
 */
@RestController
class LikeController(
    val likeRepository: LikeRepository
    ) {
    /**
     * Request all like data from a specific user
     */
    @GetMapping("/users/{userSnowflake}/likes")
    suspend fun getLikesFromUser(
        exchange: ServerWebExchange,
        @PathVariable("userSnowflake")
        userSnowflake: Long
    ): Flow<Like> = likeRepository.findAllByLiker(userSnowflake)

    /**
     * Creates a like and attaches it to the current logged in user and selected post
     */
    @PostMapping("/posts/like/{postSnowflake}")
    suspend fun createLike(
        exchange: ServerWebExchange,
        @PathVariable("postSnowflake")
        postSnowflake: Long
    ): Like {
        val userSnowflake = exchange.awaitSession().attributes["SNOWFLAKE"] as String

        return likeRepository.save(
            Like(
                userSnowflake.toLong(),
                postSnowflake
            )
        )
    }
}