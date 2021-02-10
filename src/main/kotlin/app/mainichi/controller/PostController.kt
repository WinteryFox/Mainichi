package app.mainichi.controller

import app.mainichi.ErrorCode
import app.mainichi.`object`.ShortPost
import app.mainichi.component.ResponseStatusCodeException
import app.mainichi.event.PostCreateEvent
import app.mainichi.repository.PostRepository
import app.mainichi.table.Post
import app.mainichi.repository.ShortPostRepository
import app.mainichi.request.PostCreateRequest
import app.mainichi.service.EventService
import app.mainichi.service.SnowflakeService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.security.Principal

/**
 * REST controller for post data
 */
@RestController
class PostController(
    val postRepository: PostRepository,
    val shortPostRepository: ShortPostRepository,
    val eventService: EventService,
    val snowflakeService: SnowflakeService
) {
    /**
     * Request all post data
     */
    @GetMapping("/posts")
    fun getAllPosts() = shortPostRepository.findAll()

    @GetMapping("/posts/{id}")
    suspend fun getPost(
        @PathVariable
        id: Long
    ) = shortPostRepository.findByid(id)

    /**
     * Request all post data from a specific user
     */
    @GetMapping("/users/{id}/posts")
    fun getPostsFromUser(
        @PathVariable("id")
        userid: Long
    ) = shortPostRepository.findAllByAuthor(userid)

    /**
     * Creates a post and attaches it to the current logged in user
     */
    @PostMapping("/posts")
    suspend fun createPost(
        principal: Principal,
        @RequestBody
        request: PostCreateRequest
    ): Post {
        if (request.content.length >= 1024 || request.content.isEmpty())
            throw ResponseStatusCodeException(ErrorCode.INVALID_POST)

        val post = postRepository.save(
            Post(
                snowflakeService.next(),
                principal.name.toLong(),
                request.content,
                0
            )
        )
        eventService.emit(
            PostCreateEvent(
                ShortPost(
                    post.id,
                    post.author,
                    post.content,
                    0,
                    0,
                    post.version
                )
            )
        )
        return post
    }

    /**
     * Updates selected post
     */
    @PostMapping("/posts/{id}")
    suspend fun updatePost(
        principal: Principal,
        @PathVariable("id")
        id: String,
        @RequestBody
        request: PostCreateRequest
    ): Post? {
        val post = postRepository.findById(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        if (request.content.length >= 1024 || request.content.isEmpty())
            throw ResponseStatusCodeException(ErrorCode.INVALID_POST)

        if (principal.name != post.author.toString())
            throw ResponseStatusException(HttpStatus.FORBIDDEN)

        return postRepository.save(
            Post(
                post.id,
                post.author,
                request.content,
                post.version
            )
        )
    }
}