package app.mainichi.controller

import app.mainichi.`object`.ShortPost
import app.mainichi.event.PostCreateEvent
import app.mainichi.repository.CommentRepository
import app.mainichi.repository.PostRepository
import app.mainichi.table.Post
import app.mainichi.repository.ShortPostRepository
import app.mainichi.service.EventService
import app.mainichi.service.SnowflakeService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.server.awaitFormData
import org.springframework.web.reactive.server.awaitSession
import org.springframework.web.server.ServerWebExchange

/**
 * REST controller for post data
 */
@RestController
class PostController(
    val postRepository: PostRepository,
    val shortPostRepository: ShortPostRepository,
    val commentRepository: CommentRepository,
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
    @PostMapping(
        "/posts",
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    suspend fun createPost(
        exchange: ServerWebExchange
    ): Post? {
        val userid = exchange.awaitSession().attributes["id"] as String
        val content = exchange.awaitFormData().toSingleValueMap().toMap()["content"]
        if (content == null || content.length >= 1024 || content.isEmpty()) {
            exchange.response.statusCode = HttpStatus.BAD_REQUEST
            return null
        }

        val post = postRepository.save(
            Post(
                snowflakeService.next(),
                userid.toLong(),
                content,
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
    @PostMapping(
        "/posts/{id}",
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    suspend fun updatePost(
        exchange: ServerWebExchange,
        @PathVariable("id")
        id: Long,
    ): Post? {
        val post = postRepository.findById(id.toString())
        val form = exchange.awaitFormData().toSingleValueMap().toMap()
        val content = form["content"]

        if (post == null || content == null || content.length >= 1024 || content.length <= 16) {
            exchange.response.statusCode = HttpStatus.BAD_REQUEST
            return null
        }

        if (exchange.awaitSession().attributes["id"] as String != post.author.toString()) {
            exchange.response.statusCode = HttpStatus.FORBIDDEN
            return null
        }

        return postRepository.save(
            Post(
                post.id,
                post.author,
                content,
                post.version
            )
        )
    }
}