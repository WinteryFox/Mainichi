package app.mainichi.repository

import app.mainichi.table.User
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface UserRepository : CoroutineCrudRepository<User, String> {
    @Query
    suspend fun findByEmail(email: String): User?
}