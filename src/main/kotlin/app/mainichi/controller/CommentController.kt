package app.mainichi.controller

import app.mainichi.ErrorCode
import app.mainichi.component.ResponseStatusCodeException
import app.mainichi.request.CommentCreateRequest
import app.mainichi.event.CommentCreateEvent
import app.mainichi.repository.CommentRepository
import app.mainichi.service.EventService
import app.mainichi.service.SnowflakeService
import app.mainichi.table.Comment
import kotlinx.coroutines.flow.Flow
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
class CommentController(
    val commentRepository: CommentRepository,
    val snowflakeService: SnowflakeService,
    val eventService: EventService
) {
    @GetMapping("/posts/{id}/comments")
    suspend fun getComments(
        @PathVariable
        id: Long
    ): Flow<Comment> = commentRepository.findAllByPost(id)

    @PostMapping("/posts/{id}/comments")
    suspend fun addComment(
        principal: Principal,
        @PathVariable
        id: Long,
        @RequestBody
        commentCreateRequest: CommentCreateRequest
    ): Comment {
        if (commentCreateRequest.content.length >= 1024 || commentCreateRequest.content.isEmpty())
            throw ResponseStatusCodeException(ErrorCode.INVALID_COMMENT)

        val comment = commentRepository.save(
            Comment(
                snowflakeService.next(),
                id,
                principal.name.toLong(),
                commentCreateRequest.content,
                0
            )
        )

        eventService.emit(CommentCreateEvent(comment))

        return comment
    }

    @DeleteMapping("/comments/{id}")
    suspend fun deleteComment(
        @PathVariable
        id: String
    ) {
        commentRepository.deleteById(id)
    }
}