package app.mainichi.repository

import app.mainichi.`object`.ShortPost
import org.springframework.data.r2dbc.convert.R2dbcConverter
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitSingleOrNull
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
                   count(DISTINCT l.*) like_count,
                   count(DISTINCT c.*) comment_count
            FROM posts p
                     LEFT JOIN likes l ON p.id = l.post
                     LEFT JOIN comments c ON p.id = c.post
            GROUP BY p.id
            LIMIT 30
        """)
            .map { row, metadata -> converter.read(ShortPost::class.java, row, metadata) }
            .all()

    fun findAllByAuthor(author: Long): Flux<ShortPost> =
        client.sql("""
            SELECT p.*,
                   count(DISTINCT l.*) like_count,
                   count(DISTINCT c.*) comment_count
            FROM posts p
                     LEFT JOIN likes l ON p.id = l.post
                     LEFT JOIN comments c ON p.id = c.post
            WHERE p.author = :author
            GROUP BY p.id
        """)
            .bind("author", author)
            .map { row, metadata -> converter.read(ShortPost::class.java, row, metadata) }
            .all()

    suspend fun findById(id: Long): ShortPost? =
        client.sql("""
            SELECT p.*,
                   count(DISTINCT l.*) like_count,
                   count(DISTINCT c.*) comment_count
            FROM posts p
                     LEFT JOIN likes l ON p.id = l.post
                     LEFT JOIN comments c ON p.id = c.post
            WHERE p.id = :id
            GROUP BY p.id
        """)
            .bind("id", id)
            .map { row, metadata -> converter.read(ShortPost::class.java, row, metadata) }
            .awaitSingleOrNull()
}