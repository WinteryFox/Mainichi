package app.mainichi.table

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate

@Table("users")
data class User(
    @Id
    val snowflake: Long,
    val email: String,
    val username: String,
    val birthday: LocalDate?,
    val gender: Char?,
    val summary: String?,
    val avatar: String?
)