package app.mainichi.controller

import app.mainichi.`object`.FullPost
import app.mainichi.repository.CommentRepository
import app.mainichi.table.Post
import app.mainichi.repository.FullPostRepository
import app.mainichi.repository.PostRepository
import org.springframework.http.HttpStatus
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
    val fullPostRepository: FullPostRepository
) {
    /**
     * Request all post data
     */
    @GetMapping("/posts")
    suspend fun getAllPosts() = fullPostRepository.findAll()

    @GetMapping("/posts/{snowflake}")
    suspend fun getPost(
        @PathVariable
        snowflake: Long
    ) = fullPostRepository.findBySnowflake(snowflake)

    /**
     * Request all post data from a specific user
     */
    @GetMapping("/users/{snowflake}/posts")
    suspend fun getPostsFromUser(
        @PathVariable("snowflake")
        userSnowflake: Long
    ) = fullPostRepository.findAllByAuthor(userSnowflake)

    /**
     * Creates a post and attaches it to the current logged in user
     */
    @PostMapping("/posts")
    suspend fun createPost(
        exchange: ServerWebExchange
    ): Post? {
        val userSnowflake = exchange.awaitSession().attributes["SNOWFLAKE"] as String
        val form = exchange.awaitFormData().toSingleValueMap().toMap()

        val content = form["content"]
        if (content == null || content.length >= 1024 || content.length <= 16) {
            exchange.response.statusCode = HttpStatus.BAD_REQUEST
            return null
        }

        return postRepository.save(Post(0, userSnowflake.toLong(), content))
    }

    /**
     * Updates selected post
     */
    @PostMapping("/posts/{snowflake}")
    suspend fun updatePost(
        exchange: ServerWebExchange,
        @PathVariable("snowflake")
        postSnowflake: Long,
        ): Post? {
        val post = postRepository.findById(postSnowflake.toString())
        val form = exchange.awaitFormData().toSingleValueMap().toMap()
        val content = form["content"]

        if (post == null || content == null || content.length >= 1024 || content.length <= 16) {
            exchange.response.statusCode = HttpStatus.BAD_REQUEST
            return null
        }

        if (exchange.awaitSession().attributes["SNOWFLAKE"] as String != post.author.toString()){
            exchange.response.statusCode = HttpStatus.FORBIDDEN
            return null
        }

        return postRepository.save(
            Post(
                post.snowflake,
                post.author,
                content
            )
        )
    }
}