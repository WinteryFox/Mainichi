package app.mainichi.controller

import app.mainichi.repository.EditLikeRepository
import app.mainichi.table.Like
import app.mainichi.repository.LikeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.server.awaitSession
import org.springframework.web.server.ServerWebExchange

/**
 * REST controller for like data
 */
@RestController
class LikeController(
    val likeRepository: LikeRepository,
    val editLikeRepository: EditLikeRepository
    ) {
    @GetMapping("/posts/{snowflake}/likes")
    suspend fun getLikes(
        @PathVariable("snowflake")
        snowflake: Long
    ): Flow<Long> =
        likeRepository.findAllByPost(snowflake)
            .map { it.liker }

    @PostMapping("/posts/{snowflake}/likes")
    suspend fun likePost(
        @PathVariable("snowflake")
        postSnowflake: Long,
        exchange: ServerWebExchange
    ): Like {
        //retrieve the logged in user
        val userSnowflake = exchange.awaitSession().attributes["SNOWFLAKE"] as String

        //like the post as the current user
        return editLikeRepository.save(
            Like(
                postSnowflake,
                userSnowflake.toLong()
            )
        )
    }

    @DeleteMapping("/posts/{snowflake}/likes")
    suspend fun deleteLike(
        @PathVariable("snowflake")
        postSnowflake: Long,
        exchange: ServerWebExchange
    ){
        //retrieve the current user
        val userSnowflake = exchange.awaitSession().attributes["SNOWFLAKE"] as String

        editLikeRepository.delete(
            Like(
                postSnowflake,
                userSnowflake.toLong()
            )
        )
    }

    @GetMapping("/users/{snowflake}/likes")
    suspend fun getUserLikes(
        @PathVariable("snowflake")
        userSnowflake: Long
    ): Flow<Long> = likeRepository.findAllByLiker(userSnowflake)
        .map { it.post }
}