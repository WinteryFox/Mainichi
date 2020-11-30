package app.mainichi.repository

import app.mainichi.objects.Like
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface LikeRepository : CoroutineCrudRepository<Like, String> {
    @Query("SELECT * FROM likes WHERE snowflake = :snowflake")
    suspend fun getBySnowflake(code: String): List<Like>
}