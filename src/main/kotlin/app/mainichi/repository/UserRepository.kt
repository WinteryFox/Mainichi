package app.mainichi.repository

import app.mainichi.table.User
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface UserRepository : CoroutineCrudRepository<User, String> {
    suspend fun findByEmail(email: String): User?
}