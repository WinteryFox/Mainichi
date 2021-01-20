package app.mainichi.controller

import app.mainichi.`object`.CommentCreateRequest
import app.mainichi.event.CommentCreatedEvent
import app.mainichi.repository.CommentRepository
import app.mainichi.service.EventService
import app.mainichi.service.SnowflakeService
import app.mainichi.table.Comment
import kotlinx.coroutines.flow.Flow
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.server.awaitSession
import org.springframework.web.server.ServerWebExchange

@RestController
class CommentController(
    val commentRepository: CommentRepository,
    val snowflakeService: SnowflakeService,
    val eventService: EventService
) {
    //Gets the comments from the selected post
    @GetMapping("/posts/{snowflake}/comments")
    suspend fun getComments(
        @PathVariable
        snowflake: Long
    ): Flow<Comment> =
        commentRepository.findAllByPost(snowflake)

    //Adds a comment to the selected post
    @PostMapping("/posts/{postSnowflake}/comments")
    suspend fun addComment(
        @PathVariable
        postSnowflake: Long,
        exchange: ServerWebExchange,
        @RequestBody
        commentCreateRequest: CommentCreateRequest
    ): Comment?{
        val userSnowflake = exchange.awaitSession().attributes["id"] as String

        //check if comment is empty, not set or bigger then 1024 characters
        if (commentCreateRequest.content.length >= 1024 || commentCreateRequest.content.isEmpty()){
            exchange.response.statusCode = HttpStatus.BAD_REQUEST
            return null
        }

        val comment = commentRepository.save(
            Comment(
                snowflakeService.next(),
                postSnowflake,
                userSnowflake.toLong(),
                commentCreateRequest.content,
                0
            )
        )

        eventService.emit(CommentCreatedEvent(
            comment
        ))

        return comment
    }

    //Deletes the selected comment from the selected post
    @DeleteMapping("/posts/{postSnowflake}/{commentSnowflake}")
    suspend fun deleteComment(
        @PathVariable
        postSnowflake: Long,
        @PathVariable
        commentSnowflake: Long
    ){
        commentRepository.deleteById(commentSnowflake.toString())
    }
}