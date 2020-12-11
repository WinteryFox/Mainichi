package app.mainichi.repository

import app.mainichi.`object`.FullPost
import app.mainichi.`object`.ShortPost
import org.springframework.data.r2dbc.convert.R2dbcConverter
import org.springframework.r2dbc.core.DatabaseClient

class FullPostRepository(
    private val client: DatabaseClient,
    private val converter: R2dbcConverter
) {
    fun findBySnowflake(snowflake: Long) =
        client.sql(
            """
            SELECT p.snowflake                                AS snowflake,
                   p.author                                   AS author,
                   p.content                                  AS content,
                   array_remove(array_agg(l.liker), NULL)     AS likers,
                   array_remove(array_agg(c.snowflake), NULL) AS comment_snowflakes,
                   array_remove(array_agg(c.post), NULL)      AS comment_posts,
                   array_remove(array_agg(c.commenter), NULL) AS comment_commenters,
                   array_remove(array_agg(c.content), NULL)   AS comment_contents
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
}