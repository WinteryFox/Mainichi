package app.mainichi.controller

import app.mainichi.event.LikeCreatedEvent
import app.mainichi.event.LikeDeletedEvent
import app.mainichi.repository.EditLikeRepository
import app.mainichi.table.Like
import app.mainichi.repository.LikeRepository
import app.mainichi.service.EventService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.security.Principal

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
        principal: Principal,
        @PathVariable("id")
        id: Long
    ): Like {
        val like = editLikeRepository.save(
            Like(
                id,
                principal.name.toLong()
            )
        )
        eventService.emit(LikeCreatedEvent(like))

        return like
    }

    @DeleteMapping("/posts/{id}/likes")
    suspend fun deleteLike(
        principal: Principal,
        @PathVariable("id")
        id: Long
    ): Like {
        val like = likeRepository.findByPostAndLiker(id, principal.name.toLong()) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

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