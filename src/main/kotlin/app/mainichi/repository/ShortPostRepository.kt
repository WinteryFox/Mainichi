package app.mainichi.repository

import app.mainichi.`object`.ShortPost
import org.springframework.data.r2dbc.convert.R2dbcConverter
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitSingleOrNull
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

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
                     LEFT JOIN likes l ON p.id = l.post
                     LEFT JOIN comments c ON p.id = c.post
            GROUP BY p.id
        """.trimIndent())
            .map { row, metadata -> converter.read(ShortPost::class.java, row, metadata) }
            .all()

    fun findAllByAuthor(author: Long): Flux<ShortPost> =
        client.sql("""
            SELECT p.*,
                   count(l.*) like_count,
                   count(c.*) comment_count
            FROM posts p
                     LEFT JOIN likes l ON p.id = l.post
                     LEFT JOIN comments c ON p.id = c.post
            WHERE p.author = :author
            GROUP BY p.id
        """.trimIndent())
            .bind("author", author)
            .map { row, metadata -> converter.read(ShortPost::class.java, row, metadata) }
            .all()

    suspend fun findByid(id: Long): ShortPost? =
        client.sql("""
            SELECT p.*,
                   count(l.*) like_count,
                   count(c.*) comment_count
            FROM posts p
                     LEFT JOIN likes l ON p.id = l.post
                     LEFT JOIN comments c ON p.id = c.post
            WHERE p.id = :id
            GROUP BY p.id
        """.trimIndent())
            .bind("id", id)
            .map { row, metadata -> converter.read(ShortPost::class.java, row, metadata) }
            .awaitSingleOrNull()
}