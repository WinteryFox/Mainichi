package app.mainichi.repository

import app.mainichi.objects.Comment
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PostRepository : CoroutineCrudRepository<Comment, String> {
    @Query("SELECT * FROM comment WHERE snowflake = :snowflake")
    suspend fun getBySnowflake(code: String): Comment?

    @Query("SELECT * FROM comment WHERE commenter = :commenter")
    suspend fun getByCommenter(code: String): Comment?

    @Query("SELECT * FROM comment WHERE post = :post")
    suspend fun getByPost(code: String): Comment?
}