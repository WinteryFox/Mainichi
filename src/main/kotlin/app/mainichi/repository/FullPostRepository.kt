package app.mainichi.repository

import app.mainichi.`object`.FullPost
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.data.r2dbc.convert.R2dbcConverter
import org.springframework.r2dbc.core.DatabaseClient

class FullPostRepository(
    private val client: DatabaseClient,
    private val converter: R2dbcConverter
) {
    fun findAll(): Flow<FullPost> =
        client.sql(
            """
            SELECT p.snowflake, p.author, p.content, array_agg(l.liker) likes
            FROM posts p
            JOIN likes l ON p.snowflake = l.post
            GROUP BY p.snowflake
        """.trimIndent()
        )
            .map { row, metadata -> converter.read(FullPost::class.java, row, metadata) }
            .all()
            .asFlow()
}