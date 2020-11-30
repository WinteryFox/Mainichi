package app.mainichi.objects

import org.springframework.data.relational.core.mapping.Table

@Table
data class Proficient(
    val snowflake: Long,
    val language: String
)
