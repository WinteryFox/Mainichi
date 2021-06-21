package app.mainichi.repository

import app.mainichi.table.User
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.time.LocalDate

interface UserRepository : CoroutineCrudRepository<User, String> {
    suspend fun findByEmail(email: String): User?

    @Query(
        """
        SELECT *
        FROM users
        WHERE email = $1
        AND (password = crypt($2, password))
    """
    )
    suspend fun login(email: String, password: String): User?

    @Query(
        """
        INSERT INTO users (id, email, username, gender, birthday, summary, avatar, password, version)
        VALUES ($1, $2, $3, $4, $5, $6, null, crypt($7, gen_salt('md5')), 1)
        RETURNING *
    """
    )
    suspend fun register(
        id: Long,
        email: String,
        username: String,
        gender: Char?,
        birthday: LocalDate,
        summary: String,
        password: String
    ): User
}