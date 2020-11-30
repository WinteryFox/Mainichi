package app.mainichi.objects

import org.springframework.data.relational.core.mapping.Table

@Table
data class Learning(
    val snowflake: Long,
    val language: String,
    val profiency: Short
)
