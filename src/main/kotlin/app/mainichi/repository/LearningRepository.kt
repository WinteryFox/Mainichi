package app.mainichi.repository

import app.mainichi.objects.Comment
import app.mainichi.objects.Learning
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface LearningRepository: CoroutineCrudRepository<Learning, String> {
    @Query("SELECT * FROM learning WHERE snowflake = :snowflake")
    suspend fun getBySnowflake(code: String): List<Learning>
}