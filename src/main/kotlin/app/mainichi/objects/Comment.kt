package app.mainichi.objects

import org.springframework.data.relational.core.mapping.Table

@Table
data class Comment(
    val snowflake: Long,
    val author: Long,
    val content: String,
)
