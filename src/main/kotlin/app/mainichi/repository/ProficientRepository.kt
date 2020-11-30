package app.mainichi.repository

import app.mainichi.objects.Proficient
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ProficientRepository : CoroutineCrudRepository<Proficient, String> {
    @Query("SELECT * FROM proficient WHERE snowflake = :snowflake")
    suspend fun getBySnowflake(code: String): Proficient?
}