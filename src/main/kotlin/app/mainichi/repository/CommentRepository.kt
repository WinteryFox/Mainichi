package app.mainichi.repository

import app.mainichi.objects.Comment
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface CommentRepository: CoroutineCrudRepository<Comment, String> {
    @Query("SELECT * FROM likes WHERE snowflake = :snowflake")
    suspend fun getBySnowflake(code: String): Comment

    @Query("SELECT * FROM likes WHERE author = :author")
    suspend fun getByAuthor(code: String): List<Comment>

    @Query("SELECT * FROM likes WHERE post = :post")
    suspend fun getByPost(code: String): List<Comment>
}