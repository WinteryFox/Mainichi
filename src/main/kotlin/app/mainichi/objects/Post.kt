package app.mainichi.objects

import org.springframework.data.relational.core.mapping.Table

@Table
data class Post(
        val snowflake: Long,
        val author: Long,
        val content: String
)