package app.mainichi.controller

import app.mainichi.event.LikeCreatedEvent
import app.mainichi.event.LikeDeletedEvent
import app.mainichi.repository.EditLikeRepository
import app.mainichi.table.Like
import app.mainichi.repository.LikeRepository
import app.mainichi.service.EventService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.server.awaitSession
import org.springframework.web.server.ServerWebExchange

/**
 * REST controller for like data
 */
@RestController
class LikeController(
    val likeRepository: LikeRepository,
    val editLikeRepository: EditLikeRepository,
    val eventService: EventService
) {
    @GetMapping("/posts/{id}/likes")
    suspend fun getLikes(
        @PathVariable("id")
        id: Long
    ): Flow<Long> =
        likeRepository.findAllByPost(id)
            .map { it.liker }

    @PostMapping("/posts/{id}/likes")
    suspend fun likePost(
        @PathVariable("id")
        postid: Long,
        exchange: ServerWebExchange
    ): Like {
        //retrieve the logged in user
        val userid = exchange.awaitSession().attributes["id"] as String

        //like the post as the current user
        val like = editLikeRepository.save(
            Like(
                postid,
                userid.toLong()
            )
        )

        eventService.emit(LikeCreatedEvent(like))

        return like
    }

    @DeleteMapping("/posts/{id}/likes")
    suspend fun deleteLike(
        @PathVariable("id")
        postid: Long,
        exchange: ServerWebExchange
    ): Like {
        //retrieve the current user
        val userid = exchange.awaitSession().attributes["id"] as String
        val like = Like(postid, userid.toLong())

        editLikeRepository.delete(like)
        eventService.emit(LikeDeletedEvent(like))

        return like
    }

    @GetMapping("/users/{id}/likes")
    suspend fun getUserLikes(
        @PathVariable("id")
        userid: Long
    ): Flow<Long> = likeRepository.findAllByLiker(userid)
        .map { it.post }
}