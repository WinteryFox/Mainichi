package app.mainichi.repository

import app.mainichi.`object`.FullPost
import app.mainichi.`object`.ShortPost
import app.mainichi.table.Comment
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.reactive.asFlow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.convert.R2dbcConverter
import org.springframework.r2dbc.core.DatabaseClient

class FullPostRepository(
    private val client: DatabaseClient,
    private val converter: R2dbcConverter
) {
    @Autowired
    private lateinit var postRepository: PostRepository
    @Autowired
    private lateinit var likeRepository: LikeRepository
    @Autowired
    private lateinit var commentRepository: CommentRepository

    /*fun findBySnowflake(snowflake: Long) = // TODO: Find a way to make this work instead of executing 3 queries
        client.sql(
            """
                SELECT p.snowflake,
                       p.author,
                       p.content,
                       array_remove(array_agg(l.liker), NULL)     AS likes,
                       array_remove(array_agg(c), NULL) AS comments
                FROM posts p
                         LEFT JOIN likes l ON p.snowflake = l.post
                         LEFT JOIN comments c on p.snowflake = c.post
                WHERE p.snowflake = :snowflake
                GROUP BY p.snowflake
            """.trimIndent()
        )
            .bind("snowflake", snowflake)
            .map { row, metadata -> converter.read(FullPost::class.java, row, metadata) }
            .all()
            .asFlow()*/
    suspend fun findBySnowflake(snowflake: Long): FullPost? {
        val post = postRepository.findAllById(setOf(snowflake.toString())).singleOrNull() ?: return null
        val likes = likeRepository.findAllByPost(snowflake)
        val comments = commentRepository.findAllByPost(snowflake)

        return FullPost(
            post,
            likes.toTypedArray(),
            comments.toTypedArray()
        )
    }

    fun findAll() =
        client.sql(
            """
            SELECT p.snowflake, p.author, p.content, count(l.liker) like_count, count(c.commenter) comment_count
            FROM posts p
                     LEFT JOIN likes l ON p.snowflake = l.post
                     LEFT JOIN comments c on p.snowflake = c.post
            GROUP BY p.snowflake
        """.trimIndent()
        )
            .map { row, metadata -> converter.read(ShortPost::class.java, row, metadata) }
            .all()
            .asFlow()

    fun findAllByAuthor(snowflake: Long) =
        client.sql(
            """
                SELECT p.snowflake, p.author, p.content, count(l.liker) like_count, count(c.commenter) comment_count
                FROM posts p
                         LEFT JOIN likes l ON p.snowflake = l.post
                         LEFT JOIN comments c on p.snowflake = c.post
                WHERE p.author = $1
                GROUP BY p.snowflake
            """.trimIndent()
        )
            .bind(0, snowflake)
            .map { row, metadata -> converter.read(ShortPost::class.java, row, metadata) }
            .all()
            .asFlow()
}