package app.mainichi.objects

import app.mainichi.data.SnowflakePersistable
import org.springframework.data.relational.core.mapping.Table
import java.sql.Date

@Table("users")
data class User(
    val snowflake: Long,
    val email: String,
    val username: String,
    val birthday: Date?,
    val gender: Char?,
    val summary: String?
) : SnowflakePersistable()