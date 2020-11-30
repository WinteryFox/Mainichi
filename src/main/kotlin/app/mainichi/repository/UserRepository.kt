package app.mainichi.repository

import app.mainichi.objects.User
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface UserRepository : CoroutineCrudRepository<User, String> {
    @Query("SELECT * FROM users WHERE snowflake = :snowflake")
    suspend fun getBySnowflake(snowflake: Long): User?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getByEmail(email: String): User?
}