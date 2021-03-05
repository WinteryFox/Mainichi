package app.mainichi.repository

import app.mainichi.table.Like
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.convert.R2dbcConverter
import org.springframework.http.HttpStatus
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitSingleOrNull
import org.springframework.stereotype.Repository
import org.springframework.web.server.ResponseStatusException

@Repository
class EditLikeRepository(
    @Autowired
    val client: DatabaseClient,
    val converter: R2dbcConverter
) {
    suspend fun save(entity: Like): Like =
        client.sql(
            """
            INSERT INTO likes(post, liker)
            VALUES (:post, :liker)
            ON CONFLICT (post, liker) DO NOTHING
            RETURNING *
        """.trimIndent()
        )
            .bind("post", entity.post)
            .bind("liker", entity.liker)
            .map { row, metadata -> converter.read(Like::class.java, row, metadata) }
            .awaitSingleOrNull() ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST)

    suspend fun delete(entity: Like) {
        if (client.sql(
                """
                DELETE FROM likes WHERE liker = :liker AND post = :post
            """.trimIndent()

            ).bind("post", entity.post)
                .bind("liker", entity.liker)
                .fetch()
                .rowsUpdated()
                .awaitSingle()
            != 1
        )
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
    }
}
