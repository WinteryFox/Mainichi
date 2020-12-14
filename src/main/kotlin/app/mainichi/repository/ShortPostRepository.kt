package app.mainichi.repository

import app.mainichi.`object`.ShortPost
import org.springframework.data.r2dbc.convert.R2dbcConverter
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

@Component
class ShortPostRepository(
    private val client: DatabaseClient,
    private val converter: R2dbcConverter
) {
    fun findAll(): Flux<ShortPost> =
        client.sql("""
            SELECT p.*,
                   count(l.*) like_count,
                   count(c.*) comment_count
            FROM posts p
                     LEFT JOIN likes l ON p.snowflake = l.post
                     LEFT JOIN comments c ON p.snowflake = c.post
            GROUP BY p.snowflake
        """.trimIndent())
            .map { row, metadata -> converter.read(ShortPost::class.java, row, metadata) }
            .all()

    fun findAllByAuthor(author: Long): Flux<ShortPost> =
        client.sql("""
            SELECT p.*,
                   count(l.*) like_count,
                   count(c.*) comment_count
            FROM posts p
                     LEFT JOIN likes l ON p.snowflake = l.post
                     LEFT JOIN comments c ON p.snowflake = c.post
            WHERE p.author = :author
            GROUP BY p.snowflake
        """.trimIndent())
            .bind("author", author)
            .map { row, metadata -> converter.read(ShortPost::class.java, row, metadata) }
            .all()

    fun findBySnowflake(snowflake: Long): Flux<ShortPost> =
        client.sql("""
            SELECT p.*,
                   count(l.*) like_count,
                   count(c.*) comment_count
            FROM posts p
                     LEFT JOIN likes l ON p.snowflake = l.post
                     LEFT JOIN comments c ON p.snowflake = c.post
            WHERE p.snowflake = :snowflake
            GROUP BY p.snowflake
        """.trimIndent())
            .bind("snowflake", snowflake)
            .map { row, metadata -> converter.read(ShortPost::class.java, row, metadata) }
            .all()
}